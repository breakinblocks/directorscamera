package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.cutscene.CutsceneData;
import com.breakinblocks.directorscut.util.MathUtil;

import java.util.List;

public final class LookProcessor {
    private LookProcessor() {
    }

    public static float[] sample(CutsceneData data, float time) {
        List<CameraPos> frames = data.keyframes();
        float p = CameraMotion.easedProgress(data, time);
        float global = p * (frames.size() - 1);
        int index = (int) global;
        float local = data.lookEasing().apply(global - index);
        CameraPos current = frames.get(Math.max(0, Math.min(index, frames.size() - 1)));
        CameraPos next = frames.get(Math.max(0, Math.min(index + 1, frames.size() - 1)));
        float yaw = MathUtil.lerpAround(current.yaw(), next.yaw(), -180.0F, 180.0F, local);
        float pitch = MathUtil.lerp(current.pitch(), next.pitch(), local);
        float roll = MathUtil.lerp(current.roll(), next.roll(), local);
        return new float[]{yaw, pitch, roll};
    }
}
