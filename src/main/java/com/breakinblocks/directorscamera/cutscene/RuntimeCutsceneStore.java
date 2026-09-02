package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.DirectorsCamera;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID)
public class RuntimeCutsceneStore extends SavedData {
    private static final String NAME = "directorscamera_cutscenes";
    private static final Factory<RuntimeCutsceneStore> FACTORY = new Factory<>(RuntimeCutsceneStore::new, RuntimeCutsceneStore::load);

    private final Map<String, CutsceneData> cutscenes = new LinkedHashMap<>();

    public static RuntimeCutsceneStore get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
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

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        cutscenes.forEach((id, data) -> CutsceneData.CODEC.encodeStart(NbtOps.INSTANCE, data.withId(id))
            .resultOrPartial(error -> DirectorsCamera.LOGGER.error("Failed to save cutscene {}: {}", id, error))
            .ifPresent(list::add));
        tag.put("cutscenes", list);
        return tag;
    }

    private static RuntimeCutsceneStore load(CompoundTag tag, HolderLookup.Provider registries) {
        RuntimeCutsceneStore store = new RuntimeCutsceneStore();
        for (Tag element : tag.getList("cutscenes", Tag.TAG_COMPOUND)) {
            CutsceneData.CODEC.parse(NbtOps.INSTANCE, element)
                .resultOrPartial(error -> DirectorsCamera.LOGGER.error("Failed to load saved cutscene: {}", error))
                .ifPresent(data -> store.cutscenes.put(data.id(), data));
        }
        return store;
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
