package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.curves.CurveType;
import com.breakinblocks.directorscamera.cutscene.CameraPos;
import com.breakinblocks.directorscamera.item.CameraRecording;
import com.breakinblocks.directorscamera.item.DirectorsCameraItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

import java.util.List;

public final class CameraMarkerRenderer {
    private static final Identifier ICON = DirectorsCamera.id("textures/item/directors_camera.png");
    private static final int PATH_COLOR = 0xFFE0B040;
    private static final int LOOK_COLOR = 0xFF60A0FF;
    private static final int LABEL_COLOR = 0xFFFFFFFF;
    private static final int TARGET_COLOR = 0xFF60FF60;
    private static final int SAMPLES_PER_SEGMENT = 8;
    private static final float ICON_SIZE = 0.5F;
    private static final float LOOK_LENGTH = 0.6F;
    private static final float LINE_WIDTH = 1.0F;

    private CameraMarkerRenderer() {
    }

    public static void render(SubmitCustomGeometryEvent event, LocalPlayer player, CameraRecording recording) {
        List<CameraPos> frames = recording.worldKeyframes(player.level(), player.position());
        if (frames.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        CameraRenderState camera = event.getLevelRenderState().cameraRenderState;
        Vec3 cam = camera.pos;
        PoseStack poseStack = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        int targeted = DirectorsCameraItem.findTargetedKeyframe(player, frames);
        CurveType curve = recording.curve();

        poseStack.pushPose();
        poseStack.translate(-cam.x, -cam.y, -cam.z);
        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 0; i < frames.size() - 1; i++) {
                Vec3 previous = frames.get(i).pos();
                for (int s = 1; s <= SAMPLES_PER_SEGMENT; s++) {
                    double t = (double) s / SAMPLES_PER_SEGMENT;
                    Vec3 point = curve == CurveType.LINEAR
                        ? frames.get(i).pos().lerp(frames.get(i + 1).pos(), t)
                        : CatmullRomCameraMotion.sampleSegment(frames, i, t);
                    line(buffer, pose, previous, point, PATH_COLOR);
                    previous = point;
                }
            }
            for (CameraPos frame : frames) {
                line(buffer, pose, frame.pos(), frame.pos().add(frame.lookDirection().scale(LOOK_LENGTH)), LOOK_COLOR);
            }
        });
        poseStack.popPose();

        Font font = minecraft.font;
        for (int i = 0; i < frames.size(); i++) {
            CameraPos frame = frames.get(i);
            poseStack.pushPose();
            poseStack.translate(frame.pos().x - cam.x, frame.pos().y - cam.y, frame.pos().z - cam.z);
            poseStack.mulPose(camera.orientation);
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(ICON), CameraMarkerRenderer::icon);
            String text = String.valueOf(i + 1);
            poseStack.pushPose();
            poseStack.translate(0.0F, ICON_SIZE / 2.0F + 0.1F, 0.0F);
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            collector.submitText(poseStack, -font.width(text) / 2.0F, 0.0F, Component.literal(text).getVisualOrderText(),
                true, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, i == targeted ? TARGET_COLOR : LABEL_COLOR, 0x40000000, 0);
            poseStack.popPose();
            poseStack.popPose();
        }
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, Vec3 from, Vec3 to, int color) {
        Vec3 dir = to.subtract(from);
        double len = dir.length();
        float nx = len > 0 ? (float) (dir.x / len) : 0.0F;
        float ny = len > 0 ? (float) (dir.y / len) : 1.0F;
        float nz = len > 0 ? (float) (dir.z / len) : 0.0F;
        consumer.addVertex(pose, (float) from.x, (float) from.y, (float) from.z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(LINE_WIDTH);
        consumer.addVertex(pose, (float) to.x, (float) to.y, (float) to.z).setColor(color).setNormal(pose, nx, ny, nz).setLineWidth(LINE_WIDTH);
    }

    private static void icon(PoseStack.Pose pose, VertexConsumer consumer) {
        float h = ICON_SIZE / 2.0F;
        int light = LightCoordsUtil.FULL_BRIGHT;
        consumer.addVertex(pose, -h, -h, 0.0F).setColor(0xFFFFFFFF).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, h, -h, 0.0F).setColor(0xFFFFFFFF).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, h, h, 0.0F).setColor(0xFFFFFFFF).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, -h, h, 0.0F).setColor(0xFFFFFFFF).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
