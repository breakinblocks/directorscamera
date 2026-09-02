package com.breakinblocks.directorscut.keyframe;

import org.jetbrains.annotations.Nullable;

public interface AnimationTarget {
    void resetPose();

    @Nullable
    AnimatedBone getBone(String name);
}
