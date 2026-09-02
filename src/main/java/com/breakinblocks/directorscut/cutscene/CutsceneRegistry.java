package com.breakinblocks.directorscut.cutscene;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class CutsceneRegistry {
    private static final Map<String, CutsceneDefinition> DATA = new LinkedHashMap<>();
    private static final Map<String, CutsceneDefinition> SCRIPT = new LinkedHashMap<>();
    private static final Map<String, CutsceneDefinition> RUNTIME = new LinkedHashMap<>();

    private CutsceneRegistry() {
    }

    @Nullable
    public static CutsceneDefinition get(String id) {
        if (id == null) {
            return null;
        }
        String key = normalize(id);
        CutsceneDefinition def = RUNTIME.get(key);
        if (def == null) {
            def = SCRIPT.get(key);
        }
        if (def == null) {
            def = DATA.get(key);
        }
        return def;
    }

    public static Set<String> ids() {
        Set<String> ids = new TreeSet<>(DATA.keySet());
        ids.addAll(SCRIPT.keySet());
        ids.addAll(RUNTIME.keySet());
        return ids;
    }

    public static void registerScript(String id, CutsceneDefinition definition) {
        String key = normalize(id);
        definition.id(key);
        SCRIPT.put(key, definition);
    }

    public static void registerRuntime(String id, CutsceneDefinition definition) {
        String key = normalize(id);
        definition.id(key);
        RUNTIME.put(key, definition);
    }

    public static void clearScript() {
        SCRIPT.clear();
    }

    public static void setDataDefinitions(Map<String, CutsceneDefinition> definitions) {
        DATA.clear();
        definitions.forEach((id, def) -> {
            String key = normalize(id);
            def.id(key);
            DATA.put(key, def);
        });
    }

    public static boolean removeRuntime(String id) {
        return RUNTIME.remove(normalize(id)) != null;
    }

    public static void clearRuntime() {
        RUNTIME.clear();
    }

    public static void clearAll() {
        DATA.clear();
        SCRIPT.clear();
        RUNTIME.clear();
    }

    public static String normalize(String id) {
        String trimmed = id.trim();
        return trimmed.contains(":") ? trimmed : "directorscut:" + trimmed;
    }
}
