package com.breakinblocks.directorscamera.shake;

public class ScreenShakeInstance {
    private final ScreenShake shake;
    private int currentTime;

    public ScreenShakeInstance(ScreenShake shake) {
        this.shake = shake;
    }

    public ScreenShake shake() {
        return shake;
    }

    public int currentTime() {
        return currentTime;
    }

    public void tick() {
        currentTime++;
    }

    public boolean hasEnded() {
        return shake.hasEnded(currentTime);
    }
}
