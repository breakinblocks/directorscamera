package com.breakinblocks.directorscamera.shake;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShakeData(int inTime, int stayTime, int outTime, float amplitude, float frequency) {
    public static final StreamCodec<FriendlyByteBuf, ShakeData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, ShakeData::inTime,
        ByteBufCodecs.INT, ShakeData::stayTime,
        ByteBufCodecs.INT, ShakeData::outTime,
        ByteBufCodecs.FLOAT, ShakeData::amplitude,
        ByteBufCodecs.FLOAT, ShakeData::frequency,
        ShakeData::new
    );

    public static final ShakeData DEFAULT = new ShakeData(2, 2, 2, 0.1F, 1.0F);

    public static Builder builder() {
        return new Builder();
    }

    public int duration() {
        return inTime + stayTime + outTime;
    }

    public static class Builder {
        private int inTime = 2;
        private int stayTime = 2;
        private int outTime = 2;
        private float amplitude = 0.1F;
        private float frequency = 1.0F;

        public Builder inTime(int value) {
            inTime = value;
            return this;
        }

        public Builder stayTime(int value) {
            stayTime = value;
            return this;
        }

        public Builder outTime(int value) {
            outTime = value;
            return this;
        }

        public Builder amplitude(float value) {
            amplitude = value;
            return this;
        }

        public Builder frequency(float value) {
            frequency = value;
            return this;
        }

        public ShakeData build() {
            return new ShakeData(inTime, stayTime, outTime, amplitude, frequency);
        }
    }
}
