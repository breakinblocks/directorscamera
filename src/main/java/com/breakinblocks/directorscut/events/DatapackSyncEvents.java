package com.breakinblocks.directorscut.events;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.net.SyncAnimationsPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID)
public class DatapackSyncEvents {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SyncAnimationsPayload payload = SyncAnimationsPayload.current();
        ServerPlayer player = event.getPlayer();
        if (player != null) {
            PacketDistributor.sendToPlayer(player, payload);
        } else {
            PacketDistributor.sendToAllPlayers(payload);
        }
    }
}
