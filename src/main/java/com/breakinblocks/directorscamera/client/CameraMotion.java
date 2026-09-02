package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.cutscene.CutsceneData;
import net.minecraft.world.phys.Vec3;

public interface CameraMotion {
    Vec3 sample(CutsceneData data, float time);

    static CameraMotion forData(CutsceneData data) {
        return switch (data.curve()) {
            case LINEAR -> new LinearCameraMotion();
            case CATMULLROM -> new CatmullRomCameraMotion();
        };
    }

    static float easedProgress(CutsceneData data, float time) {
        float p = data.duration() <= 0 ? 1.0F : Math.max(0.0F, Math.min(1.0F, time / data.duration()));
        return Math.max(0.0F, Math.min(1.0F, data.timeEasing().apply(p)));
    }
}
