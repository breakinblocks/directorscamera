package com.breakinblocks.directorscamera.cutscene;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

public record CutsceneScreenEffect(
    int tick,
    ScreenEffectType type,
    float red,
    float green,
    float blue,
    float alpha,
    float strength,
    int fadeIn,
    int hold,
    int fadeOut
) {
    public static final float MAX_STRENGTH = 0.5F;

    public static final Codec<CutsceneScreenEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("tick").forGetter(CutsceneScreenEffect::tick),
        ScreenEffectType.CODEC.optionalFieldOf("type", ScreenEffectType.COLOR).forGetter(CutsceneScreenEffect::type),
        Codec.FLOAT.optionalFieldOf("red", 0.0F).forGetter(CutsceneScreenEffect::red),
        Codec.FLOAT.optionalFieldOf("green", 0.0F).forGetter(CutsceneScreenEffect::green),
        Codec.FLOAT.optionalFieldOf("blue", 0.0F).forGetter(CutsceneScreenEffect::blue),
        Codec.FLOAT.optionalFieldOf("alpha", 1.0F).forGetter(CutsceneScreenEffect::alpha),
        Codec.FLOAT.optionalFieldOf("strength", 0.0F).forGetter(CutsceneScreenEffect::strength),
        Codec.INT.optionalFieldOf("fadeIn", 0).forGetter(CutsceneScreenEffect::fadeIn),
        Codec.INT.optionalFieldOf("hold", 0).forGetter(CutsceneScreenEffect::hold),
        Codec.INT.optionalFieldOf("fadeOut", 0).forGetter(CutsceneScreenEffect::fadeOut)
    ).apply(instance, CutsceneScreenEffect::new));

    public static final StreamCodec<FriendlyByteBuf, CutsceneScreenEffect> STREAM_CODEC = StreamCodec.of(CutsceneScreenEffect::write, CutsceneScreenEffect::read);

    public static CutsceneScreenEffect of(int tick, double red, double green, double blue, double alpha, int fadeIn, int hold, int fadeOut) {
        return new CutsceneScreenEffect(
            tick,
            ScreenEffectType.COLOR,
            clampUnit(red),
            clampUnit(green),
            clampUnit(blue),
            clampUnit(alpha),
            0.0F,
            Math.max(0, fadeIn),
            Math.max(0, hold),
            Math.max(0, fadeOut)
        );
    }

    public static CutsceneScreenEffect black(int tick, int fadeIn, int hold, int fadeOut) {
        return of(tick, 0.0, 0.0, 0.0, 1.0, fadeIn, hold, fadeOut);
    }

    public static CutsceneScreenEffect white(int tick, int fadeIn, int hold, int fadeOut) {
        return of(tick, 1.0, 1.0, 1.0, 1.0, fadeIn, hold, fadeOut);
    }

    public static CutsceneScreenEffect chromatic(int tick, double strength, int fadeIn, int hold, int fadeOut) {
        return new CutsceneScreenEffect(
            tick,
            ScreenEffectType.CHROMATIC,
            0.0F,
            0.0F,
            0.0F,
            0.0F,
            (float) Mth.clamp(strength, 0.0, MAX_STRENGTH),
            Math.max(0, fadeIn),
            Math.max(0, hold),
            Math.max(0, fadeOut)
        );
    }

    public int totalTicks() {
        return fadeIn + hold + fadeOut;
    }

    public float envelopeAt(float elapsed) {
        if (elapsed < 0.0F) {
            return 0.0F;
        }
        if (fadeIn > 0 && elapsed < fadeIn) {
            return elapsed / fadeIn;
        }
        float afterIn = elapsed - fadeIn;
        if (afterIn < hold) {
            return 1.0F;
        }
        if (fadeOut <= 0) {
            return 0.0F;
        }
        float out = afterIn - hold;
        if (out >= fadeOut) {
            return 0.0F;
        }
        return 1.0F - out / fadeOut;
    }

    public float alphaAt(float elapsed) {
        return alpha * envelopeAt(elapsed);
    }

    public float strengthAt(float elapsed) {
        return strength * envelopeAt(elapsed);
    }

    public int packedColor(float effectiveAlpha) {
        int a = Mth.clamp((int) (effectiveAlpha * 255.0F), 0, 255);
        int r = Mth.clamp((int) (red * 255.0F), 0, 255);
        int g = Mth.clamp((int) (green * 255.0F), 0, 255);
        int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static float clampUnit(double value) {
        return (float) Mth.clamp(value, 0.0, 1.0);
    }

    private static void write(FriendlyByteBuf buf, CutsceneScreenEffect effect) {
        buf.writeInt(effect.tick);
        buf.writeEnum(effect.type);
        buf.writeFloat(effect.red);
        buf.writeFloat(effect.green);
        buf.writeFloat(effect.blue);
        buf.writeFloat(effect.alpha);
        buf.writeFloat(effect.strength);
        buf.writeVarInt(effect.fadeIn);
        buf.writeVarInt(effect.hold);
        buf.writeVarInt(effect.fadeOut);
    }

    private static CutsceneScreenEffect read(FriendlyByteBuf buf) {
        int tick = buf.readInt();
        ScreenEffectType type = buf.readEnum(ScreenEffectType.class);
        float red = buf.readFloat();
        float green = buf.readFloat();
        float blue = buf.readFloat();
        float alpha = buf.readFloat();
        float strength = buf.readFloat();
        int fadeIn = buf.readVarInt();
        int hold = buf.readVarInt();
        int fadeOut = buf.readVarInt();
        return new CutsceneScreenEffect(tick, type, red, green, blue, alpha, strength, fadeIn, hold, fadeOut);
    }
}
