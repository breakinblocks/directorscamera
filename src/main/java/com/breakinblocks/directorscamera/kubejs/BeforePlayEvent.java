package com.breakinblocks.directorscamera.kubejs;

import com.breakinblocks.directorscamera.cutscene.CutsceneDefinition;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BeforePlayEvent implements KubeEvent {
    private final ServerPlayer player;
    private final CutsceneDefinition definition;

    public BeforePlayEvent(ServerPlayer player, CutsceneDefinition definition) {
        this.player = player;
        this.definition = definition;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getLevel() {
        return player.serverLevel();
    }

    public String getId() {
        return definition.getId();
    }

    public CutsceneDefinition getCutscene() {
        return definition;
    }
}
