package com.breakinblocks.directorscut.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public final class DirectorsCutEventsGroup {
    public static final EventGroup GROUP = EventGroup.of("DirectorsCutEvents");
    public static final EventHandler REGISTER = GROUP.server("register", () -> RegisterCutscenesEvent.class);
    public static final EventHandler BEFORE_PLAY = GROUP.server("beforePlay", () -> BeforePlayEvent.class).hasResult();
    public static final EventHandler STARTED = GROUP.server("started", () -> CutsceneStartedEvent.class);
    public static final EventHandler TICK = GROUP.server("tick", () -> CutsceneTickEvent.class);
    public static final EventHandler ENDED = GROUP.server("ended", () -> CutsceneEndedEvent.class);

    public static final EventGroup CLIENT_GROUP = EventGroup.of("DirectorsCutClientEvents");
    public static final EventHandler CLIENT_STARTED = CLIENT_GROUP.client("started", () -> ClientCutsceneKubeEvent.class);
    public static final EventHandler CLIENT_ENDED = CLIENT_GROUP.client("ended", () -> ClientCutsceneKubeEvent.class);

    private DirectorsCutEventsGroup() {
    }
}
