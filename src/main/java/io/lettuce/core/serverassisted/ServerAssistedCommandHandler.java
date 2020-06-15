package io.lettuce.core.serverassisted;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.output.ReplayOutput;
import io.lettuce.core.protocol.CommandHandler;
import io.lettuce.core.protocol.RedisCommand;
import io.lettuce.core.resource.ClientResources;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;


public class ServerAssistedCommandHandler<K, V> extends CommandHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(
        ServerAssistedCommandHandler.class);

    private final ServerAssistedEndpoint<K, V> endpoint;
    private final RedisCodec<K,V> codec;
    private ServerAssistedOutput<K,V,V> output;

    private final Deque<ReplayOutput<K, V>> queue = new ArrayDeque<>();
    private ResponseHeaderReplayOutput<K, V> replay;


    /**
     * Initialize a new instance that handles commands from the supplied queue.
     *
     * @param clientOptions client options for this connection, must not be {@literal null}
     * @param clientResources client resources for this connection, must not be {@literal null}
     * @param endpoint must not be {@literal null}.
     */
    public ServerAssistedCommandHandler(ClientOptions clientOptions, ClientResources clientResources, ServerAssistedEndpoint<K,V> endpoint, RedisCodec<K, V> codec) {
        super(clientOptions, clientResources, endpoint);

        this.endpoint = endpoint;
        this.codec = codec;
        this.output = new ServerAssistedOutput<>(codec);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        replay = null;
        queue.clear();

        super.channelInactive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer) throws InterruptedException {
        if (output.type() != null && !output.isCompleted()) {

            if (!super.decode(buffer, output)) {
                return;
            }

            RedisCommand<?, ?, ?> peek = getStack().peek();
            canComplete(peek);
            doNotifyMessage(output);
            output = new ServerAssistedOutput<>(codec);
        }

        if (!getStack().isEmpty()) {
            super.decode(ctx, buffer);
        }

        ReplayOutput<K, V> replay;
        while ((replay = queue.poll()) != null) {

            replay.replay(output);
            doNotifyMessage(output);
            output = new ServerAssistedOutput<>(codec);
        }

        while (super.getStack().isEmpty() && buffer.isReadable()) {

            if (!super.decode(buffer, output)) {
                return;
            }

            doNotifyMessage(output);
            output = new ServerAssistedOutput<>(codec);
        }

        buffer.discardReadBytes();

    }

    @Override
    protected boolean canDecode(ByteBuf buffer) {
        return super.canDecode(buffer) && output.type() == null;
    }

    @Override
    protected boolean canComplete(RedisCommand<?, ?, ?> command) {

        if (isInvalidationMessage(replay)) {

            queue.add(replay);
            replay = null;
            return false;
        }

        return super.canComplete(command);
    }

    @Override
    protected void complete(RedisCommand<?, ?, ?> command) {

        if (replay != null && command.getOutput() != null) {
            try {
                replay.replay(command.getOutput());
            } catch (Exception e) {
                command.completeExceptionally(e);
            }
            replay = null;
        }

        super.complete(command);
    }

    /**
     * Check whether {@link ResponseHeaderReplayOutput} contains a Pub/Sub message that requires Pub/Sub dispatch instead of to
     * be used as Command output.
     *
     * @param replay
     * @return
     */
    private static boolean isInvalidationMessage(ResponseHeaderReplayOutput<?, ?> replay) {

        if (replay == null) {
            return false;
        }

        String firstElement = replay.firstElement;
        if (replay.multiCount != null && firstElement != null) {

            if (replay.multiCount == 2 && firstElement.equalsIgnoreCase(ServerAssistedOutput.Type.invalidate.name())) {
                return true;
            }
        }

        return false;
    }


    protected CommandOutput<?, ?, ?> getCommandOutput(RedisCommand<?, ?, ?> command) {

        if (getStack().isEmpty() || command.getOutput() == null) {
            return super.getCommandOutput(command);
        }

        if (replay == null) {
            replay = new ResponseHeaderReplayOutput<>();
        }

        return replay;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterDecode(ChannelHandlerContext ctx, RedisCommand<?, ?, ?> command) {

        if (command.getOutput() instanceof ServerAssistedOutput) {
            doNotifyMessage((ServerAssistedOutput) command.getOutput());
        }
    }

    private void doNotifyMessage(ServerAssistedOutput<K, V, V> output) {
        try {
            endpoint.notifyMessage(output);
        } catch (Exception e) {
            logger.error("Unexpected error occurred in ServerAssistedEndpoint.notifyMessage", e);
        }
    }
    /**
     * Inspectable {@link ReplayOutput} to investigate the first multi and string response elements.
     *
     * @param <K>
     * @param <V>
     */
    static class ResponseHeaderReplayOutput<K, V> extends ReplayOutput<K, V> {

        Integer multiCount;
        String firstElement;

        @Override
        public void set(ByteBuffer bytes) {

            if (firstElement == null && bytes != null && bytes.remaining() > 0) {

                bytes.mark();
                firstElement = StringCodec.ASCII.decodeKey(bytes);
                bytes.reset();
            }

            super.set(bytes);
        }

        @Override
        public void multi(int count) {

            if (multiCount == null) {
                multiCount = count;
            }

            super.multi(count);
        }
    }

}
