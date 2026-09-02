package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.curves.CurveType;
import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.item.CameraRecording;
import com.breakinblocks.directorscut.item.DirectorsCameraItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

public final class CameraMarkerRenderer {
    private static final ResourceLocation ICON = DirectorsCut.id("textures/item/directors_camera.png");
    private static final int PATH_COLOR = 0xFFE0B040;
    private static final int LOOK_COLOR = 0xFF60A0FF;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int TARGET_COLOR = 0xFF60FF60;
    private static final int SAMPLES_PER_SEGMENT = 8;
    private static final float ICON_SIZE = 0.5F;
    private static final float LOOK_LENGTH = 0.6F;

    private CameraMarkerRenderer() {
    }

    public static void render(RenderLevelStageEvent event, LocalPlayer player, CameraRecording recording) {
        List<CameraPos> frames = recording.worldKeyframes(player.level(), player.position());
        if (frames.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = event.getCamera();
        Vec3 cam = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        int targeted = DirectorsCameraItem.findTargetedKeyframe(player, frames);

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        for (int i = 0; i < frames.size() - 1; i++) {
            Vec3 previous = frames.get(i).pos();
            for (int s = 1; s <= SAMPLES_PER_SEGMENT; s++) {
                double t = (double) s / SAMPLES_PER_SEGMENT;
                Vec3 point = recording.curve() == CurveType.LINEAR
                    ? frames.get(i).pos().lerp(frames.get(i + 1).pos(), t)
                    : CatmullRomCameraMotion.sampleSegment(frames, i, t);
                line(lines, poseStack, previous, point, PATH_COLOR);
                previous = point;
            }
        }
        for (CameraPos frame : frames) {
            line(lines, poseStack, frame.pos(), frame.pos().add(frame.lookDirection().scale(LOOK_LENGTH)), LOOK_COLOR);
        }
        poseStack.popPose();
        buffers.endBatch(RenderType.lines());

        for (int i = 0; i < frames.size(); i++) {
            CameraPos frame = frames.get(i);
            poseStack.pushPose();
            poseStack.translate(frame.pos().x - cam.x, frame.pos().y - cam.y, frame.pos().z - cam.z);
            poseStack.mulPose(camera.rotation());
            icon(buffers, poseStack);
            label(minecraft.font, buffers, poseStack, String.valueOf(i + 1), i == targeted ? TARGET_COLOR : LABEL_COLOR);
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    private static void line(VertexConsumer consumer, PoseStack poseStack, Vec3 from, Vec3 to, int color) {
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        float nx = len > 0 ? (float) (dir.x / len) : 0.0F;
        float ny = len > 0 ? (float) (dir.y / len) : 1.0F;
        float nz = len > 0 ? (float) (dir.z / len) : 0.0F;
        PoseStack.Pose pose = poseStack.last();
        consumer.addVertex(pose, (float) from.x, (float) from.y, (float) from.z).setColor(color).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, (float) to.x, (float) to.y, (float) to.z).setColor(color).setNormal(pose, nx, ny, nz);
    }

    private static void icon(MultiBufferSource buffers, PoseStack poseStack) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucent(ICON));
        PoseStack.Pose pose = poseStack.last();
        float h = ICON_SIZE / 2.0F;
        int light = LightTexture.FULL_BRIGHT;
        consumer.addVertex(pose, -h, -h, 0.0F).setColor(0xFFFFFFFF).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, h, -h, 0.0F).setColor(0xFFFFFFFF).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, h, h, 0.0F).setColor(0xFFFFFFFF).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, -h, h, 0.0F).setColor(0xFFFFFFFF).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private static void label(Font font, MultiBufferSource buffers, PoseStack poseStack, String text, int color) {
        poseStack.pushPose();
        poseStack.translate(0.0F, ICON_SIZE / 2.0F + 0.1F, 0.0F);
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();
        float x = -font.width(text) / 2.0F;
        font.drawInBatch(text, x, 0.0F, color, true, matrix, buffers, Font.DisplayMode.SEE_THROUGH, 0x40000000, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}
