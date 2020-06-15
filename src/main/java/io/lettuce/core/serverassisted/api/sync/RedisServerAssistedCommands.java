package io.lettuce.core.serverassisted.api.sync;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.serverassisted.StatefulRedisServerAssistedConnection;


public interface RedisServerAssistedCommands<K, V> extends RedisCommands<K, V> {

    String clientTracking(String state);

    StatefulRedisServerAssistedConnection<K, V> getStatefulConnection();
}
