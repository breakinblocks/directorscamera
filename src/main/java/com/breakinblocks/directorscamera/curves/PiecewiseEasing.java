package com.breakinblocks.directorscamera.curves;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PiecewiseEasing implements Function<Float, Float> {
    private final List<Area> areas = new ArrayList<>();
    private float length;

    public PiecewiseEasing addArea(float areaLength, Function<Float, Float> function) {
        areas.add(new Area(areaLength, function));
        length += areaLength;
        return this;
    }

    public float getLength() {
        return length;
    }

    public float applyFloat(float value) {
        if (length <= 0 || areas.isEmpty()) {
            return value;
        }
        float v = Math.max(0.0F, Math.min(length, value));
        float start = 0.0F;
        Area selected = areas.getLast();
        float selectedStart = length - selected.length;
        for (Area area : areas) {
            if (start + area.length > v) {
                selected = area;
                selectedStart = start;
                break;
            }
            start += area.length;
        }
        float local = v - selectedStart;
        if (selected.length <= 0) {
            return selected.function.apply(1.0F);
        }
        return selected.function.apply(local / selected.length);
    }

    @Override
    public Float apply(Float value) {
        return applyFloat(value);
    }

    private record Area(float length, Function<Float, Float> function) {
    }
}
