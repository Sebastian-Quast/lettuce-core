package io.lettuce.core.serverassisted;


public class RedisServerAssistedAdapter<K, V> implements RedisServerAssistedListener<K, V> {

    @Override
    public void message(K channel, V message) {
        // empty adapter method
    }
}
