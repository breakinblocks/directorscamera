package com.breakinblocks.directorscut.keyframe;

import com.breakinblocks.directorscut.expression.ExpressionContext;
import org.jetbrains.annotations.Nullable;

public class AnimationContext extends ExpressionContext {
    @Nullable
    private Animation animation;
    @Nullable
    private LoopMode currentLoopMode;

    public AnimationContext(@Nullable Animation animation, @Nullable LoopMode loopMode) {
        super(true);
        this.animation = animation;
        this.currentLoopMode = loopMode;
    }

    public AnimationContext() {
        this(null, null);
    }

    @Nullable
    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(@Nullable Animation animation) {
        this.animation = animation;
    }

    @Nullable
    public LoopMode getCurrentLoopMode() {
        return currentLoopMode;
    }

    public void setCurrentLoopMode(@Nullable LoopMode mode) {
        this.currentLoopMode = mode;
    }
}
