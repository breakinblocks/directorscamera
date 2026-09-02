package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.cutscene.CutsceneSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CutsceneStatePayload(int state, String id) implements CustomPacketPayload {
    public static final int STARTED = 0;
    public static final int FINISHED = 1;
    public static final int SKIPPED = 2;
    public static final int STOPPED = 3;

    public static final Type<CutsceneStatePayload> TYPE = new Type<>(DirectorsCamera.id("cutscene_state"));
    public static final StreamCodec<FriendlyByteBuf, CutsceneStatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, p -> (byte) p.state,
        ByteBufCodecs.STRING_UTF8, CutsceneStatePayload::id,
        (state, id) -> new CutsceneStatePayload(state, id)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CutsceneStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                CutsceneSessionManager.handleState(player, payload.state, payload.id);
            }
        });
    }
}
