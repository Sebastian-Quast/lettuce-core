package io.lettuce.core.serverassisted;

public interface RedisServerAssistedListener<K, V> {

    /**
     * Message received from a channel subscription.
     *
     * @param channel Channel.
     * @param message Message.
     */
    void message(K channel, V message);

}
