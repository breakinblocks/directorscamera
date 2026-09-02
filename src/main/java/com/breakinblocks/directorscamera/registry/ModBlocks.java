package com.breakinblocks.directorscamera.registry;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.anchor.AnchorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DirectorsCamera.MOD_ID);

    public static final DeferredBlock<AnchorBlock> ANCHOR = BLOCKS.register("anchor",
        () -> new AnchorBlock(BlockBehaviour.Properties.of()
            .strength(-1.0F, 3600000.0F)
            .noLootTable()
            .noOcclusion()
            .noCollission()
            .pushReaction(PushReaction.BLOCK)
            .isValidSpawn((state, level, pos, type) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)));
}
