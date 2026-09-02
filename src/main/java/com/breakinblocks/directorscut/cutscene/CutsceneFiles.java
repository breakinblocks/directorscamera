package com.breakinblocks.directorscut.cutscene;

import com.breakinblocks.directorscut.item.CameraRecording;
import com.breakinblocks.directorscut.item.RecordingExporter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class CutsceneFiles {
    private static final String PACK_MCMETA = "{\n  \"pack\": {\n    \"description\": \"DirectorsCut exported cutscenes\",\n    \"pack_format\": 48\n  }\n}\n";

    private CutsceneFiles() {
    }

    public static Path root() {
        return FMLPaths.GAMEDIR.get().resolve("directorscut");
    }

    public static Path exportRoot() {
        return root().resolve("export");
    }

    public static Path importRoot() {
        return root().resolve("import");
    }

    public static Path exportJson(CutsceneDefinition def) throws IOException {
        ResourceLocation id = ResourceLocation.parse(CutsceneRegistry.normalize(def.getId()));
        Path file = exportRoot().resolve("data").resolve(id.getNamespace()).resolve("directorscut").resolve("cutscenes").resolve(id.getPath() + ".json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, CutsceneJsonWriter.pretty(def), StandardCharsets.UTF_8);
        Path mcmeta = exportRoot().resolve("pack.mcmeta");
        if (!Files.exists(mcmeta)) {
            Files.writeString(mcmeta, PACK_MCMETA, StandardCharsets.UTF_8);
        }
        return file;
    }

    public static Path exportScript(CutsceneDefinition def) throws IOException {
        ResourceLocation id = ResourceLocation.parse(CutsceneRegistry.normalize(def.getId()));
        Path file = exportRoot().resolve("kubejs").resolve("server_scripts").resolve(id.getNamespace() + "_" + id.getPath().replace('/', '_') + ".js");
        Files.createDirectories(file.getParent());
        String script = RecordingExporter.toScript(CameraRecording.fromDefinition(def));
        Files.writeString(file, script, StandardCharsets.UTF_8);
        return file;
    }

    public static List<Path> importFiles() throws IOException {
        Path dir = importRoot();
        Files.createDirectories(dir);
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }
    }

    public static List<String> importNames() {
        try {
            List<String> names = new ArrayList<>();
            Path dir = importRoot();
            for (Path path : importFiles()) {
                names.add(dir.relativize(path).toString().replace('\\', '/'));
            }
            return names;
        } catch (IOException e) {
            return List.of();
        }
    }

    public static Path resolveImport(String name) {
        String clean = name.endsWith(".json") ? name : name + ".json";
        Path path = importRoot().resolve(clean).normalize();
        if (!path.startsWith(importRoot().normalize())) {
            throw new IllegalArgumentException("Import path must be inside " + importRoot());
        }
        return path;
    }

    public static CutsceneDefinition read(Path file, String id) throws IOException {
        JsonObject json = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
        return CutsceneJsonLoader.parse(CutsceneRegistry.normalize(id), json);
    }

    public static String idFor(Path file) {
        Path dir = importRoot().normalize();
        String relative = dir.relativize(file.normalize()).toString().replace('\\', '/');
        if (relative.endsWith(".json")) {
            relative = relative.substring(0, relative.length() - 5);
        }
        String[] parts = relative.split("/", 2);
        if (parts.length == 2 && ResourceLocation.tryParse(parts[0] + ":" + parts[1]) != null) {
            return parts[0] + ":" + parts[1];
        }
        return CutsceneRegistry.normalize(relative.replace('/', '_'));
    }
}
