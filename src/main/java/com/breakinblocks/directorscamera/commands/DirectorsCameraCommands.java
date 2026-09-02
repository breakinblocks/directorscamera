package com.breakinblocks.directorscamera.commands;

import com.breakinblocks.directorscamera.DirectorsCamera;
import com.breakinblocks.directorscamera.anchor.AnchorBlock;
import com.breakinblocks.directorscamera.anchor.AnchorBlockEntity;
import com.breakinblocks.directorscamera.anchor.AnchorIndex;
import com.breakinblocks.directorscamera.cutscene.CutsceneFiles;
import com.breakinblocks.directorscamera.cutscene.CutsceneFrame;
import com.breakinblocks.directorscamera.curves.CurveType;
import com.breakinblocks.directorscamera.curves.EasingType;
import com.breakinblocks.directorscamera.cutscene.CameraPos;
import com.breakinblocks.directorscamera.cutscene.CutsceneApi;
import com.breakinblocks.directorscamera.cutscene.CutsceneDefinition;
import com.breakinblocks.directorscamera.cutscene.CutsceneRegistry;
import com.breakinblocks.directorscamera.cutscene.RuntimeCutsceneStore;
import com.breakinblocks.directorscamera.item.CameraRecording;
import com.breakinblocks.directorscamera.item.DirectorsCameraItem;
import com.breakinblocks.directorscamera.item.RecordingExporter;
import com.breakinblocks.directorscamera.net.ClipboardPayload;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@EventBusSubscriber(modid = DirectorsCamera.MOD_ID)
public class DirectorsCameraCommands {
    private static final Map<UUID, List<CameraPos>> SCRATCH = new HashMap<>();
    private static final SuggestionProvider<CommandSourceStack> IDS = (context, builder) -> SharedSuggestionProvider.suggest(CutsceneRegistry.ids(), builder);
    private static final SuggestionProvider<CommandSourceStack> ANCHORS = (context, builder) -> SharedSuggestionProvider.suggest(AnchorIndex.ids(context.getSource().getLevel()), builder);
    private static final SuggestionProvider<CommandSourceStack> IMPORT_FILES = (context, builder) -> SharedSuggestionProvider.suggest(CutsceneFiles.importNames(), builder);
    private static final SuggestionProvider<CommandSourceStack> CURVES = (context, builder) -> SharedSuggestionProvider.suggest(List.of("LINEAR", "CATMULLROM"), builder);
    private static final SuggestionProvider<CommandSourceStack> EASINGS = (context, builder) -> SharedSuggestionProvider.suggest(List.of("LINEAR", "EASE_IN", "EASE_OUT", "EASE_IN_OUT"), builder);

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("directorscamera")
            .then(Commands.literal("play").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("id", CutsceneIdArgument.id()).suggests(IDS)
                    .executes(ctx -> play(ctx, List.of(ctx.getSource().getPlayerOrException())))
                    .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> play(ctx, EntityArgument.getPlayers(ctx, "targets"))))))
            .then(Commands.literal("stop").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> stop(ctx, List.of(ctx.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(ctx -> stop(ctx, EntityArgument.getPlayers(ctx, "targets")))))
            .then(Commands.literal("list").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(DirectorsCameraCommands::list))
            .then(Commands.literal("export").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("all")
                    .executes(ctx -> exportAll(ctx, false))
                    .then(Commands.literal("json").executes(ctx -> exportAll(ctx, false)))
                    .then(Commands.literal("script").executes(ctx -> exportAll(ctx, true))))
                .then(Commands.argument("id", CutsceneIdArgument.id()).suggests(IDS)
                    .executes(ctx -> exportOne(ctx, false))
                    .then(Commands.literal("json").executes(ctx -> exportOne(ctx, false)))
                    .then(Commands.literal("script").executes(ctx -> exportOne(ctx, true)))))
            .then(Commands.literal("import").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("all").executes(DirectorsCameraCommands::importAll))
                .then(Commands.argument("file", StringArgumentType.greedyString()).suggests(IMPORT_FILES).executes(DirectorsCameraCommands::importOne)))
            .then(Commands.literal("playanchored").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("id", CutsceneIdArgument.id()).suggests(IDS)
                    .then(Commands.argument("anchor", StringArgumentType.string()).suggests(ANCHORS)
                        .executes(ctx -> playAnchored(ctx, List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes(ctx -> playAnchored(ctx, EntityArgument.getPlayers(ctx, "targets")))))))
            .then(Commands.literal("anchor").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("set").then(Commands.argument("id", StringArgumentType.string()).executes(DirectorsCameraCommands::anchorSet)))
                .then(Commands.literal("info").executes(DirectorsCameraCommands::anchorInfo))
                .then(Commands.literal("list").executes(DirectorsCameraCommands::anchorList))
                .then(Commands.literal("reset").executes(DirectorsCameraCommands::anchorReset))
                .then(Commands.literal("trigger")
                    .then(Commands.literal("clear").executes(DirectorsCameraCommands::anchorTriggerClear))
                    .then(Commands.argument("cutscene", CutsceneIdArgument.id()).suggests(IDS)
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.5))
                            .executes(ctx -> anchorTrigger(ctx, true, 0))
                            .then(Commands.argument("once", BoolArgumentType.bool())
                                .executes(ctx -> anchorTrigger(ctx, BoolArgumentType.getBool(ctx, "once"), 0))
                                .then(Commands.argument("cooldown", IntegerArgumentType.integer(0))
                                    .executes(ctx -> anchorTrigger(ctx, BoolArgumentType.getBool(ctx, "once"), IntegerArgumentType.getInteger(ctx, "cooldown")))))))))
            .then(Commands.literal("delete").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("id", CutsceneIdArgument.id()).suggests(IDS).executes(DirectorsCameraCommands::delete)))
            .then(Commands.literal("fix")
                .executes(DirectorsCameraCommands::fix)
                .then(Commands.literal("cutscene").executes(DirectorsCameraCommands::fix)))
            .then(Commands.literal("capture")
                .executes(DirectorsCameraCommands::captureSingle)
                .then(Commands.literal("start").executes(DirectorsCameraCommands::captureStart))
                .then(Commands.literal("add").executes(DirectorsCameraCommands::captureAdd))
                .then(Commands.literal("print").executes(DirectorsCameraCommands::capturePrint))
                .then(Commands.literal("clear").executes(DirectorsCameraCommands::captureClear)))
            .then(Commands.literal("camera").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("export")
                    .executes(ctx -> export(ctx, false))
                    .then(Commands.literal("json").executes(ctx -> export(ctx, true))))
                .then(Commands.literal("save").then(Commands.argument("id", CutsceneIdArgument.id()).executes(DirectorsCameraCommands::save)))
                .then(Commands.literal("load").then(Commands.argument("id", CutsceneIdArgument.id()).suggests(IDS).executes(DirectorsCameraCommands::load)))
                .then(Commands.literal("name").then(Commands.argument("id", CutsceneIdArgument.id()).executes(DirectorsCameraCommands::name)))
                .then(Commands.literal("anchor")
                    .then(Commands.literal("clear").executes(DirectorsCameraCommands::cameraUnanchor))
                    .then(Commands.argument("id", StringArgumentType.string()).suggests(ANCHORS).executes(DirectorsCameraCommands::cameraAnchor)))
                .then(Commands.literal("roll").then(Commands.argument("index", IntegerArgumentType.integer(1))
                    .then(Commands.argument("degrees", FloatArgumentType.floatArg()).executes(DirectorsCameraCommands::roll))))
                .then(Commands.literal("insert").then(Commands.argument("index", IntegerArgumentType.integer(1)).executes(DirectorsCameraCommands::insert)))
                .then(Commands.literal("curve").then(Commands.argument("value", StringArgumentType.word()).suggests(CURVES)
                    .executes(ctx -> setting(ctx, "curve", (r, v) -> r.withCurve(CurveType.parse(v))))))
                .then(Commands.literal("easing").then(Commands.argument("value", StringArgumentType.word()).suggests(EASINGS)
                    .executes(ctx -> setting(ctx, "easing", (r, v) -> r.withTimeEasing(EasingType.parse(v)).withLookEasing(EasingType.parse(v))))))
                .then(Commands.literal("timeEasing").then(Commands.argument("value", StringArgumentType.word()).suggests(EASINGS)
                    .executes(ctx -> setting(ctx, "timeEasing", (r, v) -> r.withTimeEasing(EasingType.parse(v))))))
                .then(Commands.literal("lookEasing").then(Commands.argument("value", StringArgumentType.word()).suggests(EASINGS)
                    .executes(ctx -> setting(ctx, "lookEasing", (r, v) -> r.withLookEasing(EasingType.parse(v))))))
                .then(Commands.literal("duration").then(Commands.argument("value", StringArgumentType.word())
                    .executes(ctx -> setting(ctx, "duration", (r, v) -> r.withDuration(v.equalsIgnoreCase("auto") ? 0 : Integer.parseInt(v))))))));
    }

    private static int play(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        String id = CutsceneIdArgument.getId(ctx, "id");
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        if (definition == null) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.unknown", id));
            return 0;
        }
        int count = CutsceneApi.playFor(targets, definition);
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.played", definition.getId(), count), true);
        return count;
    }

    private static int exportOne(CommandContext<CommandSourceStack> ctx, boolean script) {
        String id = CutsceneIdArgument.getId(ctx, "id");
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        if (definition == null) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.unknown", id));
            return 0;
        }
        return exportDefinition(ctx, definition, script) ? 1 : 0;
    }

    private static int exportAll(CommandContext<CommandSourceStack> ctx, boolean script) {
        int count = 0;
        for (String id : CutsceneRegistry.ids()) {
            CutsceneDefinition definition = CutsceneRegistry.get(id);
            if (definition != null && exportDefinition(ctx, definition, script)) {
                count++;
            }
        }
        int exported = count;
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.exported_all", exported, CutsceneFiles.exportRoot().toString()), true);
        return count;
    }

    private static boolean exportDefinition(CommandContext<CommandSourceStack> ctx, CutsceneDefinition definition, boolean script) {
        try {
            Path path = script ? CutsceneFiles.exportScript(definition) : CutsceneFiles.exportJson(definition);
            ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.exported", definition.getId(), path.toString()), false);
            return true;
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.export_failed", definition.getId(), e.getMessage()));
            return false;
        }
    }

    private static int importOne(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "file");
        Path file;
        try {
            file = CutsceneFiles.resolveImport(name);
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        }
        return importFile(ctx, file) ? 1 : 0;
    }

    private static int importAll(CommandContext<CommandSourceStack> ctx) {
        int count = 0;
        try {
            for (Path file : CutsceneFiles.importFiles()) {
                if (importFile(ctx, file)) {
                    count++;
                }
            }
        } catch (IOException e) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.import_failed", CutsceneFiles.importRoot().toString(), e.getMessage()));
            return 0;
        }
        int imported = count;
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.imported_all", imported, CutsceneFiles.importRoot().toString()), true);
        return count;
    }

    private static boolean importFile(CommandContext<CommandSourceStack> ctx, Path file) {
        if (!Files.isRegularFile(file)) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.import_missing", file.toString()));
            return false;
        }
        String id = CutsceneFiles.idFor(file);
        try {
            CutsceneDefinition definition = CutsceneFiles.read(file, id);
            CutsceneRegistry.registerRuntime(id, definition);
            RuntimeCutsceneStore.get(ctx.getSource().getServer()).put(id, definition.build());
            ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.imported", id, file.getFileName().toString()), false);
            return true;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.import_failed", file.toString(), e.getMessage()));
            return false;
        }
    }

    private static int playAnchored(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        String id = CutsceneIdArgument.getId(ctx, "id");
        String anchor = StringArgumentType.getString(ctx, "anchor");
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        if (definition == null) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.unknown", id));
            return 0;
        }
        int count = 0;
        for (ServerPlayer player : targets) {
            if (CutsceneApi.playAnchored(player, definition, anchor)) {
                count++;
            }
        }
        int played = count;
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.played", definition.getId(), played), true);
        return count;
    }

    @Nullable
    private static AnchorBlockEntity lookedAtAnchor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(8.0));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK && player.level().getBlockEntity(hit.getBlockPos()) instanceof AnchorBlockEntity anchor) {
            return anchor;
        }
        ctx.getSource().sendFailure(Component.translatable("directorscamera.anchor.not_looking"));
        return null;
    }

    private static int anchorSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        AnchorBlockEntity anchor = lookedAtAnchor(ctx);
        if (anchor == null) {
            return 0;
        }
        String id = StringArgumentType.getString(ctx, "id");
        anchor.setAnchorId(id);
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.anchor.set", id), true);
        return 1;
    }

    private static int anchorInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        AnchorBlockEntity anchor = lookedAtAnchor(ctx);
        if (anchor == null) {
            return 0;
        }
        ctx.getSource().sendSuccess(() -> AnchorBlock.describe(anchor), false);
        return 1;
    }

    private static int anchorList(CommandContext<CommandSourceStack> ctx) {
        var ids = AnchorIndex.ids(ctx.getSource().getLevel());
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.anchor.list", String.join(", ", ids)), false);
        return ids.size();
    }

    private static int anchorReset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        AnchorBlockEntity anchor = lookedAtAnchor(ctx);
        if (anchor == null) {
            return 0;
        }
        anchor.resetTriggered();
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.anchor.reset"), true);
        return 1;
    }

    private static int anchorTrigger(CommandContext<CommandSourceStack> ctx, boolean once, int cooldown) throws CommandSyntaxException {
        AnchorBlockEntity anchor = lookedAtAnchor(ctx);
        if (anchor == null) {
            return 0;
        }
        String cutscene = CutsceneIdArgument.getId(ctx, "cutscene");
        double radius = DoubleArgumentType.getDouble(ctx, "radius");
        anchor.setTrigger(cutscene, radius, once, cooldown);
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.anchor.trigger_set", cutscene, radius), true);
        return 1;
    }

    private static int anchorTriggerClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        AnchorBlockEntity anchor = lookedAtAnchor(ctx);
        if (anchor == null) {
            return 0;
        }
        anchor.clearTrigger();
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.anchor.trigger_cleared"), true);
        return 1;
    }

    private static int cameraAnchor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        String id = StringArgumentType.getString(ctx, "id");
        CameraRecording recording = DirectorsCameraItem.getRecording(stack);
        List<CameraPos> world = recording.worldKeyframes(player.level(), player.position());
        var frame = AnchorIndex.nearest(player.level(), id, player.position(), 0);
        if (frame.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.anchor.none_nearby", id));
            return 0;
        }
        DirectorsCameraItem.setRecording(stack, recording.withKeyframes(frame.get().toLocal(world)).withAnchor(id));
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.camera.anchored", id), false);
        return 1;
    }

    private static int cameraUnanchor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        CameraRecording recording = DirectorsCameraItem.getRecording(stack);
        List<CameraPos> world = recording.worldKeyframes(player.level(), player.position());
        DirectorsCameraItem.setRecording(stack, recording.withKeyframes(world).withAnchor(""));
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.camera.unanchored"), false);
        return 1;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            CutsceneApi.stop(player);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.stopped", targets.size()), true);
        return targets.size();
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        var ids = CutsceneRegistry.ids();
        if (ids.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.none"), false);
            return 0;
        }
        for (String id : ids) {
            ctx.getSource().sendSuccess(() -> Component.literal(id), false);
        }
        return ids.size();
    }

    private static int fix(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CutsceneApi.stop(ctx.getSource().getPlayerOrException());
        return 1;
    }

    private static int captureSingle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String line = RecordingExporter.pointLine(DirectorsCameraItem.currentPose(player));
        player.sendSystemMessage(Component.literal(line).withStyle(ChatFormatting.GREEN));
        PacketDistributor.sendToPlayer(player, new ClipboardPayload(line));
        return 1;
    }

    private static int captureStart(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SCRATCH.put(player.getUUID(), new ArrayList<>());
        return captureAdd(ctx);
    }

    private static int captureAdd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        List<CameraPos> path = SCRATCH.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        path.add(DirectorsCameraItem.currentPose(player));
        player.sendSystemMessage(Component.translatable("directorscamera.command.captured", path.size()).withStyle(ChatFormatting.GREEN));
        return path.size();
    }

    private static int capturePrint(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        List<CameraPos> path = SCRATCH.getOrDefault(player.getUUID(), List.of());
        CameraRecording recording = CameraRecording.EMPTY.withKeyframes(path);
        String text = RecordingExporter.toScript(recording);
        player.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.GREEN));
        PacketDistributor.sendToPlayer(player, new ClipboardPayload(text));
        return path.size();
    }

    private static int captureClear(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SCRATCH.remove(player.getUUID());
        player.sendSystemMessage(Component.translatable("directorscamera.command.cleared"));
        return 1;
    }

    private static int export(CommandContext<CommandSourceStack> ctx, boolean json) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        CameraRecording recording = DirectorsCameraItem.getRecording(stack);
        String text = json ? RecordingExporter.toJson(recording) : RecordingExporter.toScript(recording);
        player.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.GREEN));
        PacketDistributor.sendToPlayer(player, new ClipboardPayload(text));
        return 1;
    }

    private static int save(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        String id = CutsceneRegistry.normalize(CutsceneIdArgument.getId(ctx, "id"));
        CameraRecording recording = DirectorsCameraItem.getRecording(stack).withName(id);
        DirectorsCameraItem.setRecording(stack, recording);
        CutsceneDefinition definition = recording.toDefinition();
        CutsceneRegistry.registerRuntime(id, definition);
        RuntimeCutsceneStore.get(player.level().getServer()).put(id, definition.build());
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.saved", id), true);
        return 1;
    }

    private static int delete(CommandContext<CommandSourceStack> ctx) {
        String id = CutsceneRegistry.normalize(CutsceneIdArgument.getId(ctx, "id"));
        boolean removed = CutsceneRegistry.removeRuntime(id);
        removed |= RuntimeCutsceneStore.get(ctx.getSource().getServer()).remove(id);
        if (!removed) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.unknown", id));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.deleted", id), true);
        return 1;
    }

    private static int load(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        String id = CutsceneIdArgument.getId(ctx, "id");
        CutsceneDefinition definition = CutsceneRegistry.get(id);
        if (definition == null) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.unknown", id));
            return 0;
        }
        DirectorsCameraItem.setRecording(stack, CameraRecording.fromDefinition(definition));
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.loaded", definition.getId()), true);
        return 1;
    }

    private static int name(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return setting(ctx, "name", (r, v) -> r.withName(CutsceneRegistry.normalize(v)), CutsceneIdArgument.getId(ctx, "id"));
    }

    private static int roll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        int index = IntegerArgumentType.getInteger(ctx, "index") - 1;
        float degrees = FloatArgumentType.getFloat(ctx, "degrees");
        CameraRecording recording = DirectorsCameraItem.getRecording(stack);
        if (index >= recording.keyframes().size()) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.command.bad_index", index + 1));
            return 0;
        }
        CameraPos old = recording.keyframes().get(index);
        DirectorsCameraItem.setRecording(stack, recording.replace(index, new CameraPos(old.pos(), old.yaw(), old.pitch(), degrees)));
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.setting", "roll " + (index + 1), degrees), false);
        return 1;
    }

    private static int insert(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        int index = IntegerArgumentType.getInteger(ctx, "index") - 1;
        CameraRecording recording = DirectorsCameraItem.getRecording(stack);
        DirectorsCameraItem.setRecording(stack, recording.insert(index, DirectorsCameraItem.currentPose(player)));
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.camera.recorded", index + 1), false);
        return 1;
    }

    private static int setting(CommandContext<CommandSourceStack> ctx, String key, BiFunction<CameraRecording, String, CameraRecording> update) throws CommandSyntaxException {
        return setting(ctx, key, update, StringArgumentType.getString(ctx, "value"));
    }

    private static int setting(CommandContext<CommandSourceStack> ctx, String key, BiFunction<CameraRecording, String, CameraRecording> update, String value) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = requireCamera(ctx, player);
        if (stack == null) {
            return 0;
        }
        try {
            DirectorsCameraItem.setRecording(stack, update.apply(DirectorsCameraItem.getRecording(stack), value));
        } catch (RuntimeException e) {
            ctx.getSource().sendFailure(Component.literal(e.getMessage() == null ? e.toString() : e.getMessage()));
            return 0;
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("directorscamera.command.setting", key, value), false);
        return 1;
    }

    private static ItemStack requireCamera(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof DirectorsCameraItem)) {
            ctx.getSource().sendFailure(Component.translatable("directorscamera.camera.not_holding"));
            return null;
        }
        return stack;
    }
}
