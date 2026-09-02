package com.breakinblocks.directorscamera.cutscene;

import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum StopMode implements StringRepresentable {
    AUTOMATIC,
    PLAYER,
    UNSTOPPABLE;

    public static final StringRepresentable.EnumCodec<StopMode> CODEC = StringRepresentable.fromEnum(StopMode::values);

    public static StopMode parse(Object value) {
        if (value instanceof StopMode mode) {
            return mode;
        }
        if (value == null) {
            throw new IllegalArgumentException("Stop mode cannot be null");
        }
        String name = value.toString().trim().toUpperCase(Locale.ROOT);
        for (StopMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid stop mode: " + value + ". Valid options: " + options());
    }

    public static String options() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
