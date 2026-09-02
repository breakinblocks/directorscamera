package com.breakinblocks.directorscut.cutscene;

import com.breakinblocks.directorscut.util.MathUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record CameraPos(Vec3 pos, float yaw, float pitch, float roll) {
    public static final Codec<CameraPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Vec3.CODEC.fieldOf("pos").forGetter(CameraPos::pos),
        Codec.FLOAT.optionalFieldOf("yaw", 0.0F).forGetter(CameraPos::yaw),
        Codec.FLOAT.optionalFieldOf("pitch", 0.0F).forGetter(CameraPos::pitch),
        Codec.FLOAT.optionalFieldOf("roll", 0.0F).forGetter(CameraPos::roll)
    ).apply(instance, CameraPos::of));

    public static final StreamCodec<FriendlyByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, v -> v.x,
        ByteBufCodecs.DOUBLE, v -> v.y,
        ByteBufCodecs.DOUBLE, v -> v.z,
        Vec3::new
    );

    public static final StreamCodec<FriendlyByteBuf, CameraPos> STREAM_CODEC = StreamCodec.composite(
        VEC3_STREAM_CODEC, CameraPos::pos,
        ByteBufCodecs.FLOAT, CameraPos::yaw,
        ByteBufCodecs.FLOAT, CameraPos::pitch,
        ByteBufCodecs.FLOAT, CameraPos::roll,
        CameraPos::new
    );

    public static CameraPos of(Vec3 pos, float yaw, float pitch, float roll) {
        return new CameraPos(pos, MathUtil.wrapYaw(yaw), Mth.clamp(pitch, -90.0F, 90.0F), roll);
    }

    public static CameraPos of(double x, double y, double z, float yaw, float pitch, float roll) {
        return of(new Vec3(x, y, z), yaw, pitch, roll);
    }

    public static CameraPos lookingAt(Vec3 pos, Vec3 target, float roll) {
        Vec3 dir = target.subtract(pos).normalize();
        return of(pos, MathUtil.yawFromDirection(dir), MathUtil.pitchFromDirection(dir), roll);
    }

    public static CameraPos lookingAlong(Vec3 pos, Vec3 direction, float roll) {
        Vec3 dir = direction.normalize();
        return of(pos, MathUtil.yawFromDirection(dir), MathUtil.pitchFromDirection(dir), roll);
    }

    public Vec3 interpolate(CameraPos next, double p) {
        return pos.lerp(next.pos, p);
    }

    public Vec3 lookDirection() {
        return MathUtil.directionFromAngles(yaw, pitch);
    }
}
