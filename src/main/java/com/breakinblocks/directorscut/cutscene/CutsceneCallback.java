package com.breakinblocks.directorscut.cutscene;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface CutsceneCallback {
    void run(ServerPlayer player);
}
