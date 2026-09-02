package com.breakinblocks.directorscut.shake;

import com.mojang.blaze3d.vertex.PoseStack;

public interface ScreenShake {
    void process(PoseStack poseStack, int time, float partialTicks);

    boolean hasEnded(int elapsedTime);
}
