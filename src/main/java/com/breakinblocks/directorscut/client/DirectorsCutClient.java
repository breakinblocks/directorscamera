package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.registry.ModBlockEntities;
import com.breakinblocks.directorscut.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class DirectorsCutClient {
    public static final String KEY_CATEGORY = "directorscut.key_category";
    public static final KeyMapping END_CUTSCENE = new KeyMapping("directorscut.key.end_cutscene", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KEY_CATEGORY);

    public static void init(IEventBus eventBus) {
        eventBus.addListener(DirectorsCutClient::registerKeyMappings);
        eventBus.addListener(DirectorsCutClient::registerRenderers);
        eventBus.addListener(DirectorsCutClient::registerGuiLayers);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(END_CUTSCENE);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CLIENT_CAMERA.get(), ClientCameraRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ANCHOR.get(), AnchorRenderer::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(SkipBarLayer.ID, new SkipBarLayer());
    }

    public static boolean isKubeJsLoaded() {
        return DirectorsCut.isModLoaded("kubejs");
    }
}
