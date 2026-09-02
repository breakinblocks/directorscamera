package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.camera.ClientCameraEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class ClientCameraRenderer extends EntityRenderer<ClientCameraEntity> {
    public ClientCameraRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ClientCameraEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
    }

    @Override
    public boolean shouldRender(ClientCameraEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(ClientCameraEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
