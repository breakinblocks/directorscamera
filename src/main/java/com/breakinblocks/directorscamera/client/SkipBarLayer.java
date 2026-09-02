package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.config.DirectorsCameraConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.gui.GuiLayer;

public class SkipBarLayer implements GuiLayer {
    public static final Identifier ID = DirectorsCamera.id("skip_bar");
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 6;
    private static final int MARGIN = 12;

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!CutsceneCameraHandler.isCutsceneActive() || !CutsceneCameraHandler.isSkippable() || minecraft.isPaused() || minecraft.screen != null) {
            return;
        }
        float partial = deltaTracker.getGameTimeDeltaPartialTick(false);
        boolean holding = CutsceneCameraHandler.isHolding();
        float alpha = holding ? 1.0F : CutsceneCameraHandler.hintAlpha(partial);
        if (!holding && !DirectorsCameraConfig.CLIENT.showSkipHint.get()) {
            return;
        }
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        if (a < 8) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int x0 = width - MARGIN - BAR_WIDTH;
        int y0 = height - MARGIN - BAR_HEIGHT;
        Font font = minecraft.font;
        Component text = Component.translatable("directorscamera.hud.hold_to_skip", minecraft.options.keyJump.getTranslatedKeyMessage());
        int textWidth = font.width(text);
        graphics.text(font, text, width - MARGIN - textWidth, y0 - 4 - font.lineHeight, withAlpha(0xFFFFFF, a), true);
        graphics.fill(x0, y0, x0 + BAR_WIDTH, y0 + BAR_HEIGHT, withAlpha(0x000000, a / 2));
        graphics.outline(x0, y0, BAR_WIDTH, BAR_HEIGHT, withAlpha(0xFFFFFF, a / 4));
        float progress = CutsceneCameraHandler.holdProgress(partial);
        int fill = (int) Math.floor((BAR_WIDTH - 2) * progress);
        if (fill > 0) {
            graphics.fill(x0 + 1, y0 + 1, x0 + 1 + fill, y0 + BAR_HEIGHT - 1, withAlpha(0xFFFFFF, a));
        }
    }

    private static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }
}
