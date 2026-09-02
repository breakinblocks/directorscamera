package com.breakinblocks.directorscut.expression;

import org.joml.Vector3f;

public class ExpressionVector3 {
    private final RpnExpression x;
    private final RpnExpression y;
    private final RpnExpression z;

    public ExpressionVector3(RpnExpression x, RpnExpression y, RpnExpression z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ExpressionVector3(String x, String y, String z) {
        this(RpnExpression.parse(x), RpnExpression.parse(y), RpnExpression.parse(z));
    }

    public ExpressionVector3(float x, float y, float z) {
        this(RpnExpression.constant(x), RpnExpression.constant(y), RpnExpression.constant(z));
    }

    public static ExpressionVector3 of(Vector3f vector) {
        return new ExpressionVector3(vector.x, vector.y, vector.z);
    }

    public float getX(ExpressionContext context) {
        return x.compute(context);
    }

    public float getY(ExpressionContext context) {
        return y.compute(context);
    }

    public float getZ(ExpressionContext context) {
        return z.compute(context);
    }

    public Vector3f get(ExpressionContext context) {
        return new Vector3f(getX(context), getY(context), getZ(context));
    }

    public boolean isConstant() {
        return x.isConstant() && y.isConstant() && z.isConstant();
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
