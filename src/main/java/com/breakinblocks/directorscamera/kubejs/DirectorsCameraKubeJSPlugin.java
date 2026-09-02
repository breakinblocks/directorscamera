package com.breakinblocks.directorscamera.kubejs;

import com.breakinblocks.directorscamera.cutscene.CutsceneHooks;
import com.breakinblocks.directorscamera.cutscene.CutsceneRegistry;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.neoforged.fml.loading.FMLEnvironment;

public class DirectorsCameraKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void init() {
        CutsceneHooks.Holder.set(new KubeJSCutsceneHooks());
        if (FMLEnvironment.dist.isClient()) {
            KubeJSClientHooks.install();
        }
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("DirectorsCamera", DirectorsCameraBindings.class);
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(DirectorsCameraEventsGroup.GROUP);
        registry.register(DirectorsCameraEventsGroup.CLIENT_GROUP);
    }

    @Override
    public void afterScriptsLoaded(ScriptManager manager) {
        if (manager.scriptType == ScriptType.SERVER) {
            CutsceneRegistry.clearScript();
            if (DirectorsCameraEventsGroup.REGISTER.hasListeners()) {
                DirectorsCameraEventsGroup.REGISTER.post(new RegisterCutscenesEvent());
            }
        }
    }
}
