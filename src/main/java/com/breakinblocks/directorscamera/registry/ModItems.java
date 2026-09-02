package com.breakinblocks.directorscamera.registry;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.item.DirectorsCameraItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DirectorsCamera.MOD_ID);

    public static final DeferredItem<DirectorsCameraItem> DIRECTORS_CAMERA = ITEMS.registerItem("directors_camera",
        DirectorsCameraItem::new, properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));

    public static final DeferredItem<BlockItem> ANCHOR = ITEMS.registerSimpleBlockItem("anchor", ModBlocks.ANCHOR,
        properties -> properties.rarity(Rarity.UNCOMMON));
}
