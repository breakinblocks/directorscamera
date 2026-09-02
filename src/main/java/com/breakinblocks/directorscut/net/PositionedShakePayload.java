package com.breakinblocks.directorscut.net;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.shake.ShakeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public record PositionedShakePayload(ShakeData data, Vec3 pos, double maxDistance) implements CustomPacketPayload {
    public static final Type<PositionedShakePayload> TYPE = new Type<>(DirectorsCut.id("position_screen_shake"));
    public static final StreamCodec<FriendlyByteBuf, PositionedShakePayload> STREAM_CODEC = StreamCodec.composite(
        ShakeData.STREAM_CODEC, PositionedShakePayload::data,
        CameraPos.VEC3_STREAM_CODEC, PositionedShakePayload::pos,
        ByteBufCodecs.DOUBLE, PositionedShakePayload::maxDistance,
        PositionedShakePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(ServerLevel level, ShakeData data, Vec3 pos, double radius) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, radius, new PositionedShakePayload(data, pos, radius));
    }
}
