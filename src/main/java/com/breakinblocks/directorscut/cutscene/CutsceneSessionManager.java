package com.breakinblocks.directorscut.cutscene;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.config.DirectorsCutConfig;
import com.breakinblocks.directorscut.net.StopCutscenePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public final class CutsceneSessionManager {
    private static final Map<UUID, CutsceneSession> SESSIONS = new HashMap<>();
    private static final Map<UUID, Integer> WAITING = new HashMap<>();

    private CutsceneSessionManager() {
    }

    public static CutsceneSession start(ServerPlayer player, CutsceneDefinition definition, CutsceneData data) {
        return start(player, definition, data, null);
    }

    public static CutsceneSession start(ServerPlayer player, CutsceneDefinition definition, CutsceneData data, @Nullable CutsceneFrame frame) {
        CutsceneSession previous = SESSIONS.remove(player.getUUID());
        if (previous != null && !previous.finished()) {
            previous.cancel(player, CutsceneSession.REASON_SERVER);
            CutsceneHooks.Holder.get().ended(player, previous.id(), CutsceneSession.REASON_SERVER);
        }
        CutsceneSession session = new CutsceneSession(player, definition, data, frame);
        SESSIONS.put(player.getUUID(), session);
        WAITING.put(player.getUUID(), 0);
        return session;
    }

    @Nullable
    public static CutsceneSession get(UUID playerId) {
        return SESSIONS.get(playerId);
    }

    public static boolean isPlaying(Player player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    public static void handleState(ServerPlayer player, int state, String id) {
        CutsceneSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return;
        }
        switch (state) {
            case 0 -> {
                session.markStarted();
                WAITING.remove(player.getUUID());
                CutsceneHooks.Holder.get().started(player, session.id());
            }
            case 1 -> finish(player, session);
            case 2 -> cancel(player, CutsceneSession.REASON_PLAYER, false);
            default -> cancel(player, CutsceneSession.REASON_SERVER, false);
        }
    }

    public static void cancel(ServerPlayer player, String reason, boolean sendStop) {
        CutsceneSession session = SESSIONS.remove(player.getUUID());
        WAITING.remove(player.getUUID());
        if (sendStop) {
            PacketDistributor.sendToPlayer(player, new StopCutscenePayload());
        }
        if (session == null) {
            return;
        }
        session.cancel(player, reason);
        CutsceneHooks.Holder.get().ended(player, session.id(), reason);
    }

    private static void finish(ServerPlayer player, CutsceneSession session) {
        SESSIONS.remove(player.getUUID());
        WAITING.remove(player.getUUID());
        session.finish(player);
        CutsceneHooks.Holder.get().ended(player, session.id(), CutsceneSession.REASON_FINISHED);
        CutsceneDefinition next = session.definition().getNext();
        if (next != null) {
            CutsceneApi.play(player, next, session.frame());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        int timeout = DirectorsCutConfig.SERVER.startTimeoutTicks.get();
        for (CutsceneSession session : new ArrayList<>(SESSIONS.values())) {
            ServerPlayer player = server.getPlayerList().getPlayer(session.playerId());
            if (player == null) {
                SESSIONS.remove(session.playerId());
                WAITING.remove(session.playerId());
                continue;
            }
            if (!session.started()) {
                int waited = WAITING.merge(session.playerId(), 1, Integer::sum);
                if (waited > timeout) {
                    DirectorsCut.LOGGER.warn("Cutscene {} for {} never confirmed start, dropping session", session.id(), player.getGameProfile().getName());
                    cancel(player, CutsceneSession.REASON_TIMEOUT, true);
                }
                continue;
            }
            session.advance(player);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancel(player, CutsceneSession.REASON_DISCONNECT, false);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isPlaying(player)) {
            cancel(player, CutsceneSession.REASON_SERVER, true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isPlaying(player)) {
            cancel(player, CutsceneSession.REASON_SERVER, true);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SESSIONS.clear();
        WAITING.clear();
    }
}
