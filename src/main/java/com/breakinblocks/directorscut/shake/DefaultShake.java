package com.breakinblocks.directorscut.shake;

import com.breakinblocks.directorscut.curves.Easings;
import com.breakinblocks.directorscut.curves.PiecewiseEasing;
import com.breakinblocks.directorscut.util.MathUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.Random;

public class DefaultShake implements ScreenShake {
    private static final long SEED_MULTIPLIER = 34324L;

    private final ShakeData data;
    private final PiecewiseEasing envelope;
    private int oldTime;
    private double xo;
    private double yo;

    public DefaultShake(ShakeData data) {
        this.data = data;
        this.envelope = new PiecewiseEasing()
            .addArea(data.inTime(), Easings::linear)
            .addArea(data.stayTime(), Easings::one)
            .addArea(data.outTime(), Easings::reversedLinear);
    }

    public ShakeData getData() {
        return data;
    }

    @Override
    public void process(PoseStack poseStack, int time, float partialTicks) {
        float power = envelope.applyFloat(time + partialTicks);
        Random random = new Random((time + 1) * SEED_MULTIPLIER);
        double x = randomOffset(random, power);
        double y = randomOffset(random, power);
        double xd = MathUtil.lerp(xo, x, partialTicks);
        double yd = MathUtil.lerp(yo, y, partialTicks);
        poseStack.translate(Double.isNaN(xd) ? 0 : xd, Double.isNaN(yd) ? 0 : yd, 0);
        if (oldTime != time) {
            oldTime = time;
            Random previous = new Random(time * SEED_MULTIPLIER);
            xo = randomOffset(previous, power);
            yo = randomOffset(previous, power);
        }
    }

    private double randomOffset(Random random, float power) {
        float amplitude = data.amplitude();
        return (random.nextFloat() * 2 * amplitude - amplitude) * power;
    }

    @Override
    public boolean hasEnded(int elapsedTime) {
        return elapsedTime > data.duration();
    }
}
