package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.DirectorsCamera;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CutsceneSession {
    public static final String REASON_FINISHED = "finished";
    public static final String REASON_PLAYER = "player";
    public static final String REASON_SERVER = "server";
    public static final String REASON_DISCONNECT = "disconnect";
    public static final String REASON_TIMEOUT = "timeout";

    private final UUID playerId;
    private final CutsceneDefinition definition;
    private final CutsceneData data;
    private final List<PendingAction> pending = new ArrayList<>();
    @Nullable
    private final CutsceneFrame frame;
    private int tick;
    private boolean started;
    private boolean finished;

    public CutsceneSession(ServerPlayer player, CutsceneDefinition definition, CutsceneData data, @Nullable CutsceneFrame frame) {
        this.playerId = player.getUUID();
        this.definition = definition;
        this.data = data;
        this.frame = frame;
        int keyframes = data.keyframes().size();
        for (CutsceneAction action : definition.getActions()) {
            pending.add(new PendingAction(action.resolveTick(data.duration(), keyframes), action));
        }
        pending.sort(Comparator.comparingInt(a -> a.tick));
    }

    public UUID playerId() {
        return playerId;
    }

    public CutsceneDefinition definition() {
        return definition;
    }

    public CutsceneData data() {
        return data;
    }

    @Nullable
    public CutsceneFrame frame() {
        return frame;
    }

    public String id() {
        return data.id();
    }

    public int tick() {
        return tick;
    }

    public boolean started() {
        return started;
    }

    public boolean finished() {
        return finished;
    }

    public void markStarted() {
        started = true;
    }

    public void advance(ServerPlayer player) {
        tick++;
        runDue(player, tick);
        CutsceneHooks.Holder.get().tick(player, id(), tick);
    }

    public void finish(ServerPlayer player) {
        finished = true;
        runDue(player, data.duration());
        runEndCallbacks(player, REASON_FINISHED);
    }

    public void cancel(ServerPlayer player, String reason) {
        finished = true;
        for (PendingAction action : pending) {
            if (!action.fired && action.action.alwaysRun()) {
                fire(player, action);
            }
        }
        if (REASON_PLAYER.equals(reason)) {
            for (CutsceneCallback callback : definition.getSkipCallbacks()) {
                try {
                    callback.run(player);
                } catch (Exception e) {
                    DirectorsCamera.LOGGER.error("Cutscene {} skip callback failed", id(), e);
                }
            }
        }
        runEndCallbacks(player, reason);
    }

    private void runDue(ServerPlayer player, int upTo) {
        for (PendingAction action : pending) {
            if (!action.fired && action.tick <= upTo) {
                fire(player, action);
            }
        }
    }

    private void fire(ServerPlayer player, PendingAction action) {
        action.fired = true;
        try {
            action.action.callback().run(player);
        } catch (Exception e) {
            DirectorsCamera.LOGGER.error("Cutscene {} action at tick {} failed", id(), action.tick, e);
        }
    }

    private void runEndCallbacks(ServerPlayer player, String reason) {
        for (CutsceneEndCallback callback : definition.getEndCallbacks()) {
            try {
                callback.run(player, reason);
            } catch (Exception e) {
                DirectorsCamera.LOGGER.error("Cutscene {} end callback failed", id(), e);
            }
        }
    }

    private static class PendingAction {
        private final int tick;
        private final CutsceneAction action;
        private boolean fired;

        private PendingAction(int tick, CutsceneAction action) {
            this.tick = tick;
            this.action = action;
        }
    }
}
