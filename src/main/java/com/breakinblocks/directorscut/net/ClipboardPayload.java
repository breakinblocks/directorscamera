package com.breakinblocks.directorscut.net;

import com.breakinblocks.directorscut.DirectorsCut;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClipboardPayload(String text) implements CustomPacketPayload {
    public static final Type<ClipboardPayload> TYPE = new Type<>(DirectorsCut.id("clipboard"));
    public static final StreamCodec<FriendlyByteBuf, ClipboardPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(262144), ClipboardPayload::text,
        ClipboardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
