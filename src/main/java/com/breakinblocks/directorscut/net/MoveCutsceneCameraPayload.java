package com.breakinblocks.directorscut.net;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.cutscene.CutsceneData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoveCutsceneCameraPayload(CutsceneData data) implements CustomPacketPayload {
    public static final Type<MoveCutsceneCameraPayload> TYPE = new Type<>(DirectorsCut.id("move_cutscene_camera"));
    public static final StreamCodec<FriendlyByteBuf, MoveCutsceneCameraPayload> STREAM_CODEC = StreamCodec.composite(
        CutsceneData.STREAM_CODEC, MoveCutsceneCameraPayload::data,
        MoveCutsceneCameraPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
