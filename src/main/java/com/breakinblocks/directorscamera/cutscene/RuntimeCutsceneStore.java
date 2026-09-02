package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID)
public class RuntimeCutsceneStore extends SavedData {
    private static final Codec<RuntimeCutsceneStore> CODEC = CutsceneData.CODEC.listOf()
        .xmap(RuntimeCutsceneStore::fromList, RuntimeCutsceneStore::toList)
        .fieldOf("cutscenes")
        .codec();

    private static final SavedDataType<RuntimeCutsceneStore> TYPE =
        new SavedDataType<>(DirectorsCamera.id("cutscenes"), RuntimeCutsceneStore::new, CODEC);

    private final Map<String, CutsceneData> cutscenes = new LinkedHashMap<>();

    public static RuntimeCutsceneStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void put(String id, CutsceneData data) {
        cutscenes.put(id, data);
        setDirty();
    }

    public boolean remove(String id) {
        boolean removed = cutscenes.remove(id) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public Map<String, CutsceneData> all() {
        return cutscenes;
    }

    public void applyToRegistry() {
        cutscenes.forEach((id, data) -> CutsceneRegistry.registerRuntime(id, CutsceneDefinition.fromData(data)));
    }

    private static RuntimeCutsceneStore fromList(List<CutsceneData> list) {
        RuntimeCutsceneStore store = new RuntimeCutsceneStore();
        for (CutsceneData data : list) {
            store.cutscenes.put(data.id(), data);
        }
        return store;
    }

    private static List<CutsceneData> toList(RuntimeCutsceneStore store) {
        List<CutsceneData> list = new ArrayList<>(store.cutscenes.size());
        store.cutscenes.forEach((id, data) -> list.add(data.withId(id)));
        return list;
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        RuntimeCutsceneStore store = get(event.getServer());
        store.applyToRegistry();
        if (!store.cutscenes.isEmpty()) {
            DirectorsCamera.LOGGER.info("Restored {} saved cutscenes", store.cutscenes.size());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CutsceneRegistry.clearRuntime();
    }
}
