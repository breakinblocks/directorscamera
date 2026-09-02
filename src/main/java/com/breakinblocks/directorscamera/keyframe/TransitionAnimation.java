package com.breakinblocks.directorscamera.keyframe;

import net.minecraft.resources.Identifier;

import java.util.Map;

public class TransitionAnimation extends Animation {
    private final Animation transitionTo;

    public TransitionAnimation(Identifier name, int animTime, LoopMode defaultLoopMode, Map<String, BoneAnimation> bones, Animation transitionTo) {
        super(name, animTime, defaultLoopMode, bones);
        this.transitionTo = transitionTo;
    }

    public Animation getTransitionTo() {
        return transitionTo;
    }

    @Override
    public Animation resolveTarget() {
        return transitionTo.resolveTarget();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TransitionAnimation transition) {
            return transition.transitionTo == transitionTo;
        }
        return other == transitionTo;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(transitionTo);
    }
}
