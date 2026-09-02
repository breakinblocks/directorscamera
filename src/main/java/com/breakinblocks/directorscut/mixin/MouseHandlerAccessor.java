package com.breakinblocks.directorscut.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("accumulatedDX")
    void directorscut$setAccumulatedDX(double value);

    @Accessor("accumulatedDY")
    void directorscut$setAccumulatedDY(double value);
}
