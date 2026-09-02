package com.breakinblocks.directorscamera.keyframe;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.expression.ExpressionVector3;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Animation {
    public static final String TRANSITION = "transition";
    public static final String TO_NULL_TRANSITION = "transition_to_null";

    private Identifier name;
    private int animTime;
    private LoopMode defaultLoopMode = LoopMode.ONCE;
    private Map<String, BoneAnimation> bones = new LinkedHashMap<>();

    public Animation(Identifier name) {
        this.name = name;
    }

    public Animation(Identifier name, int animTime, LoopMode defaultLoopMode, Map<String, BoneAnimation> bones) {
        this.name = name;
        this.animTime = animTime;
        this.defaultLoopMode = defaultLoopMode;
        this.bones = new LinkedHashMap<>(bones);
    }

    public static Animation load(Identifier name, JsonObject json, boolean bedrockConventions) {
        Animation animation = new Animation(name);
        animation.loadFrom(json, bedrockConventions);
        return animation;
    }

    public void loadFrom(JsonObject json, boolean bedrockConventions) {
        LoopMode loop = LoopMode.ONCE;
        if (json.has("loop")) {
            String value = json.get("loop").getAsString();
            if (value.equals("true")) {
                loop = LoopMode.LOOP;
            } else if (value.equals("hold_on_last_frame")) {
                loop = LoopMode.HOLD_ON_LAST_FRAME;
            } else if (!value.equals("false")) {
                throw new IllegalArgumentException("Unknown loop mode in animation " + name + ": " + value);
            }
        }
        Map<String, BoneAnimation> loaded = new LinkedHashMap<>();
        int maxTick = 0;
        if (json.has("bones")) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("bones").entrySet()) {
                JsonObject bone = entry.getValue().getAsJsonObject();
                List<KeyFrame> positions = readChannel(bone.get("position"), bedrockConventions ? Channel.POSITION : Channel.PLAIN);
                List<KeyFrame> rotations = readChannel(bone.get("rotation"), bedrockConventions ? Channel.ROTATION : Channel.PLAIN);
                List<KeyFrame> scales = readChannel(bone.get("scale"), Channel.PLAIN);
                maxTick = Math.max(maxTick, lastTick(positions));
                maxTick = Math.max(maxTick, lastTick(rotations));
                maxTick = Math.max(maxTick, lastTick(scales));
                loaded.put(entry.getKey(), new BoneAnimation(entry.getKey(), positions, rotations, scales));
            }
        }
        int length = json.has("animation_length") ? Math.round(json.get("animation_length").getAsFloat() * 20.0F) : maxTick;
        this.animTime = length;
        this.defaultLoopMode = loop;
        this.bones = loaded;
    }

    private enum Channel {
        PLAIN,
        POSITION,
        ROTATION
    }

    private static int lastTick(@Nullable List<KeyFrame> frames) {
        return frames == null || frames.isEmpty() ? 0 : frames.stream().mapToInt(KeyFrame::time).max().orElse(0);
    }

    @Nullable
    private static List<KeyFrame> readChannel(@Nullable JsonElement element, Channel channel) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        List<KeyFrame> frames = new ArrayList<>();
        if (element.isJsonPrimitive()) {
            frames.add(new KeyFrame(null, uniform(element.getAsString()), 0, InterpolationMode.LINEAR));
            return frames;
        }
        if (element.isJsonArray()) {
            frames.add(new KeyFrame(null, vector(element.getAsJsonArray(), channel), 0, InterpolationMode.LINEAR));
            return frames;
        }
        JsonObject object = element.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            int tick = Math.round(Float.parseFloat(entry.getKey()) * 20.0F);
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                frames.add(new KeyFrame(null, uniform(value.getAsString()), tick, InterpolationMode.LINEAR));
            } else if (value.isJsonArray()) {
                frames.add(new KeyFrame(null, vector(value.getAsJsonArray(), channel), tick, InterpolationMode.LINEAR));
            } else {
                JsonObject frame = value.getAsJsonObject();
                ExpressionVector3 pre = frame.has("pre") ? vector(frame.getAsJsonArray("pre"), channel) : null;
                if (!frame.has("post")) {
                    throw new IllegalArgumentException("Keyframe at " + entry.getKey() + " has no post value");
                }
                ExpressionVector3 post = vector(frame.getAsJsonArray("post"), channel);
                InterpolationMode mode = InterpolationMode.LINEAR;
                if (frame.has("lerp_mode")) {
                    String lerp = frame.get("lerp_mode").getAsString();
                    if (lerp.equals("catmullrom")) {
                        mode = InterpolationMode.CATMULLROM;
                    } else if (!lerp.equals("linear")) {
                        throw new IllegalArgumentException("Unknown lerp mode: " + lerp);
                    }
                }
                frames.add(new KeyFrame(pre, post, tick, mode));
            }
        }
        return frames;
    }

    private static ExpressionVector3 uniform(String text) {
        return new ExpressionVector3(text, text, text);
    }

    private static ExpressionVector3 vector(JsonArray array, Channel channel) {
        if (array.size() < 3) {
            throw new IllegalArgumentException("Keyframe vector needs three components");
        }
        String x = array.get(0).getAsString();
        String y = array.get(1).getAsString();
        String z = array.get(2).getAsString();
        if (channel == Channel.POSITION) {
            x = "-(" + x + ")";
        } else if (channel == Channel.ROTATION) {
            x = "-(" + x + ")";
            y = "-(" + y + ")";
        }
        return new ExpressionVector3(x, y, z);
    }

    public void applyAnimation(AnimationContext context, AnimationTarget target, float elapsedTime) {
        try {
            for (BoneAnimation bone : bones.values()) {
                AnimatedBone animated = target.getBone(bone.getBoneName());
                if (animated != null) {
                    bone.apply(animated, context, elapsedTime);
                }
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Error while applying animation: " + name, e);
        }
    }

    public Animation createTransitionTo(AnimationContext context, @Nullable Animation next, float elapsedTime, int toNullTime, boolean nextReversed) {
        if (next != null) {
            Map<String, BoneAnimation> data = new LinkedHashMap<>(next.bones);
            for (BoneAnimation bone : bones.values()) {
                BoneAnimation targetBone = next.bones.get(bone.getBoneName());
                if (targetBone != null || toNullTime != 0) {
                    data.put(bone.getBoneName(), bone.createTransitionData(next, targetBone, context, toNullTime, elapsedTime, nextReversed));
                }
            }
            return new TransitionAnimation(DirectorsCamera.id(TRANSITION), next.animTime, next.defaultLoopMode, data, next);
        }
        Map<String, BoneAnimation> data = new LinkedHashMap<>();
        for (BoneAnimation bone : bones.values()) {
            data.put(bone.getBoneName(), bone.createTransitionData(null, null, context, toNullTime, elapsedTime, false));
        }
        return new Animation(DirectorsCamera.id(TO_NULL_TRANSITION), toNullTime, LoopMode.ONCE, data);
    }

    public boolean isTransition() {
        return name.getPath().equals(TRANSITION);
    }

    public boolean isToNullTransition() {
        return name.getPath().equals(TO_NULL_TRANSITION);
    }

    public Identifier getName() {
        return name;
    }

    public int getAnimTime() {
        return animTime;
    }

    public LoopMode getDefaultLoopMode() {
        return defaultLoopMode;
    }

    public Map<String, BoneAnimation> getBones() {
        return bones;
    }

    @Nullable
    public BoneAnimation getBone(String boneName) {
        return bones.get(boneName);
    }

    public Animation resolveTarget() {
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof TransitionAnimation transition) {
            return transition.getTransitionTo() == this;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(resolveTarget());
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
