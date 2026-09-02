package com.breakinblocks.directorscamera.keyframe;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AnimationSystem {
    private final Map<String, AnimationTicker> tickers = new HashMap<>();
    private final Map<String, Float> variables = new HashMap<>();
    private boolean frozen;
    private AnimationSystemListener listener = AnimationSystemListener.NONE;
    @Nullable
    private BiConsumer<AnimationTarget, Float> applyListener;

    public AnimationSystem() {
    }

    public AnimationSystem(AnimationSystemListener listener) {
        this.listener = listener == null ? AnimationSystemListener.NONE : listener;
    }

    public void tick() {
        if (frozen) {
            return;
        }
        List<Map.Entry<String, AnimationTicker>> queued = new ArrayList<>();
        Iterator<Map.Entry<String, AnimationTicker>> iterator = tickers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AnimationTicker> entry = iterator.next();
            AnimationTicker ticker = entry.getValue();
            if (ticker.hasEnded()) {
                if (ticker.getAnimation().isToNullTransition()) {
                    iterator.remove();
                    continue;
                }
                AnimationContext context = context(ticker.getAnimation(), ticker.getLoopMode());
                ticker.addVariables(context, 0.0F);
                if (ticker.getLoopMode() == LoopMode.ONCE) {
                    if (ticker.getNext() == null) {
                        int toNull = ticker.getToNullTransitionTime();
                        if (toNull == 0) {
                            iterator.remove();
                            continue;
                        }
                        Animation transition = ticker.getAnimation().createTransitionTo(context, null, ticker.getTime(0.0F), toNull, false);
                        ticker.resetTime();
                        ticker.setAnimation(transition);
                    } else {
                        queued.add(Map.entry(entry.getKey(), ticker.getNext()));
                    }
                } else if (ticker.getLoopMode() == LoopMode.HOLD_ON_LAST_FRAME) {
                    continue;
                }
            }
            ticker.tick();
        }
        for (Map.Entry<String, AnimationTicker> entry : queued) {
            startAnimation(entry.getKey(), entry.getValue());
        }
    }

    public void applyAnimations(AnimationTarget target, float partialTicks) {
        target.resetPose();
        if (applyListener != null) {
            applyListener.accept(target, partialTicks);
        }
        for (AnimationTicker ticker : tickers.values()) {
            AnimationContext context = context(ticker.getAnimation(), ticker.getLoopMode());
            ticker.addVariables(context, partialTicks);
            ticker.getAnimation().applyAnimation(context, target, ticker.getTime(partialTicks));
        }
    }

    public void startAnimation(String layer, AnimationTicker ticker) {
        AnimationTicker current = tickers.get(layer);
        if (current == null) {
            tickers.put(layer, ticker);
        } else {
            if (ticker.equals(current) && !ticker.isImportant()) {
                return;
            }
            AnimationContext context = context(current.getAnimation(), ticker.getLoopMode());
            current.addVariables(context, 0.0F);
            Animation transition = current.getAnimation().createTransitionTo(context, ticker.getAnimation(), current.getTime(0.0F), ticker.getToNullTransitionTime(), ticker.isReversed());
            AnimationTicker copy = new AnimationTicker(ticker);
            copy.setAnimation(transition);
            copy.resetTime();
            tickers.put(layer, copy);
        }
        listener.onAnimationStart(layer, ticker);
    }

    public void startAnimation(String layer, Animation animation) {
        startAnimation(layer, new AnimationTicker(animation));
    }

    public void stopAnimation(String layer) {
        AnimationTicker current = tickers.get(layer);
        if (current != null && !current.getAnimation().isToNullTransition()) {
            int toNull = current.getToNullTransitionTime();
            if (toNull != 0) {
                AnimationContext context = context(current.getAnimation(), LoopMode.ONCE);
                current.addVariables(context, 0.0F);
                Animation transition = current.getAnimation().createTransitionTo(context, null, current.getTime(0.0F), toNull, false);
                AnimationTicker copy = new AnimationTicker(current);
                copy.setLoopMode(LoopMode.ONCE);
                copy.setAnimation(transition);
                copy.resetTime();
                tickers.put(layer, copy);
            } else {
                tickers.remove(layer);
            }
        }
        listener.onAnimationStop(layer);
    }

    public void stopAll() {
        for (String layer : new ArrayList<>(tickers.keySet())) {
            stopAnimation(layer);
        }
    }

    public void clear() {
        tickers.clear();
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        listener.onFreeze(frozen);
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setVariable(String name, float value) {
        variables.put(name, value);
        listener.onVariableAdded(name, value);
    }

    public float getVariable(String name) {
        Float value = variables.get(name);
        return value == null ? 0.0F : value;
    }

    public Map<String, Float> getVariables() {
        return variables;
    }

    @Nullable
    public AnimationTicker getTicker(String layer) {
        return tickers.get(layer);
    }

    @Nullable
    public Animation getTickerAnimation(String layer) {
        AnimationTicker ticker = tickers.get(layer);
        return ticker == null ? null : ticker.getAnimation();
    }

    public Map<String, AnimationTicker> getTickers() {
        return tickers;
    }

    public boolean isPlaying(String layer) {
        return tickers.containsKey(layer);
    }

    public void setListener(AnimationSystemListener listener) {
        this.listener = listener == null ? AnimationSystemListener.NONE : listener;
    }

    public void setAnimationsApplyListener(@Nullable BiConsumer<AnimationTarget, Float> applyListener) {
        this.applyListener = applyListener;
    }

    private AnimationContext context(Animation animation, LoopMode loopMode) {
        AnimationContext context = new AnimationContext(animation, loopMode);
        context.setVariables(variables);
        return context;
    }
}
