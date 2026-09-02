package com.breakinblocks.directorscamera.keyframe;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BoneAnimation {
    private final String boneName;
    private final ChannelSampler position;
    private final ChannelSampler rotation;
    private final ChannelSampler scale;

    public BoneAnimation(String boneName, @Nullable List<KeyFrame> positions, @Nullable List<KeyFrame> rotations, @Nullable List<KeyFrame> scales) {
        this.boneName = boneName;
        this.position = new ChannelSampler(positions);
        this.rotation = new ChannelSampler(rotations);
        this.scale = new ChannelSampler(scales);
    }

    public String getBoneName() {
        return boneName;
    }

    public ChannelSampler getPosition() {
        return position;
    }

    public ChannelSampler getRotation() {
        return rotation;
    }

    public ChannelSampler getScale() {
        return scale;
    }

    public void apply(AnimatedBone bone, AnimationContext context, float time) {
        if (position.isActive()) {
            Vector3f v = position.sample(context, time);
            bone.addPosition(v.x, v.y, v.z);
        }
        if (rotation.isActive()) {
            Vector3f v = rotation.sample(context, time);
            bone.addRotation(v.x, v.y, v.z);
        }
        if (scale.isActive()) {
            Vector3f v = scale.sample(context, time);
            bone.multiplyScale(v.x, v.y, v.z);
        }
    }

    public BoneAnimation createTransitionData(@Nullable Animation target, @Nullable BoneAnimation targetBone, AnimationContext context, int toNullTime, float elapsedTime, boolean reversed) {
        int targetLength = target == null ? 0 : target.getAnimTime();
        return new BoneAnimation(boneName,
            transitionChannel(position, targetBone == null ? null : targetBone.position, context, toNullTime, elapsedTime, reversed, targetLength, new Vector3f(0, 0, 0)),
            transitionChannel(rotation, targetBone == null ? null : targetBone.rotation, context, toNullTime, elapsedTime, reversed, targetLength, new Vector3f(0, 0, 0)),
            transitionChannel(scale, targetBone == null ? null : targetBone.scale, context, toNullTime, elapsedTime, reversed, targetLength, new Vector3f(1, 1, 1)));
    }

    @Nullable
    private static List<KeyFrame> transitionChannel(ChannelSampler source, @Nullable ChannelSampler targetChannel, AnimationContext context, int toNullTime, float elapsedTime, boolean reversed, int targetLength, Vector3f identity) {
        boolean targetActive = targetChannel != null && targetChannel.isActive();
        if (!source.isActive()) {
            return targetActive ? targetChannel.getFrames().asList() : null;
        }
        Vector3f current = source.sample(context, elapsedTime);
        if (targetActive) {
            List<KeyFrame> frames = targetChannel.getFrames().asList();
            if (!reversed) {
                KeyFrame first = frames.getFirst();
                KeyFrame frozen = KeyFrame.constant(current, 0, first.mode());
                if (first.time() != 0) {
                    frames.addFirst(frozen);
                } else {
                    frames.set(0, frozen);
                }
            } else {
                KeyFrame last = frames.getLast();
                KeyFrame frozen = KeyFrame.constant(current, targetLength, last.mode());
                if (last.time() != targetLength) {
                    frames.add(frozen);
                } else {
                    frames.set(frames.size() - 1, frozen);
                }
            }
            return frames;
        }
        List<KeyFrame> frames = new ArrayList<>(2);
        frames.add(KeyFrame.constant(current, 0, InterpolationMode.LINEAR));
        frames.add(KeyFrame.constant(identity, Math.max(1, toNullTime), InterpolationMode.LINEAR));
        return frames;
    }
}
