package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.cutscene.CutsceneData;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LinearCameraMotion implements CameraMotion {
    @Override
    public Vec3 sample(CutsceneData data, float time) {
        List<CameraPos> frames = data.keyframes();
        if (frames.isEmpty()) {
            throw new IllegalStateException("Cutscene has no keyframes");
        }
        float p = CameraMotion.easedProgress(data, time);
        float global = p * (frames.size() - 1);
        int index = (int) global;
        float local = global - index;
        CameraPos current = frames.get(Math.max(0, Math.min(index, frames.size() - 1)));
        CameraPos next = frames.get(Math.max(0, Math.min(index + 1, frames.size() - 1)));
        return current.interpolate(next, local);
    }
}
