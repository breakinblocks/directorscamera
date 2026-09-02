package com.breakinblocks.directorscut.kubejs;

import com.breakinblocks.directorscut.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscut.cutscene.CutsceneRegistry;
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
