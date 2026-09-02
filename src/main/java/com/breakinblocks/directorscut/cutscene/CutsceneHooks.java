package com.breakinblocks.directorscut.cutscene;

import net.minecraft.server.level.ServerPlayer;

public interface CutsceneHooks {
    CutsceneHooks NONE = new CutsceneHooks() {
    };

    default boolean beforePlay(ServerPlayer player, CutsceneDefinition definition) {
        return true;
    }

    default void started(ServerPlayer player, String id) {
    }

    default void tick(ServerPlayer player, String id, int tick) {
    }

    default void ended(ServerPlayer player, String id, String reason) {
    }

    default void collectDefinitions() {
    }

    final class Holder {
        private static CutsceneHooks instance = NONE;

        private Holder() {
        }

        public static CutsceneHooks get() {
            return instance;
        }

        public static void set(CutsceneHooks hooks) {
            instance = hooks == null ? NONE : hooks;
        }
    }
}
