package com.breakinblocks.directorscut.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneStartedEvent implements KubeEvent {
    private final ServerPlayer player;
    private final String id;

    public CutsceneStartedEvent(ServerPlayer player, String id) {
        this.player = player;
        this.id = id;
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
}
