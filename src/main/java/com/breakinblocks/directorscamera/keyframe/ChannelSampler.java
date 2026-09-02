package com.breakinblocks.directorscamera.keyframe;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;

public class ChannelSampler {
    @Nullable
    private final KeyframeList frames;

    public ChannelSampler(@Nullable List<KeyFrame> frames) {
        this.frames = frames == null || frames.isEmpty() ? null : new KeyframeList(frames);
    }

    public boolean isActive() {
        return frames != null && !frames.isEmpty();
    }

    @Nullable
    public KeyframeList getFrames() {
        return frames;
    }

    public Vector3f sample(AnimationContext context, float time) {
        if (frames == null || frames.isEmpty()) {
            return new Vector3f();
        }
        int floor = (int) Math.floor(time);
        KeyFrame[] pair = frames.neighbors(floor, 0, 1);
        KeyFrame current = pair[0];
        KeyFrame next = pair[1];
        if (current.mode() == InterpolationMode.LINEAR && (next == null || next.mode() == InterpolationMode.LINEAR)) {
            Vector3f v1 = time < current.time() ? current.getArrivalValue(context) : current.getPostValue(context);
            if (next == null) {
                return v1;
            }
            Vector3f v2 = next.getArrivalValue(context);
            float p = progress(time, current, next);
            return new Vector3f(v1).lerp(v2, p);
        }
        return catmullRom(context, time);
    }

    private Vector3f catmullRom(AnimationContext context, float time) {
        KeyFrame[] window = frames.neighbors((int) Math.floor(time), 1, 2);
        KeyFrame previous = window[0];
        KeyFrame current = window[1];
        KeyFrame next = window[2];
        KeyFrame next2 = window[3];
        Animation animation = context.getAnimation();
        boolean looping = context.getCurrentLoopMode() == LoopMode.LOOP && frames.size() > 2 && animation != null && !animation.isTransition();
        if (!looping) {
            if (next == null) {
                return current.getPostValue(context);
            }
            float p = progress(time, current, next);
            return catmull(previous == null ? null : previous.getPostValue(context), current.getPostValue(context),
                next.getPostValue(context), next2 == null ? null : next2.getPostValue(context), p);
        }
        int length = animation.getAnimTime();
        int state = 0;
        if (next2 == null) {
            if (next != null) {
                state = 1;
                next2 = frames.get(1);
            } else {
                state = 2;
                next2 = frames.get(2);
            }
        }
        if (next == null) {
            next = frames.get(1);
        }
        if (previous == null) {
            KeyFrame last = frames.getLast();
            previous = last.time() == length ? frames.get(frames.size() - 2) : last;
        }
        float p = state == 2 ? 0.0F : progress(time, current, next);
        return catmull(previous.getPostValue(context), current.getPostValue(context), next.getPostValue(context), next2.getPostValue(context), p);
    }

    private static float progress(float time, KeyFrame current, KeyFrame next) {
        int span = next.time() - current.time();
        if (span <= 0) {
            return time >= next.time() ? 1.0F : 0.0F;
        }
        return Mth.clamp((time - current.time()) / span, 0.0F, 1.0F);
    }

    public static Vector3f catmull(@Nullable Vector3f p0, Vector3f p1, @Nullable Vector3f p2, @Nullable Vector3f p3, float t) {
        if (p2 == null) {
            return new Vector3f(p1);
        }
        Vector3f before = p0 == null ? new Vector3f(p1).mul(2).sub(p2) : p0;
        Vector3f after = p3 == null ? new Vector3f(p2).mul(2).sub(p1) : p3;
        Vector3f a = new Vector3f(p2).sub(before).div(6.0F);
        Vector3f b = new Vector3f(p1).sub(after).div(6.0F);
        Vector3f c1 = new Vector3f(p1).add(a);
        Vector3f c2 = new Vector3f(p2).add(b);
        float u = 1.0F - t;
        float w0 = u * u * u;
        float w1 = 3.0F * t * u * u;
        float w2 = 3.0F * t * t * u;
        float w3 = t * t * t;
        return new Vector3f(p1).mul(w0).add(new Vector3f(c1).mul(w1)).add(new Vector3f(c2).mul(w2)).add(new Vector3f(p2).mul(w3));
    }
}
