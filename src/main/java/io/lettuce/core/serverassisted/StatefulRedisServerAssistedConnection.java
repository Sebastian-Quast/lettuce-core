package io.lettuce.core.serverassisted;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.serverassisted.api.async.RedisServerAssistedAsyncCommands;
import io.lettuce.core.serverassisted.api.sync.RedisServerAssistedCommands;


public interface StatefulRedisServerAssistedConnection<K, V> extends StatefulRedisConnection<K, V> {

    /**
     * Returns the {@link RedisServerAssistedCommands} API for the current connection. Does not create a new connection.
     *
     * @return the synchronous API for the underlying connection.
     */
    RedisServerAssistedCommands<K, V> sync();

    /**
     * Returns the {@link RedisServerAssistedAsyncCommands} API for the current connection. Does not create a new connection.
     *
     * @return the asynchronous API for the underlying connection.
     */
    RedisServerAssistedAsyncCommands<K, V> async();

    /**
     * Add a new {@link RedisServerAssistedListener listener}.
     *
     * @param listener the listener, must not be {@literal null}.
     */
    void addListener(RedisServerAssistedListener<K, V> listener);

    /**
     * Remove an existing {@link RedisServerAssistedListener listener}..
     *
     * @param listener the listener, must not be {@literal null}.
     */
    void removeListener(RedisServerAssistedListener<K, V> listener);

}
