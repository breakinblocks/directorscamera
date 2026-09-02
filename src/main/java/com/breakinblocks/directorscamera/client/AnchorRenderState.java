package com.breakinblocks.directorscamera.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class AnchorRenderState extends BlockEntityRenderState {
    public boolean visible;
    public Direction facing = Direction.SOUTH;
    public boolean hasTrigger;
    public double triggerRadius;
    public String label = "";
}
