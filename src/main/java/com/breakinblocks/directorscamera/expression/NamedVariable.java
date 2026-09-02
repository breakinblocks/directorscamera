package com.breakinblocks.directorscamera.expression;

public record NamedVariable(String name) implements SyValue {
    @Override
    public float getValue(ExpressionContext context) {
        return context.getVariable(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
