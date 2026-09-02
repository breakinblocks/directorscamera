package com.breakinblocks.directorscut.kubejs;

import com.breakinblocks.directorscut.cutscene.CameraPathBuilder;
import com.breakinblocks.directorscut.cutscene.CameraPos;
import com.breakinblocks.directorscut.cutscene.CutsceneApi;
import com.breakinblocks.directorscut.anchor.AnchorIndex;
import com.breakinblocks.directorscut.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscut.cutscene.CutsceneFrame;
import com.breakinblocks.directorscut.cutscene.FrameSource;
import com.breakinblocks.directorscut.cutscene.CutsceneRegistry;
import com.breakinblocks.directorscut.cutscene.CutscenePresets;
import com.breakinblocks.directorscut.expression.ExpressionContext;
import com.breakinblocks.directorscut.expression.RpnExpression;
import com.breakinblocks.directorscut.keyframe.Animation;
import com.breakinblocks.directorscut.keyframe.AnimationRegistry;
import com.breakinblocks.directorscut.keyframe.AnimationSystem;
import com.breakinblocks.directorscut.keyframe.AnimationTicker;
import com.breakinblocks.directorscut.keyframe.SimplePose;
import com.breakinblocks.directorscut.item.CameraRecording;
import com.breakinblocks.directorscut.item.DirectorsCameraItem;
import com.breakinblocks.directorscut.net.DefaultShakePayload;
import com.breakinblocks.directorscut.net.PositionedShakePayload;
import com.breakinblocks.directorscut.shake.ShakeData;
import com.breakinblocks.directorscut.util.MathUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DirectorsCutBindings {
    public static final CutscenePresets presets = CutscenePresets.INSTANCE;
    public static final Map<String, Integer> TIMING = Map.of(
        "INSTANT", 1,
        "VERY_FAST", 20,
        "FAST", 40,
        "NORMAL", 60,
        "SLOW", 100,
        "VERY_SLOW", 200,
        "CINEMATIC", 300
    );
    public static final Map<String, Double> SPEED = Map.of(
        "CRAWL", 1.0,
        "WALK", 4.3,
        "SPRINT", 5.6,
        "FLY", 10.0,
        "FAST_FLY", 20.0,
        "WARP", 50.0
    );

    private DirectorsCutBindings() {
    }

    public static CutsceneDefinition cutscene() {
        return CutsceneDefinition.create();
    }

    public static CameraPathBuilder path() {
        return cutscene().getPath();
    }

    public static CameraPos keyframe(double x, double y, double z) {
        return CameraPos.of(x, y, z, 0.0F, 0.0F, 0.0F);
    }

    public static CameraPos keyframe(double x, double y, double z, double yaw, double pitch) {
        return CameraPos.of(x, y, z, (float) yaw, (float) pitch, 0.0F);
    }

    public static CameraPos keyframe(double x, double y, double z, double yaw, double pitch, double roll) {
        return CameraPos.of(x, y, z, (float) yaw, (float) pitch, (float) roll);
    }

    public static CameraPos keyframeLookingAt(double x, double y, double z, double targetX, double targetY, double targetZ) {
        return CameraPos.lookingAt(new Vec3(x, y, z), new Vec3(targetX, targetY, targetZ), 0.0F);
    }

    public static Vec3 vec(double x, double y, double z) {
        return new Vec3(x, y, z);
    }

    public static Vec3 vecOf(Entity entity) {
        return entity.position();
    }

    public static Vec3 eyesOf(Entity entity) {
        return entity.getEyePosition();
    }

    public static Vec3 offset(Vec3 origin, double yaw, double pitch, double distance) {
        return origin.add(MathUtil.directionFromAngles((float) yaw, (float) pitch).scale(distance));
    }

    public static int durationFromSpeed(CameraPathBuilder path, double blocksPerSecond) {
        if (path.getKeyframeCount() < 2) {
            throw new IllegalArgumentException("Path must have at least 2 keyframes");
        }
        if (blocksPerSecond <= 0) {
            throw new IllegalArgumentException("Speed must be positive");
        }
        return (int) Math.ceil(path.pathLength() / blocksPerSecond * 20);
    }

    public static int durationFromSpeed(CutsceneDefinition cutscene, double blocksPerSecond) {
        return durationFromSpeed(cutscene.getPath(), blocksPerSecond);
    }

    public static boolean play(Player player, CutsceneDefinition cutscene) {
        return cutscene.play(player);
    }

    public static boolean play(Player player, String id) {
        return player instanceof ServerPlayer serverPlayer && CutsceneApi.play(serverPlayer, id);
    }

    public static int playFor(Iterable<?> players, CutsceneDefinition cutscene) {
        return CutsceneApi.playFor(players, cutscene);
    }

    public static int playNear(Level level, double x, double y, double z, double radius, CutsceneDefinition cutscene) {
        return cutscene.playNear(level, x, y, z, radius);
    }

    public static int playAll(Level level, CutsceneDefinition cutscene) {
        return cutscene.playForAll(level);
    }

    public static void stop(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CutsceneApi.stop(serverPlayer);
        }
    }

    public static int stopNear(Level level, double x, double y, double z, double radius) {
        if (level instanceof ServerLevel serverLevel) {
            return CutsceneApi.stopNear(serverLevel, new Vec3(x, y, z), radius);
        }
        return 0;
    }

    public static boolean isPlaying(Player player) {
        return CutsceneApi.isPlaying(player);
    }

    @Nullable
    public static CutsceneDefinition get(String id) {
        return CutsceneRegistry.get(id);
    }

    public static Set<String> ids() {
        return CutsceneRegistry.ids();
    }

    public static CutsceneDefinition register(String id, CutsceneDefinition cutscene) {
        CutsceneRegistry.registerScript(id, cutscene);
        return cutscene;
    }

    @Nullable
    public static CutsceneDefinition recordingOf(ItemStack stack) {
        if (!(stack.getItem() instanceof DirectorsCameraItem)) {
            return null;
        }
        return DirectorsCameraItem.getRecording(stack).toDefinition();
    }

    public static void setRecording(ItemStack stack, CutsceneDefinition cutscene) {
        if (stack.getItem() instanceof DirectorsCameraItem) {
            DirectorsCameraItem.setRecording(stack, CameraRecording.fromDefinition(cutscene));
        }
    }

    @Nullable
    public static Animation animation(String id) {
        return AnimationRegistry.get(id);
    }

    public static Set<String> animationIds() {
        return AnimationRegistry.ids();
    }

    public static Animation parseAnimation(String id, String json) {
        return AnimationRegistry.parseAndRegister(ResourceLocation.parse(id), json, true);
    }

    public static Animation parseAnimation(String id, String json, boolean bedrockConventions) {
        return AnimationRegistry.parseAndRegister(ResourceLocation.parse(id), json, bedrockConventions);
    }

    public static AnimationTicker.Builder ticker(String id) {
        Animation animation = AnimationRegistry.get(id);
        if (animation == null) {
            throw new IllegalArgumentException("Unknown animation: " + id);
        }
        return AnimationTicker.builder(animation);
    }

    public static AnimationTicker.Builder ticker(Animation animation) {
        return AnimationTicker.builder(animation);
    }

    public static AnimationSystem animationSystem() {
        return new AnimationSystem();
    }

    public static SimplePose pose() {
        return new SimplePose();
    }

    public static RpnExpression expression(String text) {
        return RpnExpression.parse(text);
    }

    public static float evaluate(String text, @Nullable Map<String, Object> variables) {
        ExpressionContext context = new ExpressionContext();
        if (variables != null) {
            variables.forEach((k, v) -> {
                if (v instanceof Number n) {
                    context.addVariable(k, n.floatValue());
                }
            });
        }
        return RpnExpression.parse(text).compute(context);
    }

    public static boolean playAnchored(Player player, CutsceneDefinition cutscene, String anchorId) {
        return player instanceof ServerPlayer serverPlayer && CutsceneApi.playAnchored(serverPlayer, cutscene, anchorId);
    }

    public static boolean playAnchored(Player player, String id, String anchorId) {
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        return definition != null && playAnchored(player, definition, anchorId);
    }

    public static boolean playAnchored(Player player, CutsceneDefinition cutscene, double x, double y, double z, double yaw) {
        return player instanceof ServerPlayer serverPlayer && CutsceneApi.play(serverPlayer, cutscene, CutsceneFrame.of(x, y, z, yaw));
    }

    public static boolean playAnchored(Player player, String id, double x, double y, double z, double yaw) {
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        return definition != null && playAnchored(player, definition, x, y, z, yaw);
    }

    public static CutsceneFrame frame(double x, double y, double z, double yaw) {
        return CutsceneFrame.of(x, y, z, yaw);
    }

    public static FrameSource anchorSource(String anchorId, double maxDistance) {
        return FrameSource.anchor(anchorId, maxDistance);
    }

    @Nullable
    public static CutsceneFrame frameOf(Player player) {
        return player instanceof ServerPlayer serverPlayer ? CutsceneApi.currentFrame(serverPlayer) : null;
    }

    public static void registerAnchor(Level level, String id, double x, double y, double z, double yaw) {
        AnchorIndex.registerVirtual(level, id, CutsceneFrame.of(x, y, z, yaw));
    }

    public static void clearVirtualAnchors(Level level, String id) {
        AnchorIndex.clearVirtual(level, id);
    }

    public static List<CutsceneFrame> anchors(Level level, String id) {
        return AnchorIndex.all(level, id);
    }

    @Nullable
    public static CutsceneFrame nearestAnchor(Level level, String id, double x, double y, double z) {
        return AnchorIndex.nearest(level, id, new Vec3(x, y, z), 0).orElse(null);
    }

    public static Set<String> anchorIds(Level level) {
        return AnchorIndex.ids(level);
    }

    public static ShakeData shakeData(int inTime, int stayTime, int outTime, double amplitude, double frequency) {
        return new ShakeData(inTime, stayTime, outTime, (float) amplitude, (float) frequency);
    }

    public static void shake(Level level, double x, double y, double z, double radius, ShakeData data) {
        if (level instanceof ServerLevel serverLevel) {
            DefaultShakePayload.send(serverLevel, new Vec3(x, y, z), radius, data);
        }
    }

    public static void positionedShake(Level level, double x, double y, double z, double radius, ShakeData data) {
        if (level instanceof ServerLevel serverLevel) {
            PositionedShakePayload.send(serverLevel, data, new Vec3(x, y, z), radius);
        }
    }
}
