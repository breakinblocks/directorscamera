package com.breakinblocks.directorscut.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class DirectorsCutConfig {
    public static final ModConfigSpec clientSpec;
    public static final ClientConfig CLIENT;
    public static final ModConfigSpec serverSpec;
    public static final ServerConfig SERVER;

    static {
        Pair<ClientConfig, ModConfigSpec> clientPair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        clientSpec = clientPair.getRight();
        CLIENT = clientPair.getLeft();
        Pair<ServerConfig, ModConfigSpec> serverPair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        serverSpec = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    public static class ClientConfig {
        public final ModConfigSpec.BooleanValue showSkipHint;
        public final ModConfigSpec.BooleanValue closeScreenOnStart;

        ClientConfig(ModConfigSpec.Builder builder) {
            builder.push("client");
            showSkipHint = builder
                .comment("Show the hold-to-skip hint at low alpha while the jump key is not held")
                .translation("directorscut.configuration.showSkipHint")
                .define("showSkipHint", true);
            closeScreenOnStart = builder
                .comment("Close any open screen when a cutscene starts")
                .translation("directorscut.configuration.closeScreenOnStart")
                .define("closeScreenOnStart", true);
            builder.pop();
        }
    }

    public static class ServerConfig {
        public final ModConfigSpec.IntValue startTimeoutTicks;

        ServerConfig(ModConfigSpec.Builder builder) {
            builder.push("server");
            startTimeoutTicks = builder
                .comment("Ticks to wait for a client to confirm that a cutscene started before the session is dropped")
                .translation("directorscut.configuration.startTimeoutTicks")
                .defineInRange("startTimeoutTicks", 40, 1, 1200);
            builder.pop();
        }
    }
}
