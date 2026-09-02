package com.breakinblocks.directorscut.curves;

public final class Easings {
    private Easings() {
    }

    public static float gaussian(float p) {
        return (float) Math.exp(-p * p);
    }

    public static float one(float p) {
        return 1.0F;
    }

    public static float zero(float p) {
        return 0.0F;
    }

    public static float linear(float p) {
        return p;
    }

    public static float reversedLinear(float p) {
        return 1.0F - p;
    }

    public static float squareHill(float p) {
        float d = p - 0.5F;
        return 1.0F - 4.0F * d * d;
    }

    public static float quadroHill(float p) {
        float d = p - 0.5F;
        return 1.0F - 16.0F * d * d * d * d;
    }

    public static float easeIn(float p) {
        return p * p;
    }

    public static float easeOut(float p) {
        float d = p - 1.0F;
        return 1.0F - d * d;
    }

    public static float reversedEaseOut(float p) {
        return 1.0F - p * p;
    }

    public static float easeInOut(float p) {
        return p <= 0.5F ? 2.0F * p * p : -2.0F * (p - 1.0F) * (p - 1.0F) + 1.0F;
    }

    public static float reversedEaseInOut(float p) {
        return easeInOut(1.0F - p);
    }

    public static float easeOutBack(float x) {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float d = x - 1.0F;
        return 1.0F + c3 * d * d * d + c1 * d * d;
    }

    public static float easeInOutBack(float x) {
        float c1 = 1.70158F;
        float c2 = c1 * 1.525F;
        if (x < 0.5F) {
            float t = 2.0F * x;
            return (t * t * ((c2 + 1.0F) * t - c2)) / 2.0F;
        }
        float t = 2.0F * x - 2.0F;
        return (t * t * ((c2 + 1.0F) * t + c2) + 2.0F) / 2.0F;
    }

    public static float easeOutBounce(float x) {
        float n1 = 7.5625F;
        float d1 = 2.75F;
        if (x < 1.0F / d1) {
            return n1 * x * x;
        }
        if (x < 2.0F / d1) {
            float t = x - 1.5F / d1;
            return n1 * t * t + 0.75F;
        }
        if (x < 2.5F / d1) {
            float t = x - 2.25F / d1;
            return n1 * t * t + 0.9375F;
        }
        float t = x - 2.625F / d1;
        return n1 * t * t + 0.984375F;
    }
}
