package io.lettuce.core.serverassisted;

import static io.lettuce.core.protocol.CommandType.CLIENT;
import static io.lettuce.core.protocol.CommandType.TRACKING;

import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.BaseRedisCommandBuilder;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;


public class ServerAssistedCommandBuilder<K, V> extends BaseRedisCommandBuilder<K, V> {


    public ServerAssistedCommandBuilder(RedisCodec<K, V> codec) {
        super(codec);
    }

    Command<K, V, String> clientTracking(String state) {
        CommandArgs<K, V> args = new CommandArgs<>(codec).add(TRACKING).add(state);
        return createCommand(CLIENT, new StatusOutput<>(codec), args);
    }

    <T> Command<K, V, T> clientCachingCommand(CommandType type, CommandOutput<K, V, T> output, K... keys) {
        return new Command<>(type, null, null);
    }

    protected <T> Command<K, V, T> createCommand(CommandType type, CommandOutput<K, V, T> output, CommandArgs<K, V> args) {
        return new Command<>(type, output, args);
    }

}
