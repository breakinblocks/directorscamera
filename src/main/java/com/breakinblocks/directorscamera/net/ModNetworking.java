package com.breakinblocks.directorscamera.net;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.client.ClientPayloadHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID)
public class ModNetworking {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(DirectorsCamera.MOD_ID).versioned("1").optional();
        boolean client = FMLEnvironment.getDist().isClient();

        registrar.playToClient(StartCutscenePayload.TYPE, StartCutscenePayload.STREAM_CODEC, client ? ClientPayloadHandler::handleStart : ModNetworking::ignore);
        registrar.playToClient(MoveCutsceneCameraPayload.TYPE, MoveCutsceneCameraPayload.STREAM_CODEC, client ? ClientPayloadHandler::handleMove : ModNetworking::ignore);
        registrar.playToClient(StopCutscenePayload.TYPE, StopCutscenePayload.STREAM_CODEC, client ? ClientPayloadHandler::handleStop : ModNetworking::ignore);
        registrar.playToClient(ClipboardPayload.TYPE, ClipboardPayload.STREAM_CODEC, client ? ClientPayloadHandler::handleClipboard : ModNetworking::ignore);
        registrar.playToClient(DefaultShakePayload.TYPE, DefaultShakePayload.STREAM_CODEC, client ? ClientPayloadHandler::handleDefaultShake : ModNetworking::ignore);
        registrar.playToClient(PositionedShakePayload.TYPE, PositionedShakePayload.STREAM_CODEC, client ? ClientPayloadHandler::handlePositionedShake : ModNetworking::ignore);
        registrar.playToClient(SyncAnimationsPayload.TYPE, SyncAnimationsPayload.STREAM_CODEC, SyncAnimationsPayload::handle);

        registrar.playToServer(CutsceneStatePayload.TYPE, CutsceneStatePayload.STREAM_CODEC, CutsceneStatePayload::handle);
        registrar.playToServer(CameraItemActionPayload.TYPE, CameraItemActionPayload.STREAM_CODEC, CameraItemActionPayload::handle);
    }

    private static <T extends CustomPacketPayload> void ignore(T payload, IPayloadContext context) {
    }
}
