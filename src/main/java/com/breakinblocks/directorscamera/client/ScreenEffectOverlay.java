package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.cutscene.CutsceneScreenEffect;
import com.breakinblocks.directorscamera.cutscene.ScreenEffectType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.List;

public class ScreenEffectOverlay implements GuiLayer {
    public static final Identifier ID = DirectorsCamera.id("screen_effect");

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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
