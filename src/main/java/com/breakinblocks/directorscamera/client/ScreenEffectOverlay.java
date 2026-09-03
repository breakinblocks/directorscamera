package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.cutscene.CutsceneScreenEffect;
import com.breakinblocks.directorscamera.cutscene.ScreenEffectType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ScreenEffectOverlay implements LayeredDraw.Layer {
    public static final ResourceLocation ID = DirectorsCamera.id("screen_effect");

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!CutsceneCameraHandler.isCutsceneActive()) {
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
        Minecraft minecraft = Minecraft.getInstance();
        float partial = minecraft.isPaused() ? 0.0F : deltaTracker.getGameTimeDeltaPartialTick(false);
        float now = executor.effectTime(partial);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        for (CutsceneScreenEffect effect : effects) {
            if (effect.type() != ScreenEffectType.COLOR) {
                continue;
            }
            float alpha = effect.alphaAt(now - effect.tick());
            if (alpha <= 0.0F) {
                continue;
            }
            graphics.fill(0, 0, width, height, effect.packedColor(alpha));
        }
    }
}
