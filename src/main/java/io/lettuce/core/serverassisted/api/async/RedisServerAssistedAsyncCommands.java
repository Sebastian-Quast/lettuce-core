package io.lettuce.core.serverassisted.api.async;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.serverassisted.StatefulRedisServerAssistedConnection;

public interface RedisServerAssistedAsyncCommands<K, V> extends RedisAsyncCommands<K, V> {

    RedisFuture<String> clientTracking(String state);

    /**
     * @return the underlying connection.
     */
    StatefulRedisServerAssistedConnection<K, V> getStatefulConnection();
}
