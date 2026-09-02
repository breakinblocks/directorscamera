package com.breakinblocks.directorscut.registry;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.item.CameraRecording;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DirectorsCut.MOD_ID);

    public static final Supplier<DataComponentType<CameraRecording>> RECORDING = DATA_COMPONENT_TYPES.register("recording", () ->
        DataComponentType.<CameraRecording>builder()
            .persistent(CameraRecording.CODEC)
            .networkSynchronized(CameraRecording.STREAM_CODEC)
            .build());
}
