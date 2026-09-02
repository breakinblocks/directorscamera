package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.camera.ClientCameraEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class CameraFollowSoundInstance extends AbstractTickableSoundInstance {
    private final ClientCameraEntity camera;

    public CameraFollowSoundInstance(SoundEvent event, SoundSource source, float volume, float pitch, ClientCameraEntity camera) {
        super(event, source, SoundInstance.createUnseededRandom());
        this.camera = camera;
        this.volume = volume;
        this.pitch = pitch;
        this.attenuation = Attenuation.LINEAR;
        this.x = camera.getX();
        this.y = camera.getY();
        this.z = camera.getZ();
    }

    @Override
    public void tick() {
        if (!CutsceneCameraHandler.isCutsceneActive()) {
            stop();
            return;
        }
        x = camera.getX();
        y = camera.getY();
        z = camera.getZ();
    }
}
