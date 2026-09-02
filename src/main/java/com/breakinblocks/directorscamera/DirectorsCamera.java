package com.breakinblocks.directorscamera;

import com.breakinblocks.directorscamera.client.DirectorsCameraClient;
import com.breakinblocks.directorscamera.config.DirectorsCameraConfig;
import com.breakinblocks.directorscamera.registry.ModArgumentTypes;
import com.breakinblocks.directorscamera.registry.ModBlockEntities;
import com.breakinblocks.directorscamera.registry.ModBlocks;
import com.breakinblocks.directorscamera.registry.ModDataComponents;
import com.breakinblocks.directorscamera.registry.ModEntities;
import com.breakinblocks.directorscamera.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

@Mod(DirectorsCamera.MOD_ID)
public class DirectorsCamera {
    public static final String MOD_ID = "directorscamera";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public DirectorsCamera(IEventBus eventBus, ModContainer container, Dist dist) {
        ModBlocks.BLOCKS.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(eventBus);
        ModEntities.ENTITY_TYPES.register(eventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(eventBus);
        ModArgumentTypes.ARGUMENT_TYPES.register(eventBus);
        container.registerConfig(ModConfig.Type.SERVER, DirectorsCameraConfig.serverSpec);
        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, DirectorsCameraConfig.clientSpec);
            container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            DirectorsCameraClient.init(eventBus);
        }
    }
}
