package com.breakinblocks.directorscut.kubejs;

import com.breakinblocks.directorscut.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscut.cutscene.CutsceneHooks;
import dev.latvian.mods.kubejs.event.EventResult;
import net.minecraft.server.level.ServerPlayer;

public class KubeJSCutsceneHooks implements CutsceneHooks {
    @Override
    public boolean beforePlay(ServerPlayer player, CutsceneDefinition definition) {
        if (!DirectorsCutEventsGroup.BEFORE_PLAY.hasListeners()) {
            return true;
        }
        EventResult result = DirectorsCutEventsGroup.BEFORE_PLAY.post(new BeforePlayEvent(player, definition));
        return !result.interruptFalse() && !result.interruptDefault();
    }

    @Override
    public void started(ServerPlayer player, String id) {
        if (DirectorsCutEventsGroup.STARTED.hasListeners()) {
            DirectorsCutEventsGroup.STARTED.post(new CutsceneStartedEvent(player, id));
        }
    }

    @Override
    public void tick(ServerPlayer player, String id, int tick) {
        if (DirectorsCutEventsGroup.TICK.hasListeners()) {
            DirectorsCutEventsGroup.TICK.post(new CutsceneTickEvent(player, id, tick));
        }
    }

    @Override
    public void ended(ServerPlayer player, String id, String reason) {
        if (DirectorsCutEventsGroup.ENDED.hasListeners()) {
            DirectorsCutEventsGroup.ENDED.post(new CutsceneEndedEvent(player, id, reason));
        }
    }
}
