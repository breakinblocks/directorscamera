package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.curves.CurveType;
import com.breakinblocks.directorscamera.curves.EasingType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record CutsceneData(
    List<CameraPos> keyframes,
    int duration,
    CurveType curve,
    EasingType timeEasing,
    EasingType lookEasing,
    StopMode stopMode,
    boolean skippable,
    boolean loop,
    List<CutsceneScreenEffect> screenEffects,
    List<CutsceneSound> sounds,
    String id
) {
    public static final Codec<CutsceneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CameraPos.CODEC.listOf().fieldOf("keyframes").forGetter(CutsceneData::keyframes),
        Codec.INT.optionalFieldOf("duration", 100).forGetter(CutsceneData::duration),
        CurveType.CODEC.optionalFieldOf("curve", CurveType.CATMULLROM).forGetter(CutsceneData::curve),
        EasingType.CODEC.optionalFieldOf("timeEasing", EasingType.EASE_IN_OUT).forGetter(CutsceneData::timeEasing),
        EasingType.CODEC.optionalFieldOf("lookEasing", EasingType.EASE_IN_OUT).forGetter(CutsceneData::lookEasing),
        StopMode.CODEC.optionalFieldOf("stopMode", StopMode.AUTOMATIC).forGetter(CutsceneData::stopMode),
        Codec.BOOL.optionalFieldOf("skippable", true).forGetter(CutsceneData::skippable),
        Codec.BOOL.optionalFieldOf("loop", false).forGetter(CutsceneData::loop),
        CutsceneScreenEffect.CODEC.listOf().optionalFieldOf("screenEffects", List.of()).forGetter(CutsceneData::screenEffects),
        CutsceneSound.CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(CutsceneData::sounds),
        Codec.STRING.optionalFieldOf("id", "").forGetter(CutsceneData::id)
    ).apply(instance, CutsceneData::new));

    public static final StreamCodec<FriendlyByteBuf, CutsceneData> STREAM_CODEC = StreamCodec.of(CutsceneData::write, CutsceneData::read);

    public boolean isSkippable() {
        return skippable && stopMode != StopMode.UNSTOPPABLE;
    }

    public CutsceneData withKeyframes(List<CameraPos> newKeyframes) {
        return new CutsceneData(List.copyOf(newKeyframes), duration, curve, timeEasing, lookEasing, stopMode, skippable, loop, screenEffects, sounds, id);
    }

    public CutsceneData withId(String newId) {
        return new CutsceneData(keyframes, duration, curve, timeEasing, lookEasing, stopMode, skippable, loop, screenEffects, sounds, newId == null ? "" : newId);
    }

    private static void write(FriendlyByteBuf buf, CutsceneData data) {
        buf.writeInt(data.keyframes.size());
        for (CameraPos pos : data.keyframes) {
            CameraPos.STREAM_CODEC.encode(buf, pos);
        }
        buf.writeInt(data.duration);
        buf.writeEnum(data.curve);
        buf.writeEnum(data.timeEasing);
        buf.writeEnum(data.lookEasing);
        buf.writeEnum(data.stopMode);
        buf.writeBoolean(data.skippable);
        buf.writeBoolean(data.loop);
        buf.writeInt(data.screenEffects.size());
        for (CutsceneScreenEffect effect : data.screenEffects) {
            CutsceneScreenEffect.STREAM_CODEC.encode(buf, effect);
        }
        buf.writeInt(data.sounds.size());
        for (CutsceneSound sound : data.sounds) {
            CutsceneSound.STREAM_CODEC.encode(buf, sound);
        }
        buf.writeUtf(data.id);
    }

    private static CutsceneData read(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<CameraPos> keyframes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keyframes.add(CameraPos.STREAM_CODEC.decode(buf));
        }
        int duration = buf.readInt();
        CurveType curve = buf.readEnum(CurveType.class);
        EasingType timeEasing = buf.readEnum(EasingType.class);
        EasingType lookEasing = buf.readEnum(EasingType.class);
        StopMode stopMode = buf.readEnum(StopMode.class);
        boolean skippable = buf.readBoolean();
        boolean loop = buf.readBoolean();
        int effectCount = buf.readInt();
        List<CutsceneScreenEffect> screenEffects = new ArrayList<>(effectCount);
        for (int i = 0; i < effectCount; i++) {
            screenEffects.add(CutsceneScreenEffect.STREAM_CODEC.decode(buf));
        }
        int soundCount = buf.readInt();
        List<CutsceneSound> sounds = new ArrayList<>(soundCount);
        for (int i = 0; i < soundCount; i++) {
            sounds.add(CutsceneSound.STREAM_CODEC.decode(buf));
        }
        String id = buf.readUtf();
        return new CutsceneData(List.copyOf(keyframes), duration, curve, timeEasing, lookEasing, stopMode, skippable, loop, List.copyOf(screenEffects), List.copyOf(sounds), id);
    }
}
