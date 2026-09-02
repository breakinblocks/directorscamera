package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.net.ClipboardPayload;
import com.breakinblocks.directorscut.net.DefaultShakePayload;
import com.breakinblocks.directorscut.net.MoveCutsceneCameraPayload;
import com.breakinblocks.directorscut.net.PositionedShakePayload;
import com.breakinblocks.directorscut.net.StartCutscenePayload;
import com.breakinblocks.directorscut.net.StopCutscenePayload;
import com.breakinblocks.directorscut.shake.DefaultShake;
import com.breakinblocks.directorscut.shake.PositionedShake;
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
                minecraft.player.displayClientMessage(Component.translatable("directorscut.command.copied"), true);
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
