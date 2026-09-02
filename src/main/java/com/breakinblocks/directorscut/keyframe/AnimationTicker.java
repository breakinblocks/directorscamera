package com.breakinblocks.directorscut.keyframe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class AnimationTicker {
    public static final StreamCodec<FriendlyByteBuf, AnimationTicker> STREAM_CODEC = StreamCodec.of(AnimationTicker::write, AnimationTicker::read);

    private Animation animation;
    private float elapsedTime;
    private float speedModifier = 1.0F;
    private int toNullTransitionTime;
    private boolean reversed;
    private LoopMode loopMode;
    @Nullable
    private AnimationTicker next;
    private boolean important;

    public AnimationTicker(Animation animation) {
        this.animation = animation;
        this.toNullTransitionTime = animation.getAnimTime();
        this.loopMode = animation.getDefaultLoopMode();
    }

    public AnimationTicker(AnimationTicker other) {
        this.animation = other.animation;
        this.elapsedTime = other.elapsedTime;
        this.speedModifier = other.speedModifier;
        this.toNullTransitionTime = other.toNullTransitionTime;
        this.reversed = other.reversed;
        this.loopMode = other.loopMode;
        this.next = other.next;
        this.important = other.important;
    }

    public static Builder builder(Animation animation) {
        return new Builder(animation);
    }

    public static Builder builder(Supplier<Animation> animation) {
        return new Builder(animation.get());
    }

    public void tick() {
        int animTime = animation.getAnimTime();
        if (loopMode != LoopMode.LOOP) {
            elapsedTime = Mth.clamp(elapsedTime + speedModifier, 0.0F, animTime);
            return;
        }
        elapsedTime += speedModifier;
        if (animation instanceof TransitionAnimation transition && elapsedTime >= animTime) {
            animation = transition.getTransitionTo();
            animTime = animation.getAnimTime();
        }
        if (animTime > 0) {
            elapsedTime = elapsedTime % animTime;
        } else {
            elapsedTime = 0.0F;
        }
    }

    public boolean hasEnded() {
        if (loopMode == LoopMode.LOOP) {
            return false;
        }
        return elapsedTime >= animation.getAnimTime();
    }

    public float getTime(float partialTicks) {
        float p = partialTicks * speedModifier;
        float t = animation.getAnimTime();
        float e = elapsedTime;
        if (loopMode != LoopMode.LOOP) {
            return reversed ? Mth.clamp(t - e - p, 0.0F, t) : Mth.clamp(e + p, 0.0F, t);
        }
        if (t <= 0) {
            return 0.0F;
        }
        if (!reversed) {
            return (e + p) % t;
        }
        float r = t - e - p;
        return r < 0 ? t + r : r;
    }

    public void addVariables(AnimationContext context, float partialTicks) {
        context.addVariable("query.anim_time", (elapsedTime + partialTicks * speedModifier) / 20.0F);
        context.addVariable("math.pi", (float) Math.PI);
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public void resetTime() {
        elapsedTime = 0.0F;
    }

    public float getSpeedModifier() {
        return speedModifier;
    }

    public void setSpeedModifier(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    public int getToNullTransitionTime() {
        return toNullTransitionTime;
    }

    public void setToNullTransitionTime(int toNullTransitionTime) {
        this.toNullTransitionTime = toNullTransitionTime;
    }

    public LoopMode getLoopMode() {
        return loopMode;
    }

    public void setLoopMode(LoopMode loopMode) {
        this.loopMode = loopMode;
    }

    @Nullable
    public AnimationTicker getNext() {
        return next;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean isImportant() {
        return important;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnimationTicker other)) {
            return false;
        }
        return animation.equals(other.animation)
            && toNullTransitionTime == other.toNullTransitionTime
            && loopMode == other.loopMode
            && speedModifier == other.speedModifier
            && reversed == other.reversed
            && Objects.equals(next, other.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(animation.resolveTarget().getName(), toNullTransitionTime, loopMode, speedModifier, reversed, next);
    }

    private static void write(FriendlyByteBuf buf, AnimationTicker ticker) {
        buf.writeBoolean(ticker.next != null);
        if (ticker.next != null) {
            write(buf, ticker.next);
        }
        buf.writeFloat(ticker.elapsedTime);
        buf.writeFloat(ticker.speedModifier);
        buf.writeInt(ticker.toNullTransitionTime);
        buf.writeBoolean(ticker.reversed);
        buf.writeUtf(ticker.loopMode.name());
        buf.writeUtf(ticker.animation.resolveTarget().getName().toString());
        buf.writeBoolean(ticker.important);
    }

    private static AnimationTicker read(FriendlyByteBuf buf) {
        AnimationTicker next = buf.readBoolean() ? read(buf) : null;
        float elapsed = buf.readFloat();
        float speed = buf.readFloat();
        int toNull = buf.readInt();
        boolean reversed = buf.readBoolean();
        LoopMode loopMode = LoopMode.valueOf(buf.readUtf());
        ResourceLocation id = ResourceLocation.parse(buf.readUtf());
        boolean important = buf.readBoolean();
        Animation animation = AnimationRegistry.get(id);
        if (animation == null) {
            throw new IllegalStateException("Unknown animation received from server: " + id);
        }
        AnimationTicker ticker = new AnimationTicker(animation);
        ticker.elapsedTime = elapsed;
        ticker.speedModifier = speed;
        ticker.toNullTransitionTime = toNull;
        ticker.reversed = reversed;
        ticker.loopMode = loopMode;
        ticker.next = next;
        ticker.important = important;
        return ticker;
    }

    public static class Builder {
        private final AnimationTicker ticker;

        public Builder(Animation animation) {
            this.ticker = new AnimationTicker(animation);
        }

        public Builder important() {
            ticker.important = true;
            return this;
        }

        public Builder nextAnimation(AnimationTicker next) {
            ticker.next = next;
            return this;
        }

        public Builder setLoopMode(Object loopMode) {
            ticker.loopMode = LoopMode.parse(loopMode);
            return this;
        }

        public Builder setToNullTransitionTime(int ticks) {
            ticker.toNullTransitionTime = ticks;
            return this;
        }

        public Builder setSpeed(float speed) {
            ticker.speedModifier = speed;
            return this;
        }

        public Builder startTime(float ticks) {
            ticker.elapsedTime = Mth.clamp(ticks, 0.0F, ticker.animation.getAnimTime());
            return this;
        }

        public Builder reversed() {
            ticker.reversed = true;
            return this;
        }

        public AnimationTicker build() {
            return ticker;
        }
    }
}
