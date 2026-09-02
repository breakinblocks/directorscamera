package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.anchor.AnchorBlock;
import com.breakinblocks.directorscamera.anchor.AnchorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class AnchorRenderer implements BlockEntityRenderer<AnchorBlockEntity> {
    private static final int BOX_COLOR = 0xFF40E0D0;
    private static final int ARROW_COLOR = 0xFFFFC040;
    private static final int TRIGGER_COLOR = 0x60FF6060;

    public AnchorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AnchorBlockEntity anchor, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !AnchorBlock.canSee(player)) {
            return;
        }
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        LevelRenderer.renderLineBox(poseStack, lines, new AABB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9), red(BOX_COLOR), green(BOX_COLOR), blue(BOX_COLOR), 1.0F);
        Direction facing = anchor.getFacing();
        PoseStack.Pose pose = poseStack.last();
        float cx = 0.5F;
        float cz = 0.5F;
        float ex = cx + facing.getStepX() * 1.2F;
        float ez = cz + facing.getStepZ() * 1.2F;
        lines.addVertex(pose, cx, 0.5F, cz).setColor(ARROW_COLOR).setNormal(pose, facing.getStepX(), 0.0F, facing.getStepZ());
        lines.addVertex(pose, ex, 0.5F, ez).setColor(ARROW_COLOR).setNormal(pose, facing.getStepX(), 0.0F, facing.getStepZ());
        Direction left = facing.getCounterClockWise();
        float hx = ex - facing.getStepX() * 0.3F;
        float hz = ez - facing.getStepZ() * 0.3F;
        lines.addVertex(pose, ex, 0.5F, ez).setColor(ARROW_COLOR).setNormal(pose, 0.0F, 1.0F, 0.0F);
        lines.addVertex(pose, hx + left.getStepX() * 0.25F, 0.5F, hz + left.getStepZ() * 0.25F).setColor(ARROW_COLOR).setNormal(pose, 0.0F, 1.0F, 0.0F);
        lines.addVertex(pose, ex, 0.5F, ez).setColor(ARROW_COLOR).setNormal(pose, 0.0F, 1.0F, 0.0F);
        lines.addVertex(pose, hx - left.getStepX() * 0.25F, 0.5F, hz - left.getStepZ() * 0.25F).setColor(ARROW_COLOR).setNormal(pose, 0.0F, 1.0F, 0.0F);
        if (anchor.hasTrigger()) {
            double r = anchor.getTriggerRadius();
            LevelRenderer.renderLineBox(poseStack, lines, new AABB(0.5 - r, -r, 0.5 - r, 0.5 + r, r, 0.5 + r), red(TRIGGER_COLOR), green(TRIGGER_COLOR), blue(TRIGGER_COLOR), 0.4F);
        }
        String label = anchor.getAnchorId().isEmpty() ? "anchor" : anchor.getAnchorId();
        if (anchor.hasTrigger()) {
            label = label + " > " + anchor.getCutsceneId();
        }
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.3F, 0.5F);
        poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Font font = minecraft.font;
        Matrix4f matrix = poseStack.last().pose();
        font.drawInBatch(label, -font.width(label) / 2.0F, 0.0F, 0xFFFFFFFF, true, matrix, buffers, Font.DisplayMode.SEE_THROUGH, 0x40000000, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AnchorBlockEntity anchor) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public AABB getRenderBoundingBox(AnchorBlockEntity anchor) {
        return AABB.INFINITE;
    }

    private static float red(int color) {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    private static float green(int color) {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    private static float blue(int color) {
        return (color & 0xFF) / 255.0F;
    }
}
