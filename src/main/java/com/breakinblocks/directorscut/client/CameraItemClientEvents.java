package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.item.DirectorsCameraItem;
import com.breakinblocks.directorscut.net.CameraItemActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID, value = Dist.CLIENT)
public final class CameraItemClientEvents {
    private static long lastAttackTick = -1;

    private CameraItemClientEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity() instanceof LocalPlayer player && holdsCamera(player)) {
            sendAttack(player);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide() || !(event.getEntity() instanceof LocalPlayer player) || !holdsCamera(player)) {
            return;
        }
        event.setCanceled(true);
        if (event.getAction() == PlayerInteractEvent.LeftClickBlock.Action.START) {
            sendAttack(player);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || !player.isShiftKeyDown() || !holdsCamera(player)) {
            return;
        }
        double delta = event.getScrollDeltaY();
        if (delta == 0) {
            return;
        }
        event.setCanceled(true);
        PacketDistributor.sendToServer(new CameraItemActionPayload(delta > 0 ? CameraItemActionPayload.DURATION_UP : CameraItemActionPayload.DURATION_DOWN));
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || CutsceneCameraHandler.isCutsceneActive()) {
            return;
        }
        ItemStack stack = DirectorsCameraItem.heldCamera(player);
        if (stack == null) {
            return;
        }
        CameraMarkerRenderer.render(event, player, DirectorsCameraItem.getRecording(stack));
    }

    private static boolean holdsCamera(LocalPlayer player) {
        return player.getMainHandItem().getItem() instanceof DirectorsCameraItem;
    }

    private static void sendAttack(LocalPlayer player) {
        long now = player.level().getGameTime();
        if (now == lastAttackTick) {
            return;
        }
        lastAttackTick = now;
        int action = player.isShiftKeyDown() ? CameraItemActionPayload.CLEAR : CameraItemActionPayload.PREVIEW;
        PacketDistributor.sendToServer(new CameraItemActionPayload(action));
    }
}
