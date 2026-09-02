package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.shake.ScreenShake;
import com.breakinblocks.directorscamera.shake.ScreenShakeInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID, value = Dist.CLIENT)
public final class ShakeHandler {
    private static final List<ScreenShakeInstance> SHAKES = new ArrayList<>();
    private static boolean renderShake = true;

    private ShakeHandler() {
    }

    public static void addShake(ScreenShake shake) {
        SHAKES.add(new ScreenShakeInstance(shake));
    }

    public static void beforeLevel() {
        renderShake = true;
    }

    public static void beforeHand() {
        renderShake = false;
    }

    public static void bobHurt(PoseStack poseStack, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!renderShake || minecraft.level == null || minecraft.isPaused() || minecraft.player == null) {
            return;
        }
        for (ScreenShakeInstance instance : SHAKES) {
            instance.shake().process(poseStack, instance.currentTime(), partialTicks);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Iterator<ScreenShakeInstance> iterator = SHAKES.iterator();
        while (iterator.hasNext()) {
            ScreenShakeInstance instance = iterator.next();
            if (instance.hasEnded()) {
                iterator.remove();
            } else {
                instance.tick();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        SHAKES.clear();
    }
}
