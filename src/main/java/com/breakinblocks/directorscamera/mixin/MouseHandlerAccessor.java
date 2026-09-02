package com.breakinblocks.directorscamera.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Accessor("accumulatedDX")
    void directorscamera$setAccumulatedDX(double value);

    @Accessor("accumulatedDY")
    void directorscamera$setAccumulatedDY(double value);
}
