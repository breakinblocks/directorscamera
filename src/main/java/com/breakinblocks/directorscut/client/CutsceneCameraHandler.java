package com.breakinblocks.directorscut.client;

import com.breakinblocks.directorscut.DirectorsCut;
import com.breakinblocks.directorscut.camera.ClientCameraEntity;
import com.breakinblocks.directorscut.config.DirectorsCutConfig;
import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.cutscene.CutsceneData;
import com.breakinblocks.directorscut.cutscene.StopMode;
import com.breakinblocks.directorscut.mixin.MouseHandlerAccessor;
import com.breakinblocks.directorscut.net.CutsceneStatePayload;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = DirectorsCut.MOD_ID, value = Dist.CLIENT)
public final class CutsceneCameraHandler {
    public static final int SKIP_HOLD_TICKS = 30;
    public static final int HINT_FADE_TICKS = 60;
    public static final int HINT_RELEASE_TICKS = 10;
    public static final float HINT_REST_ALPHA = 0.35F;

    @Nullable
    private static ClientCameraEntity camera;
    @Nullable
    private static CutsceneExecutor executor;
    @Nullable
    private static CameraType previousCameraType;
    private static int holdTicks;
    private static int holdTicksPrev;
    private static int activeTicks;
    private static int releaseTicks;
    private static float restAlpha = 1.0F;
    private static float restAlphaPrev = 1.0F;

    private CutsceneCameraHandler() {
    }

    public enum StopReason {
        FINISHED(CutsceneStatePayload.FINISHED),
        PLAYER(CutsceneStatePayload.SKIPPED),
        SERVER(CutsceneStatePayload.STOPPED);

        public final int state;

        StopReason(int state) {
            this.state = state;
        }
    }

    public static boolean isCutsceneActive() {
        return camera != null && executor != null;
    }

    @Nullable
    public static CutsceneExecutor getExecutor() {
        return executor;
    }

    public static boolean isSkippable() {
        return executor != null && executor.getData().isSkippable();
    }

    public static float holdProgress(float partialTick) {
        float ticks = holdTicksPrev + (holdTicks - holdTicksPrev) * partialTick;
        return Math.max(0.0F, Math.min(1.0F, ticks / SKIP_HOLD_TICKS));
    }

    public static float hintAlpha(float partialTick) {
        return restAlphaPrev + (restAlpha - restAlphaPrev) * partialTick;
    }

    public static boolean isHolding() {
        return holdTicks > 0;
    }

