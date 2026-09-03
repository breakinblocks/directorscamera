package com.breakinblocks.directorscamera.cutscene;

import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum ScreenEffectType implements StringRepresentable {
    COLOR,
    CHROMATIC;

    public static final StringRepresentable.EnumCodec<ScreenEffectType> CODEC = StringRepresentable.fromEnum(ScreenEffectType::values);

    public static ScreenEffectType parse(Object value) {
        if (value instanceof ScreenEffectType type) {
            return type;
        }
        if (value == null) {
            throw new IllegalArgumentException("Screen effect type cannot be null");
        }
        String name = value.toString().trim().toUpperCase(Locale.ROOT);
        for (ScreenEffectType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown screen effect type: " + value + ". Valid options: " + options());
    }

    public static String options() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
