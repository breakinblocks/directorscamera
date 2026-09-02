package com.breakinblocks.directorscamera.cutscene;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CameraPathBuilder {
    private static final double PANORAMA_YAW_STEP = 15.0;

    private final List<CameraPos> keyframes = new ArrayList<>();
    @Nullable
    private CutsceneDefinition owner;

    public CameraPathBuilder() {
    }

    public CameraPathBuilder(CutsceneDefinition owner) {
        this.owner = owner;
    }

    void setOwner(CutsceneDefinition owner) {
        this.owner = owner;
    }

    public List<CameraPos> getKeyframes() {
        return keyframes;
    }

    public int getKeyframeCount() {
        return keyframes.size();
    }

    public CameraPathBuilder clear() {
        keyframes.clear();
        return this;
    }

    public CameraPathBuilder addKeyframe(CameraPos pos) {
        keyframes.add(pos);
        return this;
    }

    public CameraPathBuilder addKeyframes(List<CameraPos> list) {
        keyframes.addAll(list);
        return this;
    }

    public CameraPathBuilder addPoint(double x, double y, double z) {
        return addPoint(x, y, z, 0, 0, 0);
    }

    public CameraPathBuilder addPoint(double x, double y, double z, double yaw) {
        return addPoint(x, y, z, yaw, 0, 0);
    }

    public CameraPathBuilder addPoint(double x, double y, double z, double yaw, double pitch) {
        return addPoint(x, y, z, yaw, pitch, 0);
    }

    public CameraPathBuilder addPoint(double x, double y, double z, double yaw, double pitch, double roll) {
        keyframes.add(CameraPos.of(new Vec3(x, y, z), (float) yaw, (float) pitch, (float) roll));
        return this;
    }

    public CameraPathBuilder addVec3(Vec3 position) {
        return addVec3(position, 0, 0, 0);
    }

    public CameraPathBuilder addVec3(Vec3 position, double yaw, double pitch) {
        return addVec3(position, yaw, pitch, 0);
    }

    public CameraPathBuilder addVec3(Vec3 position, double yaw, double pitch, double roll) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null");
        }
        keyframes.add(CameraPos.of(position, (float) yaw, (float) pitch, (float) roll));
        return this;
    }

    public CameraPathBuilder addEntity(Entity entity) {
        return addEntity(entity, 0, 0, 0);
    }

    public CameraPathBuilder addEntity(Entity entity, double offsetX, double offsetY, double offsetZ) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        Vec3 pos = new Vec3(entity.getX() + offsetX, entity.getEyeY() + offsetY, entity.getZ() + offsetZ);
        keyframes.add(CameraPos.of(pos, entity.getYRot(), entity.getXRot(), 0.0F));
        return this;
    }

    public CameraPathBuilder addLookingAt(double x, double y, double z, double targetX, double targetY, double targetZ) {
        return addLookingAt(x, y, z, targetX, targetY, targetZ, 0);
    }

    public CameraPathBuilder addLookingAt(double x, double y, double z, double targetX, double targetY, double targetZ, double roll) {
        keyframes.add(CameraPos.lookingAt(new Vec3(x, y, z), new Vec3(targetX, targetY, targetZ), (float) roll));
        return this;
    }

    public CameraPathBuilder addOrbit(double centerX, double centerY, double centerZ, double radius, double startAngle, double endAngle, int points) {
        return addOrbit(centerX, centerY, centerZ, radius, startAngle, endAngle, points, true);
    }

    public CameraPathBuilder addOrbit(double centerX, double centerY, double centerZ, double radius, double startAngle, double endAngle, int points, boolean lookAtCenter) {
        if (points < 2) {
            throw new IllegalArgumentException("Orbit must have at least 2 points");
        }
        double step = (endAngle - startAngle) / (points - 1);
        for (int i = 0; i < points; i++) {
            double angle = startAngle + step * i;
            double rad = Math.toRadians(angle);
            double x = centerX + radius * Math.cos(rad);
            double z = centerZ + radius * Math.sin(rad);
            if (lookAtCenter) {
                addLookingAt(x, centerY, z, centerX, centerY, centerZ, 0);
            } else {
                addPoint(x, centerY, z, angle + 90, 0, 0);
            }
        }
        return this;
    }

    public CameraPathBuilder addSpin(double x, double y, double z, double startYaw, double endYaw, double pitch, int points) {
        if (points < 2) {
            throw new IllegalArgumentException("Spin must have at least 2 points");
        }
        double step = (endYaw - startYaw) / (points - 1);
        for (int i = 0; i < points; i++) {
            addPoint(x, y, z, startYaw + step * i, pitch, 0);
        }
        return this;
    }

    public CameraPathBuilder addPanorama(double x, double y, double z, double turns) {
        return addPanorama(x, y, z, turns, 0, 0);
    }

    public CameraPathBuilder addPanorama(double x, double y, double z, double turns, double pitch) {
        return addPanorama(x, y, z, turns, pitch, 0);
    }

    public CameraPathBuilder addPanorama(double x, double y, double z, double turns, double pitch, double startYaw) {
        if (turns == 0) {
            throw new IllegalArgumentException("Panorama turns cannot be zero; use a negative value to spin the other way");
        }
        double sweep = turns * 360;
        int points = (int) Math.ceil(Math.abs(sweep) / PANORAMA_YAW_STEP) + 1;
        return addSpin(x, y, z, startYaw, startYaw + sweep, pitch, Math.max(3, points));
    }

    public CameraPathBuilder addArc(double startX, double startY, double startZ, double endX, double endY, double endZ, double height, int points) {
        if (points < 2) {
            throw new IllegalArgumentException("Arc must have at least 2 points");
        }
        double dx = endX - startX;
        double dz = endZ - startZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        for (int i = 0; i < points; i++) {
            double t = (double) i / (points - 1);
            double x = startX + dx * t;
            double y = startY + (endY - startY) * t + height * Math.sin(t * Math.PI);
            double z = startZ + dz * t;
            double pitch = 0;
            if (horizontal > 0) {
                double slope = height * Math.PI * Math.cos(t * Math.PI) / horizontal;
                pitch = -Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, slope))));
            }
            addPoint(x, y, z, yaw, pitch, 0);
        }
        return this;
    }

    public CameraPathBuilder addSpiral(double centerX, double startY, double centerZ, double startRadius, double endRadius, double height, double turns, int points) {
        if (points < 2) {
            throw new IllegalArgumentException("Spiral must have at least 2 points");
        }
        for (int i = 0; i < points; i++) {
            double t = (double) i / (points - 1);
            double angle = turns * 360 * t;
            double rad = Math.toRadians(angle);
            double radius = startRadius + (endRadius - startRadius) * t;
            addPoint(centerX + radius * Math.cos(rad), startY + height * t, centerZ + radius * Math.sin(rad), angle + 90, 0, 0);
        }
        return this;
    }

    public CameraPathBuilder execute(CutsceneCallback callback) {
        owner().addAction(CutsceneAction.atKeyframe(Math.max(keyframes.size() - 1, 0), callback, false));
        return this;
    }

    public CameraPathBuilder execute(CutsceneCallback callback, CutsceneDefinition cutscene) {
        adopt(cutscene);
        return execute(callback);
    }

    public CameraPathBuilder executeAt(int ticks, CutsceneCallback callback) {
        owner().executeAt(ticks, callback);
        return this;
    }

    public CameraPathBuilder executeAt(int ticks, CutsceneCallback callback, Map<String, Object> options) {
        owner().executeAt(ticks, callback, options);
        return this;
    }

    public CameraPathBuilder executeAt(int ticks, CutsceneCallback callback, CutsceneDefinition cutscene) {
        adopt(cutscene);
        return executeAt(ticks, callback);
    }

    public CameraPathBuilder executeAtSecond(double seconds, CutsceneCallback callback) {
        return executeAt((int) Math.floor(seconds * 20), callback);
    }

    public CameraPathBuilder executeAtSecond(double seconds, CutsceneCallback callback, Map<String, Object> options) {
        return executeAt((int) Math.floor(seconds * 20), callback, options);
    }

    public CameraPathBuilder executeAtSecond(double seconds, CutsceneCallback callback, CutsceneDefinition cutscene) {
        adopt(cutscene);
        return executeAtSecond(seconds, callback);
    }

    public CameraPathBuilder sound(int tick, String soundId) {
        owner().sound(tick, soundId);
        return this;
    }

    public CameraPathBuilder sound(int tick, String soundId, Map<String, Object> options) {
        owner().sound(tick, soundId, options);
        return this;
    }

    public CameraPathBuilder soundAtSecond(double seconds, String soundId) {
        owner().soundAtSecond(seconds, soundId);
        return this;
    }

    public CameraPathBuilder soundAtSecond(double seconds, String soundId, Map<String, Object> options) {
        owner().soundAtSecond(seconds, soundId, options);
        return this;
    }

    public CameraPathBuilder stopSound(int tick, String id) {
        owner().stopSound(tick, id);
        return this;
    }

    public CameraPathBuilder stopSoundAtSecond(double seconds, String id) {
        owner().stopSoundAtSecond(seconds, id);
        return this;
    }

    public double pathLength() {
        double total = 0;
        for (int i = 0; i < keyframes.size() - 1; i++) {
            total += keyframes.get(i).pos().distanceTo(keyframes.get(i + 1).pos());
        }
        return total;
    }

    private void adopt(CutsceneDefinition cutscene) {
        if (cutscene == null) {
            throw new IllegalArgumentException("Cutscene reference cannot be null");
        }
        if (owner == null) {
            owner = cutscene;
        }
    }

    private CutsceneDefinition owner() {
        if (owner == null) {
            throw new IllegalStateException("This path is not attached to a cutscene; create it with DirectorsCamera.cutscene().getPath()");
        }
        return owner;
    }
}
