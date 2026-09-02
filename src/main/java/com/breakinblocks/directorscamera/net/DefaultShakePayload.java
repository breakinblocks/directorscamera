package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.shake.ShakeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public record DefaultShakePayload(ShakeData data) implements CustomPacketPayload {
    public static final Type<DefaultShakePayload> TYPE = new Type<>(DirectorsCamera.id("default_shake"));
    public static final StreamCodec<FriendlyByteBuf, DefaultShakePayload> STREAM_CODEC = StreamCodec.composite(
        ShakeData.STREAM_CODEC, DefaultShakePayload::data,
        DefaultShakePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(ServerLevel level, Vec3 pos, double radius, ShakeData data) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius, new DefaultShakePayload(data));
    }
}
