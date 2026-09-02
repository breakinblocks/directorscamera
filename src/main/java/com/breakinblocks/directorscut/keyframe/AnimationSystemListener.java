package com.breakinblocks.directorscut.keyframe;

public interface AnimationSystemListener {
    AnimationSystemListener NONE = new AnimationSystemListener() {
    };

    default void onAnimationStart(String layer, AnimationTicker ticker) {
    }

    default void onAnimationStop(String layer) {
    }

    default void onFreeze(boolean frozen) {
    }

    default void onVariableAdded(String name, float value) {
    }
}
