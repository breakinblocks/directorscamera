package com.breakinblocks.directorscut.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;

public class ClientCutsceneKubeEvent implements KubeEvent {
    private final String id;
    private final String reason;

    public ClientCutsceneKubeEvent(String id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }
}
