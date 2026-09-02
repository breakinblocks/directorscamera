package com.breakinblocks.directorscut.kubejs;

import com.breakinblocks.directorscut.client.ClientCutsceneHooks;

public class KubeJSClientHooks implements ClientCutsceneHooks {
    public static void install() {
        ClientCutsceneHooks.Holder.set(new KubeJSClientHooks());
    }

    @Override
    public void started(String id) {
        if (DirectorsCutEventsGroup.CLIENT_STARTED.hasListeners()) {
            DirectorsCutEventsGroup.CLIENT_STARTED.post(new ClientCutsceneKubeEvent(id, "started"));
        }
    }

    @Override
    public void ended(String id, String reason) {
        if (DirectorsCutEventsGroup.CLIENT_ENDED.hasListeners()) {
            DirectorsCutEventsGroup.CLIENT_ENDED.post(new ClientCutsceneKubeEvent(id, reason));
        }
    }
}
