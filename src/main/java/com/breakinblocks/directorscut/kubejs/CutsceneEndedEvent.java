package com.breakinblocks.directorscut.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneEndedEvent implements KubeEvent {
    private final ServerPlayer player;
    private final String id;
    private final String reason;

    public CutsceneEndedEvent(ServerPlayer player, String id, String reason) {
        this.player = player;
        this.id = id;
        this.reason = reason;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getLevel() {
        return player.serverLevel();
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public boolean isFinished() {
        return "finished".equals(reason);
    }

    public boolean isSkipped() {
        return "player".equals(reason);
    }
}
