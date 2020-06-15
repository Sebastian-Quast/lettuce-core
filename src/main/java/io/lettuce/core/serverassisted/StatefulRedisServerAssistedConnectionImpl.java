package io.lettuce.core.serverassisted;

import io.lettuce.core.RedisChannelWriter;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.serverassisted.api.async.RedisServerAssistedAsyncCommands;
import io.lettuce.core.serverassisted.api.sync.RedisServerAssistedCommands;
import io.lettuce.core.codec.RedisCodec;
import java.time.Duration;


public class StatefulRedisServerAssistedConnectionImpl<K, V> extends StatefulRedisConnectionImpl<K, V>
    implements StatefulRedisServerAssistedConnection<K, V> {

    private final ServerAssistedEndpoint endpoint;

    /**
     * Initialize a new connection.
     *
     * @param writer the channel writer
     * @param codec Codec used to encode/decode keys and values.
     * @param timeout Maximum time to wait for a response.
     */
    public StatefulRedisServerAssistedConnectionImpl(ServerAssistedEndpoint endpoint, RedisChannelWriter writer,
        RedisCodec<K, V> codec, Duration timeout) {
        super(writer, codec, timeout);

        this.endpoint = endpoint;
    }

    @Override
    public void addListener(RedisServerAssistedListener<K, V> listener) {
        endpoint.addListener(listener);
    }

    @Override
    public void removeListener(RedisServerAssistedListener<K, V> listener) {
        endpoint.removeListener(listener);
    }

    @Override
    public RedisServerAssistedCommands<K, V> sync() {
        return (RedisServerAssistedCommands<K, V>) sync;
    }

    @Override
    protected RedisServerAssistedCommands<K, V> newRedisSyncCommandsImpl() {
        return syncHandler(async(), RedisServerAssistedCommands.class);
    }

    @Override
    protected RedisServerAssistedAsyncCommandsImpl<K, V> newRedisAsyncCommandsImpl() {
        return new RedisServerAssistedAsyncCommandsImpl<>(this, codec);
    }

    @Override
    public RedisServerAssistedAsyncCommands<K, V> async() {
        return (RedisServerAssistedAsyncCommands<K, V>) async;
    }

}
