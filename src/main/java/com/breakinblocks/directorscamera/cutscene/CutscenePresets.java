package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.curves.CurveType;
import com.breakinblocks.directorscamera.curves.EasingType;

public class CutscenePresets {
    public static final CutscenePresets INSTANCE = new CutscenePresets();

    public CutsceneDefinition orbit(double centerX, double centerY, double centerZ, double radius, double durationSeconds) {
        CutsceneDefinition cutscene = CutsceneDefinition.create()
            .setDurationSeconds(durationSeconds > 0 ? durationSeconds : 5)
            .setCurve(CurveType.CATMULLROM)
            .setEasing(EasingType.LINEAR);
        cutscene.getPath().addOrbit(centerX, centerY, centerZ, radius, 0, 360, 20, true);
        return cutscene;
    }

    public CutsceneDefinition orbit(double centerX, double centerY, double centerZ, double radius) {
        return orbit(centerX, centerY, centerZ, radius, 5);
    }

    public CutsceneDefinition flyby(double startX, double startY, double startZ, double endX, double endY, double endZ, double durationSeconds) {
        CutsceneDefinition cutscene = CutsceneDefinition.create()
            .setDurationSeconds(durationSeconds > 0 ? durationSeconds : 3)
            .setCurve(CurveType.CATMULLROM)
            .setEasing(EasingType.EASE_IN_OUT);
        double yaw = Math.toDegrees(Math.atan2(endZ - startZ, endX - startX)) - 90;
        cutscene.getPath()
            .addPoint(startX, startY, startZ, yaw, 0, 0)
            .addPoint(endX, endY, endZ, yaw, 0, 0);
        return cutscene;
    }

    public CutsceneDefinition flyby(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        return flyby(startX, startY, startZ, endX, endY, endZ, 3);
    }

    public CutsceneDefinition reveal(double x, double y, double z, double distance, double durationSeconds) {
        CutsceneDefinition cutscene = CutsceneDefinition.create()
            .setDurationSeconds(durationSeconds > 0 ? durationSeconds : 4)
            .setCurve(CurveType.CATMULLROM)
            .setTimeEasing(EasingType.EASE_OUT)
            .setLookEasing(EasingType.EASE_IN);
        cutscene.getPath()
            .addLookingAt(x - distance, y + distance * 0.5, z, x, y, z, 0)
            .addLookingAt(x - distance * 0.3, y + distance * 0.2, z, x, y, z, 0)
            .addLookingAt(x - distance * 0.1, y, z, x, y, z, 0);
        return cutscene;
    }

    public CutsceneDefinition reveal(double x, double y, double z, double distance) {
        return reveal(x, y, z, distance, 4);
    }

    public CutsceneDefinition pan(double x, double y, double z, double startYaw, double endYaw, double durationSeconds) {
        CutsceneDefinition cutscene = CutsceneDefinition.create()
            .setDurationSeconds(durationSeconds > 0 ? durationSeconds : 3)
            .setCurve(CurveType.LINEAR)
            .setEasing(EasingType.EASE_IN_OUT);
        cutscene.getPath()
            .addPoint(x, y, z, startYaw, 0, 0)
            .addPoint(x, y, z, endYaw, 0, 0);
        return cutscene;
    }

    public CutsceneDefinition pan(double x, double y, double z, double startYaw, double endYaw) {
        return pan(x, y, z, startYaw, endYaw, 3);
    }

    public CutsceneDefinition panorama(double x, double y, double z, double durationSeconds, double turns, double pitch) {
        CutsceneDefinition cutscene = CutsceneDefinition.create()
            .setDurationSeconds(durationSeconds > 0 ? durationSeconds : 12)
            .setCurve(CurveType.LINEAR)
            .setEasing(EasingType.LINEAR);
        cutscene.getPath().addPanorama(x, y, z, turns == 0 ? 1 : turns, pitch, 0);
        return cutscene;
    }

    public CutsceneDefinition panorama(double x, double y, double z, double durationSeconds, double turns) {
        return panorama(x, y, z, durationSeconds, turns, 0);
    }

    public CutsceneDefinition panorama(double x, double y, double z, double durationSeconds) {
        return panorama(x, y, z, durationSeconds, 1, 0);
    }

    public CutsceneDefinition panorama(double x, double y, double z) {
        return panorama(x, y, z, 12, 1, 0);
    }

    public CutsceneDefinition zoom(double startX, double startY, double startZ, double targetX, double targetY, double targetZ, double durationSeconds) {
        CutsceneDefinition cutscene = CutsceneDefinition.create()
            .setDurationSeconds(durationSeconds > 0 ? durationSeconds : 2)
            .setCurve(CurveType.CATMULLROM)
            .setEasing(EasingType.EASE_IN);
        cutscene.getPath()
            .addLookingAt(startX, startY, startZ, targetX, targetY, targetZ, 0)
            .addLookingAt(targetX, targetY, targetZ, targetX, targetY, targetZ, 0);
        return cutscene;
    }

    public CutsceneDefinition zoom(double startX, double startY, double startZ, double targetX, double targetY, double targetZ) {
        return zoom(startX, startY, startZ, targetX, targetY, targetZ, 2);
    }
}
