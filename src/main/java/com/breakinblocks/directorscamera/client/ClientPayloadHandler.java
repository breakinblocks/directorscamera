package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.net.ClipboardPayload;
import com.breakinblocks.directorscamera.net.DefaultShakePayload;
import com.breakinblocks.directorscamera.net.MoveCutsceneCameraPayload;
import com.breakinblocks.directorscamera.net.PositionedShakePayload;
import com.breakinblocks.directorscamera.net.StartCutscenePayload;
import com.breakinblocks.directorscamera.net.StopCutscenePayload;
import com.breakinblocks.directorscamera.shake.DefaultShake;
import com.breakinblocks.directorscamera.shake.PositionedShake;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleStart(StartCutscenePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CutsceneCameraHandler.startCutscene(payload.data()));
    }

    public static void handleMove(MoveCutsceneCameraPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CutsceneCameraHandler.moveCamera(payload.data()));
    }

    public static void handleStop(StopCutscenePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CutsceneCameraHandler.stopCutscene(CutsceneCameraHandler.StopReason.SERVER));
    }

    public static void handleClipboard(ClipboardPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.keyboardHandler.setClipboard(payload.text());
            if (minecraft.player != null) {
                minecraft.player.sendOverlayMessage(Component.translatable("directorscamera.command.copied"));
            }
        });
    }

    public static void handleDefaultShake(DefaultShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ShakeHandler.addShake(new DefaultShake(payload.data())));
    }

    public static void handlePositionedShake(PositionedShakePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ShakeHandler.addShake(new PositionedShake(payload.data(), payload.pos(), payload.maxDistance())));
    }
}
