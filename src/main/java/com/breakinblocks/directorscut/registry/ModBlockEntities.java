package com.breakinblocks.directorscut.registry;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.anchor.AnchorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DirectorsCut.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AnchorBlockEntity>> ANCHOR = BLOCK_ENTITIES.register("anchor",
        () -> BlockEntityType.Builder.of(AnchorBlockEntity::new, ModBlocks.ANCHOR.get()).build(null));
}
