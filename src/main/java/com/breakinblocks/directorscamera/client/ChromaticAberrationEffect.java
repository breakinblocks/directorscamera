package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.cutscene.CutsceneScreenEffect;
import com.breakinblocks.directorscamera.cutscene.ScreenEffectType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class ChromaticAberrationEffect {
    private static final Identifier SHADER = DirectorsCamera.id("post/chromatic_aberration");
    private static final Identifier SCREEN_QUAD = Identifier.withDefaultNamespace("core/screenquad");
    private static final String CONFIG_BLOCK = "ChromaticConfig";
    private static final int CONFIG_SIZE = new Std140SizeCalculator().putVec4().get();

    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
        .withLocation(SHADER)
        .withVertexShader(SCREEN_QUAD)
        .withFragmentShader(SHADER)
        .withSampler("InSampler")
        .withUniform(CONFIG_BLOCK, UniformType.UNIFORM_BUFFER)
        .build();

    private static TextureTarget scene;
    private static MappableRingBuffer config;
    private static boolean failed;

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
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (main.getColorTexture() == null || main.getColorTextureView() == null) {
            return;
        }
        try {
            draw(main, strength);
        } catch (RuntimeException e) {
            DirectorsCamera.LOGGER.error("Chromatic aberration pass failed, the effect is disabled", e);
            reset();
            failed = true;
        }
    }

    public static void reset() {
        if (scene != null) {
            scene.destroyBuffers();
            scene = null;
        }
        if (config != null) {
            config.close();
            config = null;
        }
        failed = false;
    }

    private static void draw(RenderTarget main, float strength) {
        int width = main.width;
        int height = main.height;
        if (scene == null) {
            scene = new TextureTarget("Director's Camera aberration scene", width, height, false);
        } else if (scene.width != width || scene.height != height) {
            scene.resize(width, height);
        }
        if (config == null) {
            config = new MappableRingBuffer(
                () -> "Director's Camera aberration config",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                CONFIG_SIZE
            );
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(main.getColorTexture(), scene.getColorTexture(), 0, 0, 0, 0, 0, width, height);
        try (GpuBuffer.MappedView view = encoder.mapBuffer(config.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data()).putVec4(strength, 0.0F, 0.0F, 0.0F);
        }
        try (RenderPass pass = encoder.createRenderPass(
            () -> "Director's Camera chromatic aberration",
            main.getColorTextureView(),
            OptionalInt.empty(),
            null,
            OptionalDouble.empty()
        )) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform(CONFIG_BLOCK, config.currentBuffer());
            pass.bindTexture("InSampler", scene.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.draw(0, 3);
        }
        config.rotate();
    }
}
