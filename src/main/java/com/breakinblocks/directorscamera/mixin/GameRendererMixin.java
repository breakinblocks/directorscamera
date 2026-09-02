package com.breakinblocks.directorscamera.mixin;

import com.breakinblocks.directorscamera.client.ShakeHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "bobHurt", at = @At("HEAD"))
    private void directorscamera$bobHurt(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        ShakeHandler.bobHurt(poseStack);
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void directorscamera$renderItemInHand(CameraRenderState cameraState, float deltaPartialTick, Matrix4fc modelViewMatrix, CallbackInfo ci) {
        ShakeHandler.beforeHand();
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void directorscamera$renderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        ShakeHandler.beforeLevel();
    }
}
