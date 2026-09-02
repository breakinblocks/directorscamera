package com.breakinblocks.directorscut.cutscene;

import com.breakinblocks.directorscut.DirectorsCut;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public class CutsceneJsonLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public CutsceneJsonLoader() {
        super(GSON, "directorscut/cutscenes");
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CutsceneJsonLoader());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, CutsceneDefinition> definitions = new LinkedHashMap<>();
        map.forEach((id, json) -> {
            try {
                definitions.put(id.toString(), parse(id.toString(), json.getAsJsonObject()));
            } catch (Exception e) {
                DirectorsCut.LOGGER.error("Failed to load cutscene {}", id, e);
            }
        });
        CutsceneRegistry.setDataDefinitions(definitions);
        DirectorsCut.LOGGER.info("Loaded {} cutscene definitions", definitions.size());
    }

    public static CutsceneDefinition parse(String id, JsonObject json) {
        CutsceneDefinition def = CutsceneDefinition.create().id(id);
        if (json.has("duration")) {
            def.setDuration(GsonHelper.getAsInt(json, "duration"));
        } else if (json.has("durationSeconds")) {
            def.setDurationSeconds(GsonHelper.getAsDouble(json, "durationSeconds"));
        } else {
            def.setDurationAuto();
        }
        if (json.has("curve")) {
            def.setCurve(GsonHelper.getAsString(json, "curve"));
        }
        if (json.has("easing")) {
            def.setEasing(GsonHelper.getAsString(json, "easing"));
        }
        if (json.has("timeEasing")) {
            def.setTimeEasing(GsonHelper.getAsString(json, "timeEasing"));
        }
        if (json.has("lookEasing")) {
            def.setLookEasing(GsonHelper.getAsString(json, "lookEasing"));
        }
        if (json.has("stopMode")) {
            def.setStopMode(GsonHelper.getAsString(json, "stopMode"));
        }
        if (json.has("skippable")) {
            def.skippable(GsonHelper.getAsBoolean(json, "skippable"));
        }
        if (json.has("loop")) {
            def.loop(GsonHelper.getAsBoolean(json, "loop"));
        }
        if (json.has("anchorMaxDistance")) {
            def.anchorMaxDistance(GsonHelper.getAsDouble(json, "anchorMaxDistance"));
        }
        if (json.has("anchor")) {
            JsonElement anchor = json.get("anchor");
            if (anchor.isJsonPrimitive()) {
                def.anchored(anchor.getAsString());
            } else {
                JsonObject obj = anchor.getAsJsonObject();
                String type = GsonHelper.getAsString(obj, "type", "anchor");
                switch (type) {
                    case "anchor" -> def.anchored(GsonHelper.getAsString(obj, "id"));
                    case "player" -> def.anchoredToPlayer();
                    case "structure" -> {
                        if (obj.has("id")) {
                            def.anchoredToStructure(GsonHelper.getAsString(obj, "id"));
                        } else {
                            def.anchoredToStructure();
                        }
                    }
                    case "fixed" -> {
                        Vec3 pos = readVec(obj, "pos");
                        def.anchoredTo(pos.x, pos.y, pos.z, GsonHelper.getAsDouble(obj, "yaw", 0));
                    }
                    default -> throw new IllegalArgumentException("Unknown anchor type: " + type);
                }
            }
        }
        if (json.has("startFromPlayer")) {
            def.startFromPlayer(GsonHelper.getAsBoolean(json, "startFromPlayer"));
        }
        if (json.has("endAtPlayer")) {
            def.endAtPlayer(GsonHelper.getAsBoolean(json, "endAtPlayer"));
        }
        if (json.has("keyframes")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "keyframes")) {
                parseKeyframe(def.getPath(), element);
            }
        }
        if (json.has("sounds")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "sounds")) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("second") && !obj.has("tick")) {
                    obj.addProperty("tick", (int) Math.floor(obj.get("second").getAsDouble() * 20));
                }
                if (obj.has("stop")) {
                    def.stopSound(GsonHelper.getAsInt(obj, "tick"), GsonHelper.getAsString(obj, "stop"));
                    continue;
                }
                CutsceneSound sound = CutsceneSound.CODEC.parse(JsonOps.INSTANCE, obj)
                    .getOrThrow(msg -> new IllegalArgumentException("Bad sound entry in " + id + ": " + msg));
                def.addSound(sound);
            }
        }
        if (json.has("actions")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "actions")) {
                JsonObject obj = element.getAsJsonObject();
                int tick = obj.has("tick") ? GsonHelper.getAsInt(obj, "tick") : (int) Math.floor(GsonHelper.getAsDouble(obj, "second", 0) * 20);
                String command = GsonHelper.getAsString(obj, "command");
                boolean alwaysRun = GsonHelper.getAsBoolean(obj, "alwaysRun", false);
                def.addAction(CutsceneAction.atTick(tick, player -> runCommand(player, command), alwaysRun));
            }
        }
        if (json.has("next")) {
            JsonElement next = json.get("next");
            if (next.isJsonObject()) {
                def.setNext(parse(id + "/next", next.getAsJsonObject()));
            } else {
                def.setNextId(next.getAsString());
            }
        }
        return def;
    }

    private static void parseKeyframe(CameraPathBuilder path, JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            double x = arr.get(0).getAsDouble();
            double y = arr.get(1).getAsDouble();
            double z = arr.get(2).getAsDouble();
            double yaw = arr.size() > 3 ? arr.get(3).getAsDouble() : 0;
            double pitch = arr.size() > 4 ? arr.get(4).getAsDouble() : 0;
            double roll = arr.size() > 5 ? arr.get(5).getAsDouble() : 0;
            path.addPoint(x, y, z, yaw, pitch, roll);
            return;
        }
        JsonObject obj = element.getAsJsonObject();
        String type = GsonHelper.getAsString(obj, "type", "point");
        switch (type) {
            case "point" -> {
                Vec3 pos = readVec(obj, "pos");
                if (obj.has("lookAt")) {
                    Vec3 target = readVec(obj, "lookAt");
                    path.addKeyframe(CameraPos.lookingAt(pos, target, GsonHelper.getAsFloat(obj, "roll", 0)));
                } else {
                    path.addVec3(pos, GsonHelper.getAsDouble(obj, "yaw", 0), GsonHelper.getAsDouble(obj, "pitch", 0), GsonHelper.getAsDouble(obj, "roll", 0));
                }
            }
            case "orbit" -> {
                Vec3 c = readVec(obj, "center");
                path.addOrbit(c.x, c.y, c.z, GsonHelper.getAsDouble(obj, "radius"), GsonHelper.getAsDouble(obj, "startAngle", 0),
                    GsonHelper.getAsDouble(obj, "endAngle", 360), GsonHelper.getAsInt(obj, "points", 20), GsonHelper.getAsBoolean(obj, "lookAtCenter", true));
            }
            case "spin" -> {
                Vec3 p = readVec(obj, "pos");
                path.addSpin(p.x, p.y, p.z, GsonHelper.getAsDouble(obj, "startYaw", 0), GsonHelper.getAsDouble(obj, "endYaw", 360),
                    GsonHelper.getAsDouble(obj, "pitch", 0), GsonHelper.getAsInt(obj, "points", 36));
            }
            case "panorama" -> {
                Vec3 p = readVec(obj, "pos");
                path.addPanorama(p.x, p.y, p.z, GsonHelper.getAsDouble(obj, "turns", 1), GsonHelper.getAsDouble(obj, "pitch", 0),
                    GsonHelper.getAsDouble(obj, "startYaw", 0));
            }
            case "arc" -> {
                Vec3 s = readVec(obj, "start");
                Vec3 e = readVec(obj, "end");
                path.addArc(s.x, s.y, s.z, e.x, e.y, e.z, GsonHelper.getAsDouble(obj, "height", 5), GsonHelper.getAsInt(obj, "points", 10));
            }
            case "spiral" -> {
                Vec3 c = readVec(obj, "center");
                path.addSpiral(c.x, c.y, c.z, GsonHelper.getAsDouble(obj, "startRadius"), GsonHelper.getAsDouble(obj, "endRadius", GsonHelper.getAsDouble(obj, "startRadius")),
                    GsonHelper.getAsDouble(obj, "height", 0), GsonHelper.getAsDouble(obj, "turns", 1), GsonHelper.getAsInt(obj, "points", 36));
            }
            default -> throw new IllegalArgumentException("Unknown keyframe type: " + type);
        }
    }

    private static Vec3 readVec(JsonObject obj, String key) {
        JsonArray arr = GsonHelper.getAsJsonArray(obj, key);
        return new Vec3(arr.get(0).getAsDouble(), arr.get(1).getAsDouble(), arr.get(2).getAsDouble());
    }

    private static void runCommand(ServerPlayer player, String command) {
        CommandSourceStack source = player.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        player.server.getCommands().performPrefixedCommand(source, command);
    }
}
