package com.breakinblocks.directorscamera.cutscene;

public class CutsceneAction {
    private final int tick;
    private final int keyframeIndex;
    private final CutsceneCallback callback;
    private final boolean alwaysRun;

    private CutsceneAction(int tick, int keyframeIndex, CutsceneCallback callback, boolean alwaysRun) {
        this.tick = tick;
        this.keyframeIndex = keyframeIndex;
        this.callback = callback;
        this.alwaysRun = alwaysRun;
    }

    public static CutsceneAction atTick(int tick, CutsceneCallback callback, boolean alwaysRun) {
        return new CutsceneAction(Math.max(0, tick), -1, callback, alwaysRun);
    }

    public static CutsceneAction atKeyframe(int keyframeIndex, CutsceneCallback callback, boolean alwaysRun) {
        return new CutsceneAction(0, Math.max(0, keyframeIndex), callback, alwaysRun);
    }

    public int resolveTick(int duration, int keyframeCount) {
        if (keyframeIndex < 0) {
            return tick;
        }
        int segments = Math.max(keyframeCount - 1, 1);
        return (int) Math.floor((double) duration * Math.min(keyframeIndex, segments) / segments);
    }

    public CutsceneCallback callback() {
        return callback;
    }

    public boolean alwaysRun() {
        return alwaysRun;
    }

    public boolean isKeyframeRelative() {
        return keyframeIndex >= 0;
    }

    public int keyframeIndex() {
        return keyframeIndex;
    }

    public int rawTick() {
        return tick;
    }
}
