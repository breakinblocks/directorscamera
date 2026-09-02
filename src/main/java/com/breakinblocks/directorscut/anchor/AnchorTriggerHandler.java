package com.breakinblocks.directorscut.anchor;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.cutscene.CutsceneApi;
import com.breakinblocks.directorscut.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscut.cutscene.CutsceneRegistry;
import com.breakinblocks.directorscut.cutscene.CutsceneSessionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public final class AnchorTriggerHandler {
    private static final int CHECK_INTERVAL = 10;

    private AnchorTriggerHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % CHECK_INTERVAL != 0) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.players().isEmpty()) {
                continue;
            }
            for (AnchorBlockEntity anchor : new ArrayList<>(AnchorIndex.triggers(level))) {
                if (anchor.isRemoved() || !anchor.hasTrigger()) {
                    continue;
                }
                check(level, anchor);
            }
        }
    }

    private static void check(ServerLevel level, AnchorBlockEntity anchor) {
        Vec3 center = anchor.getFrame().origin();
        double radiusSq = anchor.getTriggerRadius() * anchor.getTriggerRadius();
        long time = level.getGameTime();
        for (ServerPlayer player : new ArrayList<>(level.players())) {
            if (player.isSpectator() || CutsceneSessionManager.isPlaying(player)) {
                continue;
            }
            if (player.position().distanceToSqr(center) > radiusSq || !anchor.canTrigger(player.getUUID(), time)) {
                continue;
            }
            CutsceneDefinition definition = CutsceneRegistry.get(anchor.getCutsceneId());
            if (definition == null) {
                DirectorsCut.LOGGER.warn("Anchor at {} references unknown cutscene {}", anchor.getBlockPos(), anchor.getCutsceneId());
                anchor.markTriggered(player.getUUID(), time);
                continue;
            }
            anchor.markTriggered(player.getUUID(), time);
            CutsceneApi.play(player, definition, anchor.getFrame());
        }
    }
}
