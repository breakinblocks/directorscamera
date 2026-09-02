package com.breakinblocks.directorscamera.item;

import com.breakinblocks.directorscamera.cutscene.CameraPos;
import com.breakinblocks.directorscamera.cutscene.CutsceneApi;
import com.breakinblocks.directorscamera.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscamera.net.CameraItemActionPayload;
import com.breakinblocks.directorscamera.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DirectorsCameraItem extends Item {
    public static final double MARKER_REACH = 6.0;
    public static final double MARKER_HALF_SIZE = 0.25;
    public static final int[] DURATION_PRESETS = {0, 20, 40, 60, 100, 200, 300};
    private static final Map<UUID, Long> CLEAR_CONFIRM = new HashMap<>();

    public DirectorsCameraItem(Properties properties) {
        super(properties);
    }

    public static CameraRecording getRecording(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.RECORDING.get(), CameraRecording.EMPTY);
    }

    public static void setRecording(ItemStack stack, CameraRecording recording) {
        stack.set(ModDataComponents.RECORDING.get(), recording);
    }

    @Nullable
    public static ItemStack heldCamera(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof DirectorsCameraItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof DirectorsCameraItem) {
            return off;
        }
        return null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        CameraRecording recording = getRecording(stack);
        if (player.isShiftKeyDown()) {
            if (!recording.isEmpty()) {
                int index = recording.keyframes().size();
                setRecording(stack, recording.removeLast());
                actionBar(serverPlayer, Component.translatable("directorscamera.camera.removed", index));
                click(serverPlayer, 0.8F);
            }
            return InteractionResult.CONSUME;
        }
        CameraPos pose = recording.toStored(currentPose(serverPlayer), level, player.position());
        int targeted = findTargetedKeyframe(serverPlayer, recording.worldKeyframes(level, player.position()));
        if (targeted >= 0) {
            setRecording(stack, recording.replace(targeted, pose));
            actionBar(serverPlayer, Component.translatable("directorscamera.camera.updated", targeted + 1));
        } else {
            setRecording(stack, recording.append(pose));
            actionBar(serverPlayer, Component.translatable("directorscamera.camera.recorded", recording.keyframes().size() + 1));
        }
        click(serverPlayer, 1.4F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        CameraRecording recording = getRecording(stack);
        if (recording.isEmpty()) {
            tooltip.accept(Component.translatable("item.directorscamera.directors_camera.empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.accept(Component.translatable("item.directorscamera.directors_camera.keyframes", recording.keyframes().size()).withStyle(ChatFormatting.GRAY));
        String seconds = RecordingExporter.format(recording.effectiveDuration() / 20.0);
        if (recording.duration() > 0) {
            tooltip.accept(Component.translatable("item.directorscamera.directors_camera.duration", seconds).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.directorscamera.directors_camera.duration_auto", seconds).withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable("item.directorscamera.directors_camera.settings", recording.curve().name(), recording.timeEasing().name(), recording.lookEasing().name()).withStyle(ChatFormatting.DARK_GRAY));
        if (recording.isAnchored()) {
            tooltip.accept(Component.translatable("item.directorscamera.directors_camera.anchor", recording.anchor()).withStyle(ChatFormatting.GOLD));
        }
        if (!recording.name().isEmpty()) {
            tooltip.accept(Component.translatable("item.directorscamera.directors_camera.name", recording.name()).withStyle(ChatFormatting.AQUA));
        }
    }

    public static void handleClientAction(ServerPlayer player, int action) {
        ItemStack stack = heldCamera(player);
        if (stack == null) {
            return;
        }
        CameraRecording recording = getRecording(stack);
        switch (action) {
            case CameraItemActionPayload.PREVIEW -> preview(player, recording);
            case CameraItemActionPayload.CLEAR -> clear(player, stack, recording);
            case CameraItemActionPayload.DURATION_UP -> cycleDuration(player, stack, recording, 1);
            case CameraItemActionPayload.DURATION_DOWN -> cycleDuration(player, stack, recording, -1);
            default -> {
            }
        }
    }

    private static void preview(ServerPlayer player, CameraRecording recording) {
        if (recording.keyframes().size() < 2) {
            actionBar(player, Component.translatable("directorscamera.camera.need_two"));
            return;
        }
        CutsceneDefinition definition = recording.toDefinition().id("").skippable(true);
        CutsceneApi.play(player, definition);
    }

    private static void clear(ServerPlayer player, ItemStack stack, CameraRecording recording) {
        long now = player.level().getGameTime();
        Long armed = CLEAR_CONFIRM.get(player.getUUID());
        if (armed != null && now - armed <= 40) {
            CLEAR_CONFIRM.remove(player.getUUID());
            setRecording(stack, CameraRecording.EMPTY.withName(recording.name()));
            actionBar(player, Component.translatable("directorscamera.camera.cleared"));
            click(player, 0.6F);
            return;
        }
        CLEAR_CONFIRM.put(player.getUUID(), now);
        actionBar(player, Component.translatable("directorscamera.camera.confirm_clear"));
    }

    private static void cycleDuration(ServerPlayer player, ItemStack stack, CameraRecording recording, int direction) {
        int index = 0;
        for (int i = 0; i < DURATION_PRESETS.length; i++) {
            if (DURATION_PRESETS[i] == recording.duration()) {
                index = i;
                break;
            }
        }
        index = Math.floorMod(index + direction, DURATION_PRESETS.length);
        int duration = DURATION_PRESETS[index];
        setRecording(stack, recording.withDuration(duration));
        if (duration == 0) {
            actionBar(player, Component.translatable("directorscamera.camera.duration_auto"));
        } else {
            actionBar(player, Component.translatable("directorscamera.camera.duration_set", RecordingExporter.format(duration / 20.0) + "s"));
        }
    }

    public static CameraPos currentPose(Player player) {
        return CameraPos.of(player.getEyePosition(), player.getYRot(), player.getXRot(), 0.0F);
    }

    public static int findTargetedKeyframe(Player player, List<CameraPos> keyframes) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(MARKER_REACH));
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < keyframes.size(); i++) {
            Vec3 p = keyframes.get(i).pos();
            AABB box = new AABB(p.x - MARKER_HALF_SIZE, p.y - MARKER_HALF_SIZE, p.z - MARKER_HALF_SIZE, p.x + MARKER_HALF_SIZE, p.y + MARKER_HALF_SIZE, p.z + MARKER_HALF_SIZE);
            if (box.contains(eye)) {
                continue;
            }
            var hit = box.clip(eye, end);
            if (hit.isPresent()) {
                double d = hit.get().distanceToSqr(eye);
                if (d < bestDist) {
                    bestDist = d;
                    best = i;
                }
            }
        }
        return best;
    }

    private static void actionBar(ServerPlayer player, Component message) {
        player.sendOverlayMessage(message);
    }

    private static void click(ServerPlayer player, float pitch) {
        player.connection.send(new ClientboundSoundPacket(SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 0.5F, pitch, player.getRandom().nextLong()));
    }
}
