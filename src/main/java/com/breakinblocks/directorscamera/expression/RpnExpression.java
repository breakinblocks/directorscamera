package com.breakinblocks.directorscamera.expression;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RpnExpression {
    private final List<SyNode> nodes;
    private final String source;

    public RpnExpression(List<SyNode> nodes, String source) {
        this.nodes = List.copyOf(nodes);
        this.source = source;
    }

    public static RpnExpression constant(float value) {
        return new RpnExpression(List.of(new StaticValue(value)), Float.toString(value));
    }

    public static RpnExpression parse(String text) {
        try {
            return ShuntingYard.parse(text);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Error while reading expression: " + text + " (" + e.getMessage() + ")", e);
        }
    }

    public float compute(ExpressionContext context) {
        Deque<Float> stack = new ArrayDeque<>();
        for (SyNode node : nodes) {
            if (node instanceof SyValue value) {
                stack.push(value.getValue(context));
            } else if (node instanceof SyFunction function) {
                int count = function.getArgumentCount();
                if (stack.size() < count) {
                    throw new IllegalStateException("Expression is incorrect: " + source);
                }
                List<Float> args = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    args.addFirst(stack.pop());
                }
                stack.push(function.compute(args));
            } else {
                throw new IllegalStateException("Unknown node in expression: " + source);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalStateException("Expression is incorrect: " + source);
        }
        return stack.pop();
    }

    public String getSource() {
        return source;
    }

    public boolean isConstant() {
        return nodes.size() == 1 && nodes.getFirst() instanceof StaticValue;
    }

    @Override
    public String toString() {
        return source;
    }
}
