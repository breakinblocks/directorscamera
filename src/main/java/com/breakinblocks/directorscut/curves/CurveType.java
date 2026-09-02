package com.breakinblocks.directorscut.curves;

import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum CurveType implements StringRepresentable {
    LINEAR,
    CATMULLROM;

    public static final StringRepresentable.EnumCodec<CurveType> CODEC = StringRepresentable.fromEnum(CurveType::values);

    public static CurveType parse(Object value) {
        if (value instanceof CurveType type) {
            return type;
        }
        if (value == null) {
            throw new IllegalArgumentException("Curve name cannot be null");
        }
        String name = value.toString().trim().toUpperCase(Locale.ROOT);
        for (CurveType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown curve type: " + value + ". Valid options: " + options());
    }

    public static String options() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
