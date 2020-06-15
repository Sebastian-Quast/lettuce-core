package io.lettuce.core.serverassisted;

import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.serverassisted.api.async.RedisServerAssistedAsyncCommands;
import io.lettuce.core.codec.RedisCodec;


public class RedisServerAssistedAsyncCommandsImpl<K,V> extends RedisAsyncCommandsImpl<K,V> implements
    RedisServerAssistedAsyncCommands<K,V> {

    private final ServerAssistedCommandBuilder<K, V> commandBuilder;

    /**
     * Initialize a new instance.
     *
     * @param connection the connection to operate on
     * @param codec the codec for command encoding
     */
    public RedisServerAssistedAsyncCommandsImpl(StatefulRedisConnection<K, V> connection,
        RedisCodec<K, V> codec) {
        super(connection, codec);
        this.commandBuilder = new ServerAssistedCommandBuilder<>(codec);
    }

    @Override
    public RedisFuture<String> clientTracking(String state) {
        return (RedisFuture<String>) dispatch(commandBuilder.clientTracking(state));
    }

    @Override
    @SuppressWarnings("unchecked")
    public StatefulRedisServerAssistedConnection<K, V> getStatefulConnection() {
        return (StatefulRedisServerAssistedConnection<K, V>) super.getStatefulConnection();
    }
}
