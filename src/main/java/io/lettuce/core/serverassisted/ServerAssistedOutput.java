package io.lettuce.core.serverassisted;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.CommandOutput;
import java.nio.ByteBuffer;


public class ServerAssistedOutput<K,V,T> extends CommandOutput<K,V,T> {

    public enum Type {
        invalidate
    }

    private Type type;
    private K channel;
    private boolean completed;


    /**
     * Initialize a new instance that encodes and decodes keys and values using the supplied codec.
     *
     * @param codec Codec used to encode/decode keys and values, must not be {@literal null}.
     */
    public ServerAssistedOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    @SuppressWarnings({ "fallthrough", "unchecked" })
    public void set(ByteBuffer bytes) {

        if (bytes == null) {
            return;
        }

        if (type == null) {
            type = Type.valueOf(decodeAscii(bytes));
            return;
        }

        handleOutput(bytes);
    }

    private void handleOutput(ByteBuffer bytes) {
        if (channel == null) {
            channel = codec.decodeKey(bytes);
        }
        output = (T) codec.decodeValue(bytes);
        completed = true;
    }

    public Type type() {
        return type;
    }

    public K channel() {
        return channel;
    }

    boolean isCompleted() {
        return completed;
    }
}
