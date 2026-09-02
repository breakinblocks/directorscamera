package com.breakinblocks.directorscamera.client;

public interface ClientCutsceneHooks {
    ClientCutsceneHooks NONE = new ClientCutsceneHooks() {
    };

    default void started(String id) {
    }

    default void ended(String id, String reason) {
    }

    final class Holder {
        private static ClientCutsceneHooks instance = NONE;

        private Holder() {
        }

        public static ClientCutsceneHooks get() {
            return instance;
        }

        public static void set(ClientCutsceneHooks hooks) {
            instance = hooks == null ? NONE : hooks;
        }
    }
}
