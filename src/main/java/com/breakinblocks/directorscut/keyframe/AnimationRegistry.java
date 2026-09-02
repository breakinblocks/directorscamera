package com.breakinblocks.directorscut.keyframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class AnimationRegistry {
    private static final Map<ResourceLocation, Animation> DATA = new LinkedHashMap<>();
    private static final Map<ResourceLocation, String> DATA_JSON = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Animation> RUNTIME = new LinkedHashMap<>();

    private AnimationRegistry() {
    }

    @Nullable
    public static Animation get(ResourceLocation id) {
        Animation animation = RUNTIME.get(id);
        return animation != null ? animation : DATA.get(id);
    }

    @Nullable
    public static Animation get(String id) {
        return get(ResourceLocation.parse(id));
    }

    public static Set<String> ids() {
        Set<String> ids = new TreeSet<>();
        DATA.keySet().forEach(id -> ids.add(id.toString()));
        RUNTIME.keySet().forEach(id -> ids.add(id.toString()));
        return ids;
    }

    public static Animation register(ResourceLocation id, Animation animation) {
        RUNTIME.put(id, animation);
        return animation;
    }

    public static Animation parseAndRegister(ResourceLocation id, String json, boolean bedrockConventions) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return register(id, Animation.load(id, object, bedrockConventions));
    }

    public static void setDataAnimations(Map<ResourceLocation, Animation> animations, Map<ResourceLocation, String> json) {
        DATA.clear();
        DATA.putAll(animations);
        DATA_JSON.clear();
        DATA_JSON.putAll(json);
    }

    public static Map<ResourceLocation, String> dataJson() {
        return DATA_JSON;
    }

    public static void clearRuntime() {
        RUNTIME.clear();
    }
}
