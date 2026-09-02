package com.breakinblocks.directorscut.kubejs;

import com.breakinblocks.directorscut.cutscene.CutsceneHooks;
import com.breakinblocks.directorscut.cutscene.CutsceneRegistry;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.neoforged.fml.loading.FMLEnvironment;

public class DirectorsCutKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void init() {
        CutsceneHooks.Holder.set(new KubeJSCutsceneHooks());
        if (FMLEnvironment.dist.isClient()) {
            KubeJSClientHooks.install();
        }
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("DirectorsCut", DirectorsCutBindings.class);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(DirectorsCutEventsGroup.GROUP);
        registry.register(DirectorsCutEventsGroup.CLIENT_GROUP);
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType == ScriptType.SERVER) {
            CutsceneRegistry.clearScript();
            if (DirectorsCutEventsGroup.REGISTER.hasListeners()) {
                DirectorsCutEventsGroup.REGISTER.post(new RegisterCutscenesEvent());
            }
        }
    }
}
