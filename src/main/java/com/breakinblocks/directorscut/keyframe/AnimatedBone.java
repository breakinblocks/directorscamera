package com.breakinblocks.directorscut.keyframe;

public interface AnimatedBone {
    void addPosition(float x, float y, float z);

    void addRotation(float x, float y, float z);

    void multiplyScale(float x, float y, float z);
}
