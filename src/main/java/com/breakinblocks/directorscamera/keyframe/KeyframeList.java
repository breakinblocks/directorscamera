package com.breakinblocks.directorscamera.keyframe;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeyframeList {
    private final List<KeyFrame> frames;

    public KeyframeList(List<KeyFrame> input) {
        List<KeyFrame> sorted = new ArrayList<>(input);
        sorted.sort(Comparator.comparingInt(KeyFrame::time));
        List<KeyFrame> deduped = new ArrayList<>(sorted.size());
        for (KeyFrame frame : sorted) {
            if (!deduped.isEmpty() && deduped.getLast().time() == frame.time()) {
                deduped.set(deduped.size() - 1, frame);
            } else {
                deduped.add(frame);
            }
        }
        this.frames = deduped;
    }

    public int size() {
        return frames.size();
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    public KeyFrame get(int index) {
        return frames.get(index);
    }

    public KeyFrame getFirst() {
        return frames.getFirst();
    }

    public KeyFrame getLast() {
        return frames.getLast();
    }

    public List<KeyFrame> asList() {
        return new ArrayList<>(frames);
    }

    public int indexOf(int tick) {
        int n = frames.size();
        if (n == 0) {
            return -1;
        }
        if (n == 1 || tick <= frames.getFirst().time()) {
            return 0;
        }
        if (tick >= frames.getLast().time()) {
            return n - 1;
        }
        for (int i = 1; i < n; i++) {
            if (tick < frames.get(i).time()) {
                return i - 1;
            }
        }
        return n - 1;
    }

    @Nullable
    public KeyFrame at(int index) {
        return index >= 0 && index < frames.size() ? frames.get(index) : null;
    }

    public KeyFrame[] neighbors(int tick, int before, int after) {
        KeyFrame[] result = new KeyFrame[before + after + 1];
        int index = indexOf(tick);
        for (int i = -before; i <= after; i++) {
            result[i + before] = at(index + i);
        }
        return result;
    }
}
