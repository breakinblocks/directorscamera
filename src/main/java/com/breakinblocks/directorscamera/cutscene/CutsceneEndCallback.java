package com.breakinblocks.directorscamera.cutscene;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface CutsceneEndCallback {
    void run(ServerPlayer player, String reason);
}
