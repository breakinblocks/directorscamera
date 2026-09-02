package com.breakinblocks.directorscamera.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Optional;

public record CutsceneSound(
    int tick,
    boolean stop,
    ResourceLocation sound,
    float volume,
    float pitch,
    SoundSource category,
    Optional<Vec3> position,
    boolean attachToCamera,
    boolean stopOnEnd,
    String id
) {
    public static final Codec<SoundSource> SOURCE_CODEC = Codec.STRING.xmap(CutsceneSound::parseSource, s -> s.getName());

    public static final Codec<CutsceneSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("tick").forGetter(CutsceneSound::tick),
        Codec.BOOL.optionalFieldOf("isStop", false).forGetter(CutsceneSound::stop),
        ResourceLocation.CODEC.optionalFieldOf("sound", ResourceLocation.withDefaultNamespace("intentionally_empty")).forGetter(CutsceneSound::sound),
        Codec.FLOAT.optionalFieldOf("volume", 1.0F).forGetter(CutsceneSound::volume),
        Codec.FLOAT.optionalFieldOf("pitch", 1.0F).forGetter(CutsceneSound::pitch),
        SOURCE_CODEC.optionalFieldOf("category", SoundSource.MASTER).forGetter(CutsceneSound::category),
        Vec3.CODEC.optionalFieldOf("pos").forGetter(CutsceneSound::position),
        Codec.BOOL.optionalFieldOf("attachToCamera", false).forGetter(CutsceneSound::attachToCamera),
        Codec.BOOL.optionalFieldOf("stopOnEnd", true).forGetter(CutsceneSound::stopOnEnd),
        Codec.STRING.optionalFieldOf("id", "").forGetter(CutsceneSound::id)
    ).apply(instance, CutsceneSound::new));

    public static final StreamCodec<FriendlyByteBuf, CutsceneSound> STREAM_CODEC = StreamCodec.of(CutsceneSound::write, CutsceneSound::read);

    public static CutsceneSound play(int tick, ResourceLocation sound) {
        return new CutsceneSound(tick, false, sound, 1.0F, 1.0F, SoundSource.MASTER, Optional.empty(), false, true, "");
    }

    public static CutsceneSound stopEntry(int tick, String id) {
        return new CutsceneSound(tick, true, ResourceLocation.withDefaultNamespace("intentionally_empty"), 1.0F, 1.0F, SoundSource.MASTER, Optional.empty(), false, true, id);
    }

    public CutsceneSound withVolume(float value) {
        return new CutsceneSound(tick, stop, sound, value, pitch, category, position, attachToCamera, stopOnEnd, id);
    }

    public CutsceneSound withPitch(float value) {
        return new CutsceneSound(tick, stop, sound, volume, value, category, position, attachToCamera, stopOnEnd, id);
    }

    public CutsceneSound withCategory(SoundSource value) {
        return new CutsceneSound(tick, stop, sound, volume, pitch, value, position, attachToCamera, stopOnEnd, id);
    }

    public CutsceneSound withPosition(Vec3 value) {
        return new CutsceneSound(tick, stop, sound, volume, pitch, category, Optional.ofNullable(value), attachToCamera, stopOnEnd, id);
    }

    public CutsceneSound withAttachToCamera(boolean value) {
        return new CutsceneSound(tick, stop, sound, volume, pitch, category, position, value, stopOnEnd, id);
    }

    public CutsceneSound withStopOnEnd(boolean value) {
        return new CutsceneSound(tick, stop, sound, volume, pitch, category, position, attachToCamera, value, id);
    }

    public CutsceneSound withId(String value) {
        return new CutsceneSound(tick, stop, sound, volume, pitch, category, position, attachToCamera, stopOnEnd, value == null ? "" : value);
    }

    public static SoundSource parseSource(Object value) {
        if (value instanceof SoundSource source) {
            return source;
        }
        String name = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        for (SoundSource source : SoundSource.values()) {
            if (source.getName().equals(name) || source.name().toLowerCase(Locale.ROOT).equals(name)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown sound category: " + value);
    }

    private static void write(FriendlyByteBuf buf, CutsceneSound s) {
        buf.writeInt(s.tick);
        buf.writeBoolean(s.stop);
        if (s.stop) {
            buf.writeUtf(s.id);
            return;
        }
        buf.writeResourceLocation(s.sound);
        buf.writeFloat(s.volume);
        buf.writeFloat(s.pitch);
        buf.writeUtf(s.category.getName());
        buf.writeBoolean(s.position.isPresent());
        if (s.position.isPresent()) {
            buf.writeDouble(s.position.get().x);
            buf.writeDouble(s.position.get().y);
            buf.writeDouble(s.position.get().z);
        }
        buf.writeBoolean(s.attachToCamera);
        buf.writeBoolean(s.stopOnEnd);
        buf.writeBoolean(!s.id.isEmpty());
        if (!s.id.isEmpty()) {
            buf.writeUtf(s.id);
        }
    }

    private static CutsceneSound read(FriendlyByteBuf buf) {
        int tick = buf.readInt();
        boolean stop = buf.readBoolean();
        if (stop) {
            return stopEntry(tick, buf.readUtf());
        }
        ResourceLocation sound = buf.readResourceLocation();
        float volume = buf.readFloat();
        float pitch = buf.readFloat();
        SoundSource category = parseSource(buf.readUtf());
        Optional<Vec3> position = Optional.empty();
        if (buf.readBoolean()) {
            position = Optional.of(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        boolean attach = buf.readBoolean();
        boolean stopOnEnd = buf.readBoolean();
        String id = buf.readBoolean() ? buf.readUtf() : "";
        return new CutsceneSound(tick, false, sound, volume, pitch, category, position, attach, stopOnEnd, id);
    }
}