    public static void startCutscene(CutsceneData data) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (data.keyframes().isEmpty()) {
            DirectorsCut.LOGGER.warn("Ignoring cutscene {} with no keyframes", data.id());
            return;
        }
        if (isCutsceneActive() && executor != null) {
            CutsceneSoundPlayer.endAll();
            ClientCutsceneHooks.Holder.get().ended(executor.getData().id(), "replaced");
        }
        if (DirectorsCutConfig.CLIENT.closeScreenOnStart.get() && minecraft.screen != null) {
            minecraft.setScreen(null);
        }
        CameraPos first = data.keyframes().getFirst();
        if (camera == null || minecraft.getCameraEntity() != camera) {
            camera = new ClientCameraEntity(minecraft.level, minecraft.player);
            previousCameraType = minecraft.options.getCameraType();
        }
        camera.setViewer(minecraft.player);
        camera.moveTo(first.pos());
        camera.setYRot(first.yaw());
        camera.setXRot(first.pitch());
        minecraft.setCameraEntity(camera);
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        executor = new CutsceneExecutor(data);
        holdTicks = 0;
        holdTicksPrev = 0;
        activeTicks = 0;
        releaseTicks = 0;
        restAlpha = 1.0F;
        restAlphaPrev = 1.0F;
        PacketDistributor.sendToServer(new CutsceneStatePayload(CutsceneStatePayload.STARTED, data.id()));
        ClientCutsceneHooks.Holder.get().started(data.id());
    }

    public static void moveCamera(CutsceneData data) {
        if (!isCutsceneActive() || executor == null) {
            return;
        }
        Vec3 pos = executor.currentPosition();
        float[] rot = executor.rotation(0.0F);
        List<CameraPos> frames = new ArrayList<>(data.keyframes());
        frames.addFirst(CameraPos.of(pos, rot[0], rot[1], rot[2]));
        executor = new CutsceneExecutor(data.withKeyframes(frames));
    }

    public static void stopCutscene(StopReason reason) {
        if (!isCutsceneActive() || executor == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        String id = executor.getData().id();
        CutsceneSoundPlayer.endAll();
        camera = null;
        executor = null;
        holdTicks = 0;
        holdTicksPrev = 0;
        MouseHandlerAccessor mouse = (MouseHandlerAccessor) minecraft.mouseHandler;
        mouse.directorscut$setAccumulatedDX(0.0);
        mouse.directorscut$setAccumulatedDY(0.0);
        if (minecraft.player != null) {
            minecraft.setCameraEntity(minecraft.player);
        }
        if (previousCameraType != null) {
            minecraft.options.setCameraType(previousCameraType);
            previousCameraType = null;
        }
        PacketDistributor.sendToServer(new CutsceneStatePayload(reason.state, id));
        ClientCutsceneHooks.Holder.get().ended(id, reason.name().toLowerCase());
    }

    public static void nullifyInput(Input input) {
        input.leftImpulse = 0.0F;
        input.forwardImpulse = 0.0F;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CutsceneSoundPlayer.endAll();
        camera = null;
        executor = null;
        previousCameraType = null;
        holdTicks = 0;
        holdTicksPrev = 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused()) {
            return;
        }
        LocalPlayer player = minecraft.player;
        if (player == null) {
            if (isCutsceneActive()) {
                stopCutscene(StopReason.SERVER);
            }
            return;
        }
        boolean endPressed = false;
        while (DirectorsCutClient.END_CUTSCENE.consumeClick()) {
            endPressed = true;
        }
        if (!isCutsceneActive() || executor == null || camera == null) {
            return;
        }
        nullifyInput(player.input);
        StopMode stopMode = executor.getData().stopMode();
        if (stopMode == StopMode.PLAYER && endPressed) {
            stopCutscene(StopReason.PLAYER);
            return;
        }
        if (tickHoldToSkip(minecraft)) {
            return;
        }
        if (minecraft.options.getCameraType() != CameraType.FIRST_PERSON) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        }
        if (minecraft.getCameraEntity() != camera) {
            minecraft.setCameraEntity(camera);
        }
        executor.tick(camera);
        activeTicks++;
        if (executor.hasEnded() && stopMode == StopMode.AUTOMATIC) {
            stopCutscene(StopReason.FINISHED);
        }
    }

    private static boolean tickHoldToSkip(Minecraft minecraft) {
        holdTicksPrev = holdTicks;
        restAlphaPrev = restAlpha;
        boolean skippable = isSkippable();
        boolean down = skippable && minecraft.screen == null && minecraft.options.keyJump.isDown();
        if (down) {
            holdTicks++;
            releaseTicks = 0;
            restAlpha = 1.0F;
        } else {
            if (holdTicks > 0) {
                releaseTicks = HINT_RELEASE_TICKS;
            }
            holdTicks = 0;
            float target = HINT_REST_ALPHA;
            if (releaseTicks > 0) {
                releaseTicks--;
                restAlpha = restAlpha + (target - restAlpha) / (releaseTicks + 1);
            } else if (activeTicks < HINT_FADE_TICKS) {
                restAlpha = 1.0F - (1.0F - target) * ((float) activeTicks / HINT_FADE_TICKS);
            } else {
                restAlpha = target;
            }
        }
        if (holdTicks >= SKIP_HOLD_TICKS) {
            holdTicks = 0;
            holdTicksPrev = 0;
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2F));
            stopCutscene(StopReason.PLAYER);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        if (isCutsceneActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (isCutsceneActive() && !event.getName().equals(SkipBarLayer.ID)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (isCutsceneActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (isCutsceneActive()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!isCutsceneActive() || executor == null) {
            return;
        }
        float[] rot = executor.rotation((float) event.getPartialTick());
        event.setYaw(rot[0]);
        event.setPitch(rot[1]);
        event.setRoll(rot[2]);
    }
}
