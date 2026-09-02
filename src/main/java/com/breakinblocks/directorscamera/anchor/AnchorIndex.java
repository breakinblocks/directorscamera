package com.breakinblocks.directorscamera.anchor;

import com.breakinblocks.directorscamera.cutscene.CutsceneFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

public final class AnchorIndex {
    private static final Map<Level, Map<String, Set<BlockPos>>> BLOCKS = new WeakHashMap<>();
    private static final Map<Level, Map<String, List<CutsceneFrame>>> VIRTUAL = new WeakHashMap<>();
    private static final Map<Level, Set<AnchorBlockEntity>> TRIGGERS = new WeakHashMap<>();

    private AnchorIndex() {
    }

    public static void register(AnchorBlockEntity anchor) {
        Level level = anchor.getLevel();
        if (level == null) {
            return;
        }
        if (!anchor.getAnchorId().isEmpty()) {
            BLOCKS.computeIfAbsent(level, l -> new HashMap<>()).computeIfAbsent(anchor.getAnchorId(), k -> new HashSet<>()).add(anchor.getBlockPos().immutable());
        }
        if (anchor.hasTrigger()) {
            TRIGGERS.computeIfAbsent(level, l -> new HashSet<>()).add(anchor);
        }
    }

    public static void unregister(AnchorBlockEntity anchor, String previousId) {
        Level level = anchor.getLevel();
        if (level == null) {
            return;
        }
        Map<String, Set<BlockPos>> byId = BLOCKS.get(level);
        if (byId != null && !previousId.isEmpty()) {
            Set<BlockPos> positions = byId.get(previousId);
            if (positions != null) {
                positions.remove(anchor.getBlockPos());
                if (positions.isEmpty()) {
                    byId.remove(previousId);
                }
            }
        }
        Set<AnchorBlockEntity> triggers = TRIGGERS.get(level);
        if (triggers != null) {
            triggers.remove(anchor);
        }
    }

    public static void refresh(AnchorBlockEntity anchor, String previousId) {
        unregister(anchor, previousId);
        register(anchor);
    }

    public static void registerVirtual(Level level, String id, CutsceneFrame frame) {
        VIRTUAL.computeIfAbsent(level, l -> new HashMap<>()).computeIfAbsent(id, k -> new ArrayList<>()).add(frame);
    }

    public static void clearVirtual(Level level, String id) {
        Map<String, List<CutsceneFrame>> byId = VIRTUAL.get(level);
        if (byId != null) {
            byId.remove(id);
        }
    }

    public static List<CutsceneFrame> all(Level level, String id) {
        List<CutsceneFrame> frames = new ArrayList<>();
        Map<String, Set<BlockPos>> byId = BLOCKS.get(level);
        if (byId != null) {
            Set<BlockPos> positions = byId.get(id);
            if (positions != null) {
                for (BlockPos pos : positions) {
                    if (level.getBlockEntity(pos) instanceof AnchorBlockEntity anchor) {
                        frames.add(anchor.getFrame());
                    }
                }
            }
        }
        Map<String, List<CutsceneFrame>> virtual = VIRTUAL.get(level);
        if (virtual != null) {
            frames.addAll(virtual.getOrDefault(id, List.of()));
        }
        return frames;
    }

    public static Set<String> ids(Level level) {
        Set<String> ids = new HashSet<>();
        Map<String, Set<BlockPos>> byId = BLOCKS.get(level);
        if (byId != null) {
            ids.addAll(byId.keySet());
        }
        Map<String, List<CutsceneFrame>> virtual = VIRTUAL.get(level);
        if (virtual != null) {
            ids.addAll(virtual.keySet());
        }
        return ids;
    }

    public static Optional<CutsceneFrame> nearest(Level level, String id, Vec3 from, double maxDistance) {
        CutsceneFrame best = null;
        double bestDist = maxDistance <= 0 ? Double.MAX_VALUE : maxDistance * maxDistance;
        for (CutsceneFrame frame : all(level, id)) {
            double d = frame.origin().distanceToSqr(from);
            if (d <= bestDist) {
                bestDist = d;
                best = frame;
            }
        }
        return Optional.ofNullable(best);
    }

    public static Set<AnchorBlockEntity> triggers(Level level) {
        Set<AnchorBlockEntity> triggers = TRIGGERS.get(level);
        return triggers == null ? Collections.emptySet() : triggers;
    }
}
