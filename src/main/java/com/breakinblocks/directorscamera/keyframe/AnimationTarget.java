package com.breakinblocks.directorscamera.keyframe;

import org.jetbrains.annotations.Nullable;

public interface AnimationTarget {
    void resetPose();

    @Nullable
    AnimatedBone getBone(String name);
}
