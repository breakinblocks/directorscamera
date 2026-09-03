package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.registry.ModBlockEntities;
import com.breakinblocks.directorscamera.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class DirectorsCameraClient {
    public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(DirectorsCamera.id("main"));
    public static final KeyMapping END_CUTSCENE = new KeyMapping("directorscamera.key.end_cutscene", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KEY_CATEGORY);

    public static void init(IEventBus eventBus) {
        eventBus.addListener(DirectorsCameraClient::registerKeyMappings);
        eventBus.addListener(DirectorsCameraClient::registerRenderers);
        eventBus.addListener(DirectorsCameraClient::registerGuiLayers);
        eventBus.addListener(DirectorsCameraClient::addReloadListeners);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(KEY_CATEGORY);
        event.register(END_CUTSCENE);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CLIENT_CAMERA.get(), ClientCameraRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ANCHOR.get(), AnchorRenderer::new);
    }

    private static void addReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(DirectorsCamera.id("screen_effects"), (ResourceManagerReloadListener) manager -> ChromaticAberrationEffect.reset());
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ScreenEffectOverlay.ID, new ScreenEffectOverlay());
        event.registerAboveAll(SkipBarLayer.ID, new SkipBarLayer());
    }

    public static boolean isKubeJsLoaded() {
        return DirectorsCamera.isModLoaded("kubejs");
    }
}
