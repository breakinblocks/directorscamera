package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.anchor.AnchorBlock;
import com.breakinblocks.directorscamera.anchor.AnchorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class AnchorRenderer implements BlockEntityRenderer<AnchorBlockEntity, AnchorRenderState> {
    private static final int BOX_COLOR = 0xFF40E0D0;
    private static final int ARROW_COLOR = 0xFFFFC040;
    private static final int TRIGGER_COLOR = 0x60FF6060;
    private static final float LINE_WIDTH = 1.0F;

    private final Font font;

    public AnchorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
    }

    @Override
    public AnchorRenderState createRenderState() {
        return new AnchorRenderState();
    }

    @Override
    public void extractRenderState(AnchorBlockEntity anchor, AnchorRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(anchor, state, partialTicks, cameraPosition, breakProgress);
        LocalPlayer player = Minecraft.getInstance().player;
        state.visible = player != null && AnchorBlock.canSee(player);
        state.facing = anchor.getFacing();
        state.hasTrigger = anchor.hasTrigger();
        state.triggerRadius = anchor.getTriggerRadius();
        String label = anchor.getAnchorId().isEmpty() ? "anchor" : anchor.getAnchorId();
        state.label = state.hasTrigger ? label + " > " + anchor.getCutsceneId() : label;
    }

    @Override
    public void submit(AnchorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible) {
            return;
        }
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            box(buffer, pose, new AABB(0.1, 0.1, 0.1, 0.9, 0.9, 0.9), BOX_COLOR);
            Direction facing = state.facing;
            float cx = 0.5F;
            float cz = 0.5F;
            float ex = cx + facing.getStepX() * 1.2F;
            float ez = cz + facing.getStepZ() * 1.2F;
            line(buffer, pose, cx, 0.5F, cz, ex, 0.5F, ez, ARROW_COLOR);
            Direction left = facing.getCounterClockWise();
            float hx = ex - facing.getStepX() * 0.3F;
            float hz = ez - facing.getStepZ() * 0.3F;
            line(buffer, pose, ex, 0.5F, ez, hx + left.getStepX() * 0.25F, 0.5F, hz + left.getStepZ() * 0.25F, ARROW_COLOR);
            line(buffer, pose, ex, 0.5F, ez, hx - left.getStepX() * 0.25F, 0.5F, hz - left.getStepZ() * 0.25F, ARROW_COLOR);
            if (state.hasTrigger) {
                double r = state.triggerRadius;
                box(buffer, pose, new AABB(0.5 - r, -r, 0.5 - r, 0.5 + r, r, 0.5 + r), TRIGGER_COLOR);
            }
        });
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.3F, 0.5F);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        collector.submitText(poseStack, -font.width(state.label) / 2.0F, 0.0F, Component.literal(state.label).getVisualOrderText(),
            true, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, 0xFFFFFFFF, 0x40000000, 0);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
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

    private static void box(VertexConsumer buffer, PoseStack.Pose pose, AABB box, int color) {
        float x0 = (float) box.minX;
        float y0 = (float) box.minY;
        float z0 = (float) box.minZ;
        float x1 = (float) box.maxX;
        float y1 = (float) box.maxY;
        float z1 = (float) box.maxZ;
        line(buffer, pose, x0, y0, z0, x1, y0, z0, color);
        line(buffer, pose, x1, y0, z0, x1, y0, z1, color);
        line(buffer, pose, x1, y0, z1, x0, y0, z1, color);
        line(buffer, pose, x0, y0, z1, x0, y0, z0, color);
        line(buffer, pose, x0, y1, z0, x1, y1, z0, color);
        line(buffer, pose, x1, y1, z0, x1, y1, z1, color);
        line(buffer, pose, x1, y1, z1, x0, y1, z1, color);
        line(buffer, pose, x0, y1, z1, x0, y1, z0, color);
        line(buffer, pose, x0, y0, z0, x0, y1, z0, color);
        line(buffer, pose, x1, y0, z0, x1, y1, z0, color);
        line(buffer, pose, x1, y0, z1, x1, y1, z1, color);
        line(buffer, pose, x0, y0, z1, x0, y1, z1, color);
    }

    private static void line(VertexConsumer buffer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = length > 0 ? dx / length : 0.0F;
        float ny = length > 0 ? dy / length : 1.0F;
        float nz = length > 0 ? dz / length : 0.0F;
        buffer.addVertex(pose, x0, y0, z0).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(LINE_WIDTH);
        buffer.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(LINE_WIDTH);
    }
}
