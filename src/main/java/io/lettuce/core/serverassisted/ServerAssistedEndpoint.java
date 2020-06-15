package io.lettuce.core.serverassisted;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.DefaultEndpoint;
import io.lettuce.core.resource.ClientResources;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ServerAssistedEndpoint<K, V> extends DefaultEndpoint {


    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerAssistedEndpoint.class);
    private final List<RedisServerAssistedListener<K, V>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Create a new {@link DefaultEndpoint}.
     *
     * @param clientOptions client options for this connection, must not be {@literal null}.
     * @param clientResources client resources for this connection, must not be {@literal null}.
     */
    public ServerAssistedEndpoint(ClientOptions clientOptions,
        ClientResources clientResources) {
        super(clientOptions, clientResources);
    }

    /**
     * Add a new {@link RedisServerAssistedListener listener}.
     *
     * @param listener the listener, must not be {@literal null}.
     */
    public void addListener(RedisServerAssistedListener<K, V> listener) {
        listeners.add(listener);
    }

    /**
     * Remove an existing {@link RedisServerAssistedListener listener}..
     *
     * @param listener the listener, must not be {@literal null}.
     */
    public void removeListener(RedisServerAssistedListener<K, V> listener) {
        listeners.remove(listener);
    }

    protected List<RedisServerAssistedListener<K, V>> getListeners() {
        return listeners;
    }

    public void notifyMessage(ServerAssistedOutput<K, V, V> output) {

        // drop empty messages
        if (output.type() == null || output.channel() == null && output.get() == null) {
            return;
        }
        try {
            notifyListeners(output);
        } catch (Exception e) {
            logger.error("Unexpected error occurred in RedisPubSubListener callback", e);
        }
    }

    protected void notifyListeners(ServerAssistedOutput<K, V, V> output) {
        // update listeners
        for (RedisServerAssistedListener<K, V> listener : listeners) {
            listener.message(output.channel(), output.get());
        }
    }

}
