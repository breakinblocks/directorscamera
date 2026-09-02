package com.breakinblocks.directorscut.expression;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public final class FunctionRegistry {
    public static final Map<String, SyFunction> FUNCTIONS = new HashMap<>();
    public static final Map<String, SyFunction> OPERATORS = new HashMap<>();
    public static final Map<String, Integer> OPERATOR_PRIORITY = new HashMap<>();
    public static final SyFunction NEGATE = new SyFunction("neg", 1, a -> -a.get(0));
    private static final Random RANDOM = new Random();

    static {
        operator("+", 2, a -> a.get(0) + a.get(1));
        operator("-", 2, a -> a.get(0) - a.get(1));
        operator("*", 3, a -> a.get(0) * a.get(1));
        operator("/", 3, a -> a.get(0) / a.get(1));
        register("math.abs", 1, a -> Math.abs(a.get(0)));
        register("math.acos", 1, a -> (float) Math.toDegrees(Math.acos(a.get(0))));
        register("math.asin", 1, a -> (float) Math.toDegrees(Math.asin(a.get(0))));
        register("math.atan", 1, a -> (float) Math.toDegrees(Math.atan(a.get(0))));
        register("math.atan2", 2, a -> (float) Math.toDegrees(Math.atan2(a.get(0), a.get(1))));
        register("math.ceil", 1, a -> (float) Math.ceil(a.get(0)));
        register("math.clamp", 3, a -> Mth.clamp(a.get(0), a.get(1), a.get(2)));
        register("math.cos", 1, a -> (float) Math.cos(Math.toRadians(a.get(0))));
        register("math.exp", 1, a -> (float) Math.exp(a.get(0)));
        register("math.floor", 1, a -> (float) Math.floor(a.get(0)));
        register("math.lerp", 3, a -> a.get(0) + (a.get(1) - a.get(0)) * a.get(2));
        register("math.ln", 1, a -> (float) Math.log(a.get(0)));
        register("math.max", 2, a -> Math.max(a.get(0), a.get(1)));
        register("math.min", 2, a -> Math.min(a.get(0), a.get(1)));
        register("math.mod", 2, a -> a.get(0) % a.get(1));
        register("math.pow", 2, a -> (float) Math.pow(a.get(0), a.get(1)));
        register("math.random", 2, a -> a.get(0) + RANDOM.nextFloat() * (a.get(1) - a.get(0)));
        register("math.round", 1, a -> (float) Math.round(a.get(0)));
        register("math.sin", 1, a -> (float) Math.sin(Math.toRadians(a.get(0))));
        register("math.sqrt", 1, a -> (float) Math.sqrt(a.get(0)));
        register("math.trunc", 1, a -> (float) (int) a.get(0).floatValue());
    }

    private FunctionRegistry() {
    }

    public static SyFunction register(String name, int argumentCount, Function<List<Float>, Float> body) {
        SyFunction function = new SyFunction(name, argumentCount, body);
        FUNCTIONS.put(name, function);
        return function;
    }

    private static void operator(String symbol, int priority, Function<List<Float>, Float> body) {
        OPERATORS.put(symbol, new SyFunction(symbol, 2, body));
        OPERATOR_PRIORITY.put(symbol, priority);
    }

    @Nullable
    public static SyFunction getFunction(String name) {
        return FUNCTIONS.get(name);
    }

    public static boolean isOperator(char c) {
        return OPERATORS.containsKey(String.valueOf(c));
    }
}
