package com.breakinblocks.directorscamera.registry;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.item.DirectorsCameraItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DirectorsCamera.MOD_ID);

    public static final DeferredItem<DirectorsCameraItem> DIRECTORS_CAMERA = ITEMS.register("directors_camera",
        () -> new DirectorsCameraItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<BlockItem> ANCHOR = ITEMS.register("anchor",
        () -> new BlockItem(ModBlocks.ANCHOR.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
}
