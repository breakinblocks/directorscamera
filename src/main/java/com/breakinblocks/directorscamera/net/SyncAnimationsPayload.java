package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.keyframe.Animation;
import com.breakinblocks.directorscamera.keyframe.AnimationRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record SyncAnimationsPayload(Map<ResourceLocation, String> animations) implements CustomPacketPayload {
    public static final Type<SyncAnimationsPayload> TYPE = new Type<>(DirectorsCamera.id("sync_animations"));
    public static final StreamCodec<FriendlyByteBuf, SyncAnimationsPayload> STREAM_CODEC = StreamCodec.of(SyncAnimationsPayload::write, SyncAnimationsPayload::read);

    public static SyncAnimationsPayload current() {
        return new SyncAnimationsPayload(new LinkedHashMap<>(AnimationRegistry.dataJson()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAnimationsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Map<ResourceLocation, Animation> parsed = new LinkedHashMap<>();
            payload.animations.forEach((id, json) -> {
                try {
                    JsonObject object = JsonParser.parseString(json).getAsJsonObject();
                    parsed.put(id, Animation.load(id, object, GsonHelper.getAsBoolean(object, "bedrock_conventions", true)));
                } catch (Exception e) {
                    DirectorsCamera.LOGGER.error("Failed to parse synced animation {}", id, e);
                }
            });
            AnimationRegistry.setDataAnimations(parsed, payload.animations);
        });
    }

    private static void write(FriendlyByteBuf buf, SyncAnimationsPayload payload) {
        buf.writeVarInt(payload.animations.size());
        payload.animations.forEach((id, json) -> {
            buf.writeResourceLocation(id);
            buf.writeUtf(json, 1 << 20);
        });
    }

    private static SyncAnimationsPayload read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Map<ResourceLocation, String> map = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation id = buf.readResourceLocation();
            map.put(id, buf.readUtf(1 << 20));
        }
        return new SyncAnimationsPayload(map);
    }
}
