package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.item.RecordingExporter;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.phys.Vec3;

public final class CutsceneJsonWriter {
    private CutsceneJsonWriter() {
    }

    public static JsonObject write(CutsceneDefinition def) {
        JsonObject json = new JsonObject();
        if (def.getDuration() > 0) {
            json.addProperty("duration", def.getDuration());
        }
        json.addProperty("curve", def.getCurve().name());
        json.addProperty("timeEasing", def.getTimeEasing().name());
        json.addProperty("lookEasing", def.getLookEasing().name());
        json.addProperty("stopMode", def.getStopMode().name());
        json.addProperty("skippable", def.isSkippable());
        if (def.isLooping()) {
            json.addProperty("loop", true);
        }
        FrameSource source = def.getFrameSource();
        if (source instanceof FrameSource.Anchor anchor) {
            json.addProperty("anchor", anchor.anchorId());
            if (anchor.maxDistance() > 0) {
                json.addProperty("anchorMaxDistance", anchor.maxDistance());
            }
        } else if (source instanceof FrameSource.PlayerPose) {
            JsonObject anchor = new JsonObject();
            anchor.addProperty("type", "player");
            json.add("anchor", anchor);
        } else if (source instanceof FrameSource.Structure structure) {
            JsonObject anchor = new JsonObject();
            anchor.addProperty("type", "structure");
            if (structure.structureId() != null) {
                anchor.addProperty("id", structure.structureId().toString());
            }
            json.add("anchor", anchor);
        } else if (source instanceof FrameSource.Fixed fixed) {
            JsonObject anchor = new JsonObject();
            anchor.addProperty("type", "fixed");
            anchor.add("pos", vec(fixed.frame().origin()));
            anchor.addProperty("yaw", fixed.frame().yaw());
            json.add("anchor", anchor);
        }
        if (def.isStartFromPlayer()) {
            json.addProperty("startFromPlayer", true);
        }
        if (def.isEndAtPlayer()) {
            json.addProperty("endAtPlayer", true);
        }
        JsonArray keyframes = new JsonArray();
        for (CameraPos pos : def.getPath().getKeyframes()) {
            JsonArray entry = new JsonArray();
            entry.add(round(pos.pos().x));
            entry.add(round(pos.pos().y));
            entry.add(round(pos.pos().z));
            entry.add(round(pos.yaw()));
            entry.add(round(pos.pitch()));
            entry.add(round(pos.roll()));
            keyframes.add(entry);
        }
        json.add("keyframes", keyframes);
        if (!def.getScreenEffects().isEmpty()) {
            JsonArray effects = new JsonArray();
            for (CutsceneScreenEffect effect : def.getScreenEffects()) {
                effects.add(CutsceneScreenEffect.CODEC.encodeStart(JsonOps.INSTANCE, effect).getOrThrow());
            }
            json.add("screenEffects", effects);
        }
        if (!def.getSounds().isEmpty()) {
            JsonArray sounds = new JsonArray();
            for (CutsceneSound sound : def.getSounds()) {
                if (sound.stop()) {
                    JsonObject stop = new JsonObject();
                    stop.addProperty("tick", sound.tick());
                    stop.addProperty("stop", sound.id());
                    sounds.add(stop);
                } else {
                    JsonElement encoded = CutsceneSound.CODEC.encodeStart(JsonOps.INSTANCE, sound).getOrThrow();
                    sounds.add(encoded);
                }
            }
            json.add("sounds", sounds);
        }
        if (!def.getNextId().isEmpty()) {
            json.addProperty("next", def.getNextId());
        } else if (def.getNext() != null && !def.getNext().getId().isEmpty()) {
            json.addProperty("next", def.getNext().getId());
        }
        return json;
    }

    public static String pretty(CutsceneDefinition def) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(write(def));
    }

    private static JsonArray vec(Vec3 v) {
        JsonArray arr = new JsonArray();
        arr.add(round(v.x));
        arr.add(round(v.y));
        arr.add(round(v.z));
        return arr;
    }

    private static double round(double value) {
        return Double.parseDouble(RecordingExporter.format(value));
    }
}
