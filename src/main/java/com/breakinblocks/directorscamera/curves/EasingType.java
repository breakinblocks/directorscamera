package com.breakinblocks.directorscamera.curves;

import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum EasingType implements StringRepresentable {
    LINEAR,
    EASE_IN,
    EASE_OUT,
    EASE_IN_OUT;

    public static final StringRepresentable.EnumCodec<EasingType> CODEC = StringRepresentable.fromEnum(EasingType::values);

    public float apply(float p) {
        return switch (this) {
            case LINEAR -> p;
            case EASE_IN -> p * p;
            case EASE_OUT -> 1.0F - (p - 1.0F) * (p - 1.0F);
            case EASE_IN_OUT -> p <= 0.5F ? 2.0F * p * p : -2.0F * (p - 1.0F) * (p - 1.0F) + 1.0F;
        };
    }

    public static EasingType parse(Object value) {
        if (value instanceof EasingType type) {
            return type;
        }
        if (value == null) {
            throw new IllegalArgumentException("Easing name cannot be null");
        }
        String name = value.toString().trim().toUpperCase(Locale.ROOT);
        for (EasingType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown easing type: " + value + ". Valid options: " + options());
    }

    public static String options() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
