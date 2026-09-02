package com.breakinblocks.directorscamera.expression;

public record StaticValue(float value) implements SyValue {
    @Override
    public float getValue(ExpressionContext context) {
        return value;
    }

    @Override
    public String toString() {
        return Float.toString(value);
    }
}
