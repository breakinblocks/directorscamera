package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.camera.ClientCameraEntity;
import com.breakinblocks.directorscamera.cutscene.CutsceneSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CutsceneSoundPlayer {
    private static final List<Tracked> TRACKED = new ArrayList<>();

    private CutsceneSoundPlayer() {
    }

    public static void fire(CutsceneSound entry, ClientCameraEntity camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (entry.stop()) {
            stop(entry.id());
            return;
        }
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(entry.sound())
            .map(Holder::value)
            .orElseGet(() -> SoundEvent.createVariableRangeEvent(entry.sound()));
        SoundInstance instance;
        if (entry.position().isEmpty()) {
            instance = new SimpleSoundInstance(event.location(), entry.category(), entry.volume(), entry.pitch(), SoundInstance.createUnseededRandom(),
                false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
        } else if (entry.attachToCamera()) {
            instance = new CameraFollowSoundInstance(event, entry.category(), entry.volume(), entry.pitch(), camera);
        } else {
            Vec3 pos = entry.position().get();
            instance = new SimpleSoundInstance(event.location(), entry.category(), entry.volume(), entry.pitch(), SoundInstance.createUnseededRandom(),
                false, 0, SoundInstance.Attenuation.LINEAR, pos.x, pos.y, pos.z, false);
        }
        if (entry.category() == SoundSource.MUSIC) {
            minecraft.getMusicManager().stopPlaying();
        }
        try {
            minecraft.getSoundManager().play(instance);
        } catch (Exception e) {
            DirectorsCamera.LOGGER.warn("Failed to play cutscene sound {}", entry.sound(), e);
            return;
        }
        TRACKED.add(new Tracked(entry.id(), entry.stopOnEnd(), instance));
    }

    public static void stop(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Iterator<Tracked> iterator = TRACKED.iterator();
        while (iterator.hasNext()) {
            Tracked tracked = iterator.next();
            if (tracked.id.equals(id)) {
                minecraft.getSoundManager().stop(tracked.instance);
                iterator.remove();
            }
        }
    }

    public static void endAll() {
        Minecraft minecraft = Minecraft.getInstance();
        for (Tracked tracked : TRACKED) {
            if (tracked.stopOnEnd) {
                minecraft.getSoundManager().stop(tracked.instance);
            }
        }
        TRACKED.clear();
    }

    private record Tracked(String id, boolean stopOnEnd, SoundInstance instance) {
    }
}
