package com.breakinblocks.directorscamera.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CutsceneTickEvent implements KubeEvent {
    private final ServerPlayer player;
    private final String id;
    private final int tick;

    public CutsceneTickEvent(ServerPlayer player, String id, int tick) {
        this.player = player;
        this.id = id;
        this.tick = tick;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getLevel() {
        return player.level();
    }

    public String getId() {
        return id;
    }

    public int getTick() {
        return tick;
    }

    public double getSeconds() {
        return tick / 20.0;
    }
}
