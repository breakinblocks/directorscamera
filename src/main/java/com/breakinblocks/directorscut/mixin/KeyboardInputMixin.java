package com.breakinblocks.directorscut.mixin;

import com.breakinblocks.directorscut.client.CutsceneCameraHandler;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void directorscut$tick(boolean sneaking, float sneakMultiplier, CallbackInfo ci) {
        if (CutsceneCameraHandler.isCutsceneActive()) {
            CutsceneCameraHandler.nullifyInput((Input) (Object) this);
        }
    }
}
