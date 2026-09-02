package com.breakinblocks.directorscut.cutscene;

import com.breakinblocks.directorscut.util.MathUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CutsceneFrame(Vec3 origin, float yaw) {
    public static final CutsceneFrame IDENTITY = new CutsceneFrame(Vec3.ZERO, 0.0F);

    public static final Codec<CutsceneFrame> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Vec3.CODEC.fieldOf("origin").forGetter(CutsceneFrame::origin),
        Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(CutsceneFrame::yaw)
    ).apply(instance, CutsceneFrame::new));

    public static CutsceneFrame of(double x, double y, double z, double yaw) {
        return new CutsceneFrame(new Vec3(x, y, z), MathUtil.wrapYaw((float) yaw));
    }

    public Vec3 toWorld(Vec3 local) {
        double rad = Math.toRadians(yaw);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = local.x * cos - local.z * sin;
        double z = local.x * sin + local.z * cos;
        return new Vec3(origin.x + x, origin.y + local.y, origin.z + z);
    }

    public Vec3 toLocal(Vec3 world) {
        Vec3 d = world.subtract(origin);
        double rad = Math.toRadians(-yaw);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double x = d.x * cos - d.z * sin;
        double z = d.x * sin + d.z * cos;
        return new Vec3(x, d.y, z);
    }

    public float toWorldYaw(float localYaw) {
        return MathUtil.wrapYaw(localYaw + yaw);
    }

    public float toLocalYaw(float worldYaw) {
        return MathUtil.wrapYaw(worldYaw - yaw);
    }

    public CameraPos toWorld(CameraPos local) {
        return CameraPos.of(toWorld(local.pos()), toWorldYaw(local.yaw()), local.pitch(), local.roll());
    }

    public CameraPos toLocal(CameraPos world) {
        return CameraPos.of(toLocal(world.pos()), toLocalYaw(world.yaw()), world.pitch(), world.roll());
    }

    public CutsceneSound toWorld(CutsceneSound sound) {
        if (sound.position().isEmpty()) {
            return sound;
        }
        return sound.withPosition(toWorld(sound.position().get()));
    }

    public CutsceneData toWorld(CutsceneData data) {
        List<CameraPos> keyframes = new ArrayList<>(data.keyframes().size());
        for (CameraPos pos : data.keyframes()) {
            keyframes.add(toWorld(pos));
        }
        List<CutsceneSound> sounds = new ArrayList<>(data.sounds().size());
        for (CutsceneSound sound : data.sounds()) {
            sounds.add(toWorld(sound));
        }
        return new CutsceneData(List.copyOf(keyframes), data.duration(), data.curve(), data.timeEasing(), data.lookEasing(), data.stopMode(), data.skippable(), data.loop(), List.copyOf(sounds), data.id());
    }

    public List<CameraPos> toLocal(List<CameraPos> world) {
        List<CameraPos> result = new ArrayList<>(world.size());
        for (CameraPos pos : world) {
            result.add(toLocal(pos));
        }
        return result;
    }

    public Vec3 pos(double x, double y, double z) {
        return toWorld(new Vec3(x, y, z));
    }

    public static Optional<CutsceneFrame> optional(CutsceneFrame frame) {
        return Optional.ofNullable(frame);
    }
}
