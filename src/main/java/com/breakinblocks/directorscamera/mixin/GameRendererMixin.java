package com.breakinblocks.directorscamera.mixin;

import com.breakinblocks.directorscamera.client.ShakeHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void directorscamera$bobHurt(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        ShakeHandler.bobHurt(poseStack, partialTicks);
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void directorscamera$renderItemInHand(Camera camera, float partialTicks, Matrix4f projection, CallbackInfo ci) {
        ShakeHandler.beforeHand();
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void directorscamera$renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        ShakeHandler.beforeLevel();
    }
}
