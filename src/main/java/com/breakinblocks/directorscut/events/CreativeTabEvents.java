package com.breakinblocks.directorscut.events;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public class CreativeTabEvents {
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.DIRECTORS_CAMERA.get());
            event.accept(ModItems.ANCHOR.get());
        }
    }
}
