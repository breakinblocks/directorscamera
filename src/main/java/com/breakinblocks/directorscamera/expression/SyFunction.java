package com.breakinblocks.directorscamera.expression;

import java.util.List;
import java.util.function.Function;

public class SyFunction implements SyNode {
    private final String name;
    private final int argumentCount;
    private final Function<List<Float>, Float> body;

    public SyFunction(String name, int argumentCount, Function<List<Float>, Float> body) {
        this.name = name;
        this.argumentCount = argumentCount;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public int getArgumentCount() {
        return argumentCount;
    }

    public float compute(List<Float> arguments) {
        return body.apply(arguments);
    }

    @Override
    public String toString() {
        return name;
    }
}
