package com.breakinblocks.directorscamera.client;

import com.breakinblocks.directorscamera.camera.ClientCameraEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class ClientCameraRenderer extends EntityRenderer<ClientCameraEntity, EntityRenderState> {
    public ClientCameraRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public boolean shouldRender(ClientCameraEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }
}
