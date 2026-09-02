package com.breakinblocks.directorscamera.keyframe;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID)
public class AnimationLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Gson GSON = new GsonBuilder().create();

    public AnimationLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("directorscamera/animations"));
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(DirectorsCamera.id("animations"), new AnimationLoader());
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, Animation> animations = new LinkedHashMap<>();
        Map<Identifier, String> json = new LinkedHashMap<>();
        map.forEach((fileId, element) -> {
            try {
                JsonObject root = element.getAsJsonObject();
                boolean bedrock = GsonHelper.getAsBoolean(root, "bedrock_conventions", true);
                if (!root.has("animations")) {
                    Identifier id = fileId;
                    animations.put(id, Animation.load(id, root, bedrock));
                    json.put(id, GSON.toJson(root));
                    return;
                }
                for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("animations").entrySet()) {
                    Identifier id = Identifier.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath() + "/" + entry.getKey());
                    JsonObject body = entry.getValue().getAsJsonObject();
                    if (!body.has("bedrock_conventions")) {
                        body.addProperty("bedrock_conventions", bedrock);
                    }
                    animations.put(id, Animation.load(id, body, bedrock));
                    json.put(id, GSON.toJson(body));
                }
            } catch (Exception e) {
                DirectorsCamera.LOGGER.error("Failed to load animation file {}", fileId, e);
            }
        });
        AnimationRegistry.setDataAnimations(animations, json);
        DirectorsCamera.LOGGER.info("Loaded {} keyframe animations", animations.size());
    }
}
