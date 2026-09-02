package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.camera.ClientCameraEntity;
import com.breakinblocks.directorscamera.cutscene.CutsceneData;
import com.breakinblocks.directorscamera.cutscene.CutsceneSound;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CutsceneExecutor {
    private final CutsceneData data;
    private final CameraMotion motion;
    private final List<CutsceneSound> sounds;
    private int elapsed;
    private int nextSound;

    public CutsceneExecutor(CutsceneData data) {
        this.data = data;
        this.motion = CameraMotion.forData(data);
        this.sounds = data.sounds();
    }

    public CutsceneData getData() {
        return data;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void tick(ClientCameraEntity camera) {
        camera.rememberPreviousPosition();
        int sampleTick = sampleTick(elapsed);
        while (nextSound < sounds.size() && sounds.get(nextSound).tick() <= sampleTick) {
            CutsceneSoundPlayer.fire(sounds.get(nextSound), camera);
            nextSound++;
        }
        Vec3 pos = motion.sample(data, sampleTick);
        camera.setPos(pos.x, pos.y, pos.z);
        elapsed++;
    }

    public boolean hasEnded() {
        return !looping() && elapsed > data.duration() + 1;
    }

    public Vec3 currentPosition() {
        return motion.sample(data, sampleTick(elapsed));
    }

    public float[] rotation(float partialTick) {
        float time = Math.max(0.0F, elapsed - 2 + partialTick);
        return LookProcessor.sample(data, looping() ? time % data.duration() : Math.min(data.duration(), time));
    }

    private boolean looping() {
        return data.loop() && data.duration() > 0;
    }

    private int sampleTick(int tick) {
        return looping() ? Math.floorMod(tick, data.duration()) : Math.min(tick, data.duration());
    }
}
