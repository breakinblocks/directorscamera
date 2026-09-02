package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.item.DirectorsCameraItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CameraItemActionPayload(int action) implements CustomPacketPayload {
    public static final int PREVIEW = 0;
    public static final int CLEAR = 1;
    public static final int DURATION_UP = 2;
    public static final int DURATION_DOWN = 3;

    public static final Type<CameraItemActionPayload> TYPE = new Type<>(DirectorsCamera.id("camera_item_action"));
    public static final StreamCodec<FriendlyByteBuf, CameraItemActionPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, CameraItemActionPayload::action,
        CameraItemActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CameraItemActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                DirectorsCameraItem.handleClientAction(player, payload.action);
            }
        });
    }
}
