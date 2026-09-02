package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.cutscene.CutsceneData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StartCutscenePayload(CutsceneData data) implements CustomPacketPayload {
    public static final Type<StartCutscenePayload> TYPE = new Type<>(DirectorsCamera.id("cutscene_packet"));
    public static final StreamCodec<FriendlyByteBuf, StartCutscenePayload> STREAM_CODEC = StreamCodec.composite(
        CutsceneData.STREAM_CODEC, StartCutscenePayload::data,
        StartCutscenePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
