package com.breakinblocks.directorscut.keyframe;

import com.breakinblocks.directorscut.cutscene.CameraPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SimplePose implements AnimationTarget {
    private final Map<String, BoneTransform> bones = new LinkedHashMap<>();
    private final boolean createMissing;

    public SimplePose() {
        this(true);
    }

    public SimplePose(boolean createMissing) {
        this.createMissing = createMissing;
    }

    public SimplePose(String... boneNames) {
        this(false);
        for (String name : boneNames) {
            bones.put(name, new BoneTransform());
        }
    }

    @Override
    public void resetPose() {
        for (BoneTransform bone : bones.values()) {
            bone.reset();
        }
    }

    @Override
    public BoneTransform getBone(String name) {
        BoneTransform bone = bones.get(name);
        if (bone == null && createMissing) {
            bone = new BoneTransform();
            bones.put(name, bone);
        }
        return bone;
    }

    public Set<String> boneNames() {
        return bones.keySet();
    }

    public Vector3f position(String bone) {
        BoneTransform t = getBone(bone);
        return t == null ? new Vector3f() : new Vector3f(t.position);
    }

    public Vector3f rotation(String bone) {
        BoneTransform t = getBone(bone);
        return t == null ? new Vector3f() : new Vector3f(t.rotation);
    }

    public Vector3f scale(String bone) {
        BoneTransform t = getBone(bone);
        return t == null ? new Vector3f(1, 1, 1) : new Vector3f(t.scale);
    }

    public CameraPos cameraPos(String bone, Vec3 origin) {
        BoneTransform t = getBone(bone);
        if (t == null) {
            return CameraPos.of(origin, 0.0F, 0.0F, 0.0F);
        }
        Vec3 pos = origin.add(t.position.x, t.position.y, t.position.z);
        return CameraPos.of(pos, t.rotation.y, t.rotation.x, t.rotation.z);
    }

    public static class BoneTransform implements AnimatedBone {
        public final Vector3f position = new Vector3f();
        public final Vector3f rotation = new Vector3f();
        public final Vector3f scale = new Vector3f(1, 1, 1);

        public void reset() {
            position.set(0, 0, 0);
            rotation.set(0, 0, 0);
            scale.set(1, 1, 1);
        }

        @Override
        public void addPosition(float x, float y, float z) {
            position.add(x, y, z);
        }

        @Override
        public void addRotation(float x, float y, float z) {
            rotation.add(x, y, z);
        }

        @Override
        public void multiplyScale(float x, float y, float z) {
            scale.mul(x, y, z);
        }
    }
}
