package com.breakinblocks.directorscamera.cutscene;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.net.MoveCutsceneCameraPayload;
import com.breakinblocks.directorscamera.net.StartCutscenePayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CutsceneApi {
    private CutsceneApi() {
    }

    public static boolean play(ServerPlayer player, CutsceneDefinition definition) {
        return play(player, definition, null);
    }

    public static boolean play(ServerPlayer player, CutsceneDefinition definition, @Nullable CutsceneFrame frameOverride) {
        if (!CutsceneHooks.Holder.get().beforePlay(player, definition)) {
            return false;
        }
        CutsceneData data;
        try {
            data = definition.build();
        } catch (RuntimeException e) {
            DirectorsCamera.LOGGER.error("Cannot play cutscene {}: {}", definition.getId(), e.getMessage());
            return false;
        }
        CutsceneFrame frame = frameOverride;
        if (frame == null && definition.getFrameSource() != null) {
            Optional<CutsceneFrame> resolved = definition.getFrameSource().resolve(player);
            if (resolved.isEmpty()) {
                DirectorsCamera.LOGGER.warn("Cutscene {} could not resolve its frame ({}) for {}", definition.getId(), definition.getFrameSource().describe(), player.getGameProfile().getName());
                return false;
            }
            frame = resolved.get();
        }
        if (frame != null) {
            data = frame.toWorld(data);
        }
        if (definition.isStartFromPlayer() || definition.isEndAtPlayer()) {
            List<CameraPos> keyframes = new ArrayList<>(data.keyframes());
            CameraPos pose = CameraPos.of(player.getEyePosition(), player.getYRot(), player.getXRot(), 0.0F);
            if (definition.isStartFromPlayer()) {
                keyframes.addFirst(pose);
            }
            if (definition.isEndAtPlayer()) {
                keyframes.add(pose);
            }
            data = data.withKeyframes(keyframes);
        }
        CutsceneSessionManager.start(player, definition, data, frame);
        PacketDistributor.sendToPlayer(player, new StartCutscenePayload(data));
        return true;
    }

    public static boolean playAnchored(ServerPlayer player, CutsceneDefinition definition, String anchorId) {
        Optional<CutsceneFrame> frame = FrameSource.anchor(anchorId, 0).resolve(player);
        if (frame.isEmpty()) {
            DirectorsCamera.LOGGER.warn("No anchor {} found for {}", anchorId, player.getGameProfile().getName());
            return false;
        }
        return play(player, definition, frame.get());
    }

    @Nullable
    public static CutsceneFrame currentFrame(ServerPlayer player) {
        CutsceneSession session = CutsceneSessionManager.get(player.getUUID());
        return session == null ? null : session.frame();
    }

    public static boolean play(ServerPlayer player, String id) {
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        return definition != null && play(player, definition);
    }

    public static int playFor(Iterable<?> players, CutsceneDefinition definition) {
        int count = 0;
        List<ServerPlayer> targets = new ArrayList<>();
        for (Object o : players) {
            if (o instanceof ServerPlayer player) {
                targets.add(player);
            }
        }
        for (ServerPlayer player : targets) {
            if (play(player, definition)) {
                count++;
            }
        }
        return count;
    }

    public static int playNear(ServerLevel level, Vec3 pos, double radius, CutsceneDefinition definition) {
        double radiusSq = radius * radius;
        List<ServerPlayer> targets = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(pos) <= radiusSq) {
                targets.add(player);
            }
        }
        return playFor(targets, definition);
    }

    public static int playAll(ServerLevel level, CutsceneDefinition definition) {
        return playFor(new ArrayList<>(level.players()), definition);
    }

    public static void moveCamera(ServerPlayer player, CutsceneDefinition definition) {
        PacketDistributor.sendToPlayer(player, new MoveCutsceneCameraPayload(definition.build()));
    }

    public static void stop(ServerPlayer player) {
        CutsceneSessionManager.cancel(player, CutsceneSession.REASON_SERVER, true);
    }

    public static int stopNear(ServerLevel level, Vec3 pos, double radius) {
        double radiusSq = radius * radius;
        int count = 0;
        for (ServerPlayer player : new ArrayList<>(level.players())) {
            if (player.position().distanceToSqr(pos) <= radiusSq) {
                stop(player);
                count++;
            }
        }
        return count;
    }

    public static boolean isPlaying(Player player) {
        return CutsceneSessionManager.isPlaying(player);
    }
}
