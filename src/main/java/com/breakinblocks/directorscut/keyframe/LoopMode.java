package com.breakinblocks.directorscut.keyframe;

import java.util.Locale;

public enum LoopMode {
    LOOP,
    ONCE,
    HOLD_ON_LAST_FRAME;

    public static LoopMode parse(Object value) {
        if (value instanceof LoopMode mode) {
            return mode;
        }
        String name = String.valueOf(value).trim().toUpperCase(Locale.ROOT);
        for (LoopMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown loop mode: " + value + ". Valid options: LOOP, ONCE, HOLD_ON_LAST_FRAME");
    }
}
