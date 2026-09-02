package com.breakinblocks.directorscut.keyframe;

import com.breakinblocks.directorscut.DirectorsCut;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public class AnimationLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public AnimationLoader() {
        super(GSON, "directorscut/animations");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new AnimationLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Animation> animations = new LinkedHashMap<>();
        Map<ResourceLocation, String> json = new LinkedHashMap<>();
        map.forEach((fileId, element) -> {
            try {
                JsonObject root = element.getAsJsonObject();
                boolean bedrock = GsonHelper.getAsBoolean(root, "bedrock_conventions", true);
                if (!root.has("animations")) {
                    ResourceLocation id = fileId;
                    animations.put(id, Animation.load(id, root, bedrock));
                    json.put(id, GSON.toJson(root));
                    return;
                }
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("animations").entrySet()) {
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath() + "/" + entry.getKey());
                    JsonObject body = entry.getValue().getAsJsonObject();
                    if (!body.has("bedrock_conventions")) {
                        body.addProperty("bedrock_conventions", bedrock);
                    }
                    animations.put(id, Animation.load(id, body, bedrock));
                    json.put(id, GSON.toJson(body));
                }
            } catch (Exception e) {
                DirectorsCut.LOGGER.error("Failed to load animation file {}", fileId, e);
            }
        });
        AnimationRegistry.setDataAnimations(animations, json);
        DirectorsCut.LOGGER.info("Loaded {} keyframe animations", animations.size());
    }
}
