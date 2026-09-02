package com.breakinblocks.directorscut.registry;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.commands.CutsceneIdArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, DirectorsCut.MOD_ID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<CutsceneIdArgument>> CUTSCENE_ID = ARGUMENT_TYPES.register("cutscene_id",
        () -> ArgumentTypeInfos.registerByClass(CutsceneIdArgument.class, SingletonArgumentInfo.contextFree(CutsceneIdArgument::id)));
}
