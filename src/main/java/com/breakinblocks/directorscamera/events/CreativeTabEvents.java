package com.breakinblocks.directorscamera.events;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID)
public class CreativeTabEvents {
    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.DIRECTORS_CAMERA.get());
            event.accept(ModItems.ANCHOR.get());
        }
    }
}
