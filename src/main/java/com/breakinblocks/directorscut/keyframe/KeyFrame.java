package com.breakinblocks.directorscut.keyframe;

import com.breakinblocks.directorscut.expression.ExpressionContext;
import com.breakinblocks.directorscut.expression.ExpressionVector3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public record KeyFrame(@Nullable ExpressionVector3 pre, ExpressionVector3 post, int time, InterpolationMode mode) {
    public static KeyFrame constant(Vector3f value, int time, InterpolationMode mode) {
        return new KeyFrame(null, ExpressionVector3.of(value), time, mode);
    }

    @Nullable
    public Vector3f getPreValue(ExpressionContext context) {
        return pre == null ? null : pre.get(context);
    }

    public Vector3f getPostValue(ExpressionContext context) {
        return post.get(context);
    }

    public Vector3f getArrivalValue(ExpressionContext context) {
        return pre != null ? pre.get(context) : post.get(context);
    }

    public KeyFrame withTime(int newTime) {
        return new KeyFrame(pre, post, newTime, mode);
    }
}
