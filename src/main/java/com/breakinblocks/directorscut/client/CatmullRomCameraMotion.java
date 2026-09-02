package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.cutscene.CutsceneData;
import com.breakinblocks.directorscut.util.MathUtil;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CatmullRomCameraMotion implements CameraMotion {
    @Override
    public Vec3 sample(CutsceneData data, float time) {
        List<CameraPos> frames = data.keyframes();
        if (frames.isEmpty()) {
            throw new IllegalStateException("Cutscene has no keyframes");
        }
        float p = CameraMotion.easedProgress(data, time);
        float global = p * (frames.size() - 1);
        int index = Math.min((int) global, frames.size() - 1);
        float local = global - index;
        return sampleSegment(frames, index, local);
    }

    public static Vec3 sampleSegment(List<CameraPos> frames, int index, double local) {
        Vec3 previous = index - 1 >= 0 ? frames.get(index - 1).pos() : null;
        Vec3 current = frames.get(Math.min(index, frames.size() - 1)).pos();
        Vec3 next = index + 1 < frames.size() ? frames.get(index + 1).pos() : null;
        Vec3 next2 = index + 2 < frames.size() ? frames.get(index + 2).pos() : null;
        return MathUtil.catmullRom(previous, current, next, next2, local);
    }
}
