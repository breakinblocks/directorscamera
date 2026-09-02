package com.breakinblocks.directorscut.mixin;

import com.breakinblocks.directorscut.client.CutsceneCameraHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void directorscut$isControlledCamera(CallbackInfoReturnable<Boolean> cir) {
        if (CutsceneCameraHandler.isCutsceneActive()) {
            cir.setReturnValue(true);
        }
    }
}
