package com.breakinblocks.directorscamera.registry;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.anchor.AnchorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DirectorsCamera.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnchorBlockEntity>> ANCHOR = BLOCK_ENTITIES.register("anchor",
        () -> new BlockEntityType<>(AnchorBlockEntity::new, ModBlocks.ANCHOR.get()));
}
