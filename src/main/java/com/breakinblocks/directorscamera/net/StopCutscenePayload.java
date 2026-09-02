package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StopCutscenePayload() implements CustomPacketPayload {
    public static final Type<StopCutscenePayload> TYPE = new Type<>(DirectorsCamera.id("stop_cutscene"));
    public static final StreamCodec<FriendlyByteBuf, StopCutscenePayload> STREAM_CODEC = StreamCodec.unit(new StopCutscenePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
