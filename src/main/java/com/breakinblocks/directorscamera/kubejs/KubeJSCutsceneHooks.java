package com.breakinblocks.directorscamera.kubejs;

import com.breakinblocks.directorscamera.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscamera.cutscene.CutsceneHooks;
import dev.latvian.mods.kubejs.event.EventResult;
import net.minecraft.server.level.ServerPlayer;

public class KubeJSCutsceneHooks implements CutsceneHooks {
    @Override
    public boolean beforePlay(ServerPlayer player, CutsceneDefinition definition) {
        if (!DirectorsCameraEventsGroup.BEFORE_PLAY.hasListeners()) {
            return true;
        }
        EventResult result = DirectorsCameraEventsGroup.BEFORE_PLAY.post(new BeforePlayEvent(player, definition));
        return !result.interruptFalse() && !result.interruptDefault();
    }

    @Override
    public void started(ServerPlayer player, String id) {
        if (DirectorsCameraEventsGroup.STARTED.hasListeners()) {
            DirectorsCameraEventsGroup.STARTED.post(new CutsceneStartedEvent(player, id));
        }
    }

    @Override
    public void tick(ServerPlayer player, String id, int tick) {
        if (DirectorsCameraEventsGroup.TICK.hasListeners()) {
            DirectorsCameraEventsGroup.TICK.post(new CutsceneTickEvent(player, id, tick));
        }
    }

    @Override
    public void ended(ServerPlayer player, String id, String reason) {
        if (DirectorsCameraEventsGroup.ENDED.hasListeners()) {
            DirectorsCameraEventsGroup.ENDED.post(new CutsceneEndedEvent(player, id, reason));
        }
    }
}
