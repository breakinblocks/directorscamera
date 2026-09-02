package com.breakinblocks.directorscamera.item;

import com.breakinblocks.directorscamera.anchor.AnchorIndex;
import com.breakinblocks.directorscamera.curves.CurveType;
import com.breakinblocks.directorscamera.curves.EasingType;
import com.breakinblocks.directorscamera.cutscene.CameraPos;
import com.breakinblocks.directorscamera.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscamera.cutscene.CutsceneFrame;
import com.breakinblocks.directorscamera.cutscene.FrameSource;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CameraRecording(List<CameraPos> keyframes, int duration, CurveType curve, EasingType timeEasing, EasingType lookEasing, String name, String anchor) {
    public static final CameraRecording EMPTY = new CameraRecording(List.of(), 0, CurveType.CATMULLROM, EasingType.EASE_IN_OUT, EasingType.EASE_IN_OUT, "", "");

    public static final Codec<CameraRecording> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CameraPos.CODEC.listOf().optionalFieldOf("keyframes", List.of()).forGetter(CameraRecording::keyframes),
        Codec.INT.optionalFieldOf("duration", 0).forGetter(CameraRecording::duration),
        CurveType.CODEC.optionalFieldOf("curve", CurveType.CATMULLROM).forGetter(CameraRecording::curve),
        EasingType.CODEC.optionalFieldOf("timeEasing", EasingType.EASE_IN_OUT).forGetter(CameraRecording::timeEasing),
        EasingType.CODEC.optionalFieldOf("lookEasing", EasingType.EASE_IN_OUT).forGetter(CameraRecording::lookEasing),
        Codec.STRING.optionalFieldOf("name", "").forGetter(CameraRecording::name),
        Codec.STRING.optionalFieldOf("anchor", "").forGetter(CameraRecording::anchor)
    ).apply(instance, CameraRecording::new));

    public static final StreamCodec<FriendlyByteBuf, CameraRecording> STREAM_CODEC = StreamCodec.of(CameraRecording::write, CameraRecording::read);

    public CameraRecording withKeyframes(List<CameraPos> list) {
        return new CameraRecording(List.copyOf(list), duration, curve, timeEasing, lookEasing, name, anchor);
    }

    public CameraRecording withDuration(int value) {
        return new CameraRecording(keyframes, Math.max(0, value), curve, timeEasing, lookEasing, name, anchor);
    }

    public CameraRecording withCurve(CurveType value) {
        return new CameraRecording(keyframes, duration, value, timeEasing, lookEasing, name, anchor);
    }

    public CameraRecording withTimeEasing(EasingType value) {
        return new CameraRecording(keyframes, duration, curve, value, lookEasing, name, anchor);
    }

    public CameraRecording withLookEasing(EasingType value) {
        return new CameraRecording(keyframes, duration, curve, timeEasing, value, name, anchor);
    }

    public CameraRecording withName(String value) {
        return new CameraRecording(keyframes, duration, curve, timeEasing, lookEasing, value == null ? "" : value, anchor);
    }

    public CameraRecording withAnchor(String value) {
        return new CameraRecording(keyframes, duration, curve, timeEasing, lookEasing, name, value == null ? "" : value);
    }

    public boolean isAnchored() {
        return !anchor.isEmpty();
    }

    public CameraRecording append(CameraPos pos) {
        List<CameraPos> list = new ArrayList<>(keyframes);
        list.add(pos);
        return withKeyframes(list);
    }

    public CameraRecording insert(int index, CameraPos pos) {
        List<CameraPos> list = new ArrayList<>(keyframes);
        list.add(Math.max(0, Math.min(index, list.size())), pos);
        return withKeyframes(list);
    }

    public CameraRecording replace(int index, CameraPos pos) {
        List<CameraPos> list = new ArrayList<>(keyframes);
        list.set(index, pos);
        return withKeyframes(list);
    }

    public CameraRecording removeLast() {
        if (keyframes.isEmpty()) {
            return this;
        }
        List<CameraPos> list = new ArrayList<>(keyframes);
        list.removeLast();
        return withKeyframes(list);
    }

    public boolean isEmpty() {
        return keyframes.isEmpty();
    }

    public double pathLength() {
        double total = 0;
        for (int i = 0; i < keyframes.size() - 1; i++) {
            total += keyframes.get(i).pos().distanceTo(keyframes.get(i + 1).pos());
        }
        return total;
    }

    public int effectiveDuration() {
        if (duration > 0) {
            return duration;
        }
        return Math.max(20, (int) Math.ceil(pathLength() / CutsceneDefinition.AUTO_DURATION_SPEED * 20));
    }

    public Optional<CutsceneFrame> resolveFrame(Level level, Vec3 near) {
        if (!isAnchored()) {
            return Optional.empty();
        }
        return AnchorIndex.nearest(level, anchor, near, 0);
    }

    public List<CameraPos> worldKeyframes(Level level, Vec3 near) {
        Optional<CutsceneFrame> frame = resolveFrame(level, near);
        if (frame.isEmpty()) {
            return keyframes;
        }
        List<CameraPos> world = new ArrayList<>(keyframes.size());
        for (CameraPos pos : keyframes) {
            world.add(frame.get().toWorld(pos));
        }
        return world;
    }

    public CameraPos toStored(CameraPos world, Level level, Vec3 near) {
        Optional<CutsceneFrame> frame = resolveFrame(level, near);
        return frame.map(f -> f.toLocal(world)).orElse(world);
    }

    public CutsceneDefinition toDefinition() {
        CutsceneDefinition def = CutsceneDefinition.create()
            .setCurve(curve)
            .setTimeEasing(timeEasing)
            .setLookEasing(lookEasing)
            .id(name);
        if (duration > 0) {
            def.setDuration(duration);
        } else {
            def.setDurationAuto();
        }
        if (isAnchored()) {
            def.anchored(anchor);
        }
        def.getPath().addKeyframes(keyframes);
        return def;
    }

    public static CameraRecording fromDefinition(CutsceneDefinition def) {
        String anchorId = def.getFrameSource() instanceof FrameSource.Anchor a ? a.anchorId() : "";
        return new CameraRecording(List.copyOf(def.getPath().getKeyframes()), Math.max(0, def.getDuration()), def.getCurve(), def.getTimeEasing(), def.getLookEasing(), def.getId(), anchorId);
    }

    private static void write(FriendlyByteBuf buf, CameraRecording recording) {
        buf.writeVarInt(recording.keyframes.size());
        for (CameraPos pos : recording.keyframes) {
            CameraPos.STREAM_CODEC.encode(buf, pos);
        }
        buf.writeVarInt(recording.duration);
        buf.writeUtf(recording.curve.name());
        buf.writeUtf(recording.timeEasing.name());
        buf.writeUtf(recording.lookEasing.name());
        buf.writeUtf(recording.name);
        buf.writeUtf(recording.anchor);
    }

    private static CameraRecording read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<CameraPos> keyframes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keyframes.add(CameraPos.STREAM_CODEC.decode(buf));
        }
        int duration = buf.readVarInt();
        CurveType curve = CurveType.parse(buf.readUtf());
        EasingType timeEasing = EasingType.parse(buf.readUtf());
        EasingType lookEasing = EasingType.parse(buf.readUtf());
        String name = buf.readUtf();
        String anchor = buf.readUtf();
        return new CameraRecording(List.copyOf(keyframes), duration, curve, timeEasing, lookEasing, name, anchor);
    }
}
