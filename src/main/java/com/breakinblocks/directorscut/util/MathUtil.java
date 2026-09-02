package com.breakinblocks.directorscut.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class MathUtil {
    private MathUtil() {
    }

    public static float lerp(float a, float b, float p) {
        return a + (b - a) * p;
    }

    public static double lerp(double a, double b, double p) {
        return a + (b - a) * p;
    }

    public static float wrapYaw(float yaw) {
        float d = yaw % 360.0F;
        if (yaw > 0) {
            return d > 180.0F ? d - 360.0F : d;
        }
        return d < -180.0F ? d + 360.0F : d;
    }

    public static float yawFromDirection(Vec3 dir) {
        if (dir.x == 0 && dir.z == 0) {
            return 0.0F;
        }
        return (float) Math.toDegrees(-Math.atan2(dir.x, dir.z));
    }

    public static float pitchFromDirection(Vec3 dir) {
        double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        return (float) Math.toDegrees(-Math.atan2(dir.y, horizontal));
    }

    public static Vec3 directionFromAngles(float yaw, float pitch) {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double cosPitch = Math.cos(pitchRad);
        return new Vec3(-Math.sin(yawRad) * cosPitch, -Math.sin(pitchRad), Math.cos(yawRad) * cosPitch);
    }

    public static float lerpAround(float v1, float v2, float min, float max, float p) {
        if (v2 < v1) {
            float t = v1;
            v1 = v2;
            v2 = t;
            p = 1.0F - p;
        }
        float direct = v2 - v1;
        float around = (v1 - min) + (max - v2);
        if (direct < around) {
            return lerp(v1, v2, p);
        }
        float d = around * p;
        if (d < v1 - min) {
            return v1 - d;
        }
        return max - (d - (v1 - min));
    }

    public static double catmullRom(@Nullable Double previous, double current, @Nullable Double next, @Nullable Double next2, double p) {
        if (next == null) {
            return current;
        }
        double p0 = previous == null ? 2 * current - next : previous;
        double p3 = next2 == null ? 2 * next - current : next2;
        double h1 = current + (next - p0) / 6.0;
        double h2 = next + (current - p3) / 6.0;
        return bezier(current, h1, h2, next, p);
    }

    public static Vec3 catmullRom(@Nullable Vec3 previous, Vec3 current, @Nullable Vec3 next, @Nullable Vec3 next2, double p) {
        return new Vec3(
            catmullRom(previous == null ? null : previous.x, current.x, next == null ? null : next.x, next2 == null ? null : next2.x, p),
            catmullRom(previous == null ? null : previous.y, current.y, next == null ? null : next.y, next2 == null ? null : next2.y, p),
            catmullRom(previous == null ? null : previous.z, current.z, next == null ? null : next.z, next2 == null ? null : next2.z, p)
        );
    }

    public static double bezier(double p0, double p1, double p2, double p3, double t) {
        double u = 1 - t;
        return p0 * u * u * u + 3 * p1 * t * u * u + 3 * p2 * t * t * u + p3 * t * t * t;
    }

    public static Vec3 projectOntoPlane(Vec3 v, Vec3 normal) {
        double scale = v.dot(normal) / normal.dot(normal);
        return v.subtract(normal.scale(scale));
    }

    public static double angleBetween(Vec3 a, Vec3 b) {
        return Math.acos(a.dot(b) / (a.length() * b.length()));
    }

    public static float clamp01(float v) {
        return Mth.clamp(v, 0.0F, 1.0F);
    }
}
