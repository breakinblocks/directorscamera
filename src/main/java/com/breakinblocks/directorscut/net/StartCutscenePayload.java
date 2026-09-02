package com.breakinblocks.directorscut.net;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.cutscene.CutsceneData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StartCutscenePayload(CutsceneData data) implements CustomPacketPayload {
    public static final Type<StartCutscenePayload> TYPE = new Type<>(DirectorsCut.id("cutscene_packet"));
    public static final StreamCodec<FriendlyByteBuf, StartCutscenePayload> STREAM_CODEC = StreamCodec.composite(
        CutsceneData.STREAM_CODEC, StartCutscenePayload::data,
        StartCutscenePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
