package com.breakinblocks.directorscut.expression;

import java.util.HashMap;
import java.util.Map;

public class ExpressionContext {
    private Map<String, Float> variables;

    public ExpressionContext() {
        this(true);
    }

    public ExpressionContext(boolean init) {
        this.variables = init ? new HashMap<>() : null;
    }

    public ExpressionContext addVariable(String name, float value) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put(name, value);
        return this;
    }

    public float getVariable(String name) {
        if (variables == null) {
            return 0.0F;
        }
        Float value = variables.get(name);
        return value == null ? 0.0F : value;
    }

    public Map<String, Float> getVariables() {
        if (variables == null) {
            variables = new HashMap<>();
        }
        return variables;
    }

    public void setVariables(Map<String, Float> map) {
        this.variables = map;
    }
}
