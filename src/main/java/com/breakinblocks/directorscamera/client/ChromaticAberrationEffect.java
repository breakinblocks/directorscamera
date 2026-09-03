package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.cutscene.CutsceneScreenEffect;
import com.breakinblocks.directorscamera.cutscene.ScreenEffectType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.List;

public final class ChromaticAberrationEffect {
    private static final ResourceLocation CHAIN = DirectorsCamera.id("shaders/post/chromatic_aberration.json");

    private static PostChain chain;
    private static boolean failed;
    private static int width;
    private static int height;

    private ChromaticAberrationEffect() {
    }

    public static void process(float partialTick) {
        if (failed) {
            return;
        }
        CutsceneExecutor executor = CutsceneCameraHandler.getExecutor();
        if (executor == null) {
            return;
        }
        List<CutsceneScreenEffect> effects = executor.getData().screenEffects();
        if (effects.isEmpty()) {
            return;
        }
        float now = executor.effectTime(partialTick);
        float strength = 0.0F;
        for (CutsceneScreenEffect effect : effects) {
            if (effect.type() == ScreenEffectType.CHROMATIC) {
                strength = Math.max(strength, effect.strengthAt(now - effect.tick()));
            }
        }
        if (strength <= 0.0001F) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!ensureLoaded(minecraft)) {
            return;
        }
        chain.setUniform("Strength", strength);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        chain.process(partialTick);
        minecraft.getMainRenderTarget().bindWrite(true);
    }

    public static void reset() {
        if (chain != null) {
            chain.close();
            chain = null;
        }
        width = 0;
        height = 0;
        failed = false;
    }

    private static boolean ensureLoaded(Minecraft minecraft) {
        int targetWidth = minecraft.getWindow().getWidth();
        int targetHeight = minecraft.getWindow().getHeight();
        if (chain == null) {
            try {
                chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), minecraft.getMainRenderTarget(), CHAIN);
            } catch (IOException | RuntimeException e) {
                DirectorsCamera.LOGGER.error("Failed to load the chromatic aberration post chain, the effect is disabled", e);
                chain = null;
                failed = true;
                return false;
            }
            width = 0;
            height = 0;
        }
        if (targetWidth != width || targetHeight != height) {
            chain.resize(targetWidth, targetHeight);
            width = targetWidth;
            height = targetHeight;
        }
        return true;
    }
}
