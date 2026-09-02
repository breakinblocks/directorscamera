package com.breakinblocks.directorscamera.cutscene;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface CutsceneCallback {
    void run(ServerPlayer player);
}
