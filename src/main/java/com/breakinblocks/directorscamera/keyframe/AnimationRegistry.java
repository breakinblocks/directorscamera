package com.breakinblocks.directorscamera.keyframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class AnimationRegistry {
    private static final Map<Identifier, Animation> DATA = new LinkedHashMap<>();
    private static final Map<Identifier, String> DATA_JSON = new LinkedHashMap<>();
    private static final Map<Identifier, Animation> RUNTIME = new LinkedHashMap<>();

    private AnimationRegistry() {
    }

    @Nullable
    public static Animation get(Identifier id) {
        Animation animation = RUNTIME.get(id);
        return animation != null ? animation : DATA.get(id);
    }

    @Nullable
    public static Animation get(String id) {
        return get(Identifier.parse(id));
    }

    public static Set<String> ids() {
        Set<String> ids = new TreeSet<>();
        DATA.keySet().forEach(id -> ids.add(id.toString()));
        RUNTIME.keySet().forEach(id -> ids.add(id.toString()));
        return ids;
    }

    public static Animation register(Identifier id, Animation animation) {
        RUNTIME.put(id, animation);
        return animation;
    }

    public static Animation parseAndRegister(Identifier id, String json, boolean bedrockConventions) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return register(id, Animation.load(id, object, bedrockConventions));
    }

    public static void setDataAnimations(Map<Identifier, Animation> animations, Map<Identifier, String> json) {
        DATA.clear();
        DATA.putAll(animations);
        DATA_JSON.clear();
        DATA_JSON.putAll(json);
    }

    public static Map<Identifier, String> dataJson() {
        return DATA_JSON;
    }

    public static void clearRuntime() {
        RUNTIME.clear();
    }
}
