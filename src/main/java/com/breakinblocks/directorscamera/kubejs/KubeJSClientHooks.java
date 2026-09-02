package com.breakinblocks.directorscamera.kubejs;

import com.breakinblocks.directorscamera.client.ClientCutsceneHooks;

public class KubeJSClientHooks implements ClientCutsceneHooks {
    public static void install() {
        ClientCutsceneHooks.Holder.set(new KubeJSClientHooks());
    }

    @Override
    public void started(String id) {
        if (DirectorsCameraEventsGroup.CLIENT_STARTED.hasListeners()) {
            DirectorsCameraEventsGroup.CLIENT_STARTED.post(new ClientCutsceneKubeEvent(id, "started"));
        }
    }

    @Override
    public void ended(String id, String reason) {
        if (DirectorsCameraEventsGroup.CLIENT_ENDED.hasListeners()) {
            DirectorsCameraEventsGroup.CLIENT_ENDED.post(new ClientCutsceneKubeEvent(id, reason));
        }
    }
}
