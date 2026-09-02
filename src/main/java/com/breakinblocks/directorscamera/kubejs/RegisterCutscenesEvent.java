package com.breakinblocks.directorscamera.kubejs;

import com.breakinblocks.directorscamera.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscamera.cutscene.CutsceneRegistry;
import dev.latvian.mods.kubejs.event.KubeEvent;

public class RegisterCutscenesEvent implements KubeEvent {
    public CutsceneDefinition create(String id) {
        CutsceneDefinition definition = CutsceneDefinition.create();
        CutsceneRegistry.registerScript(id, definition);
        return definition;
    }

    public CutsceneDefinition add(String id, CutsceneDefinition definition) {
        CutsceneRegistry.registerScript(id, definition);
        return definition;
    }
}
