package com.breakinblocks.directorscut.cutscene;

import com.breakinblocks.directorscut.curves.CurveType;
import com.breakinblocks.directorscut.curves.EasingType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CutsceneDefinition {
    public static final int DEFAULT_DURATION = 100;
    public static final double AUTO_DURATION_SPEED = 4.3;

    private String id = "";
    private final CameraPathBuilder path;
    private int duration = DEFAULT_DURATION;
    private CurveType curve = CurveType.CATMULLROM;
    private EasingType timeEasing = EasingType.EASE_IN_OUT;
    private EasingType lookEasing = EasingType.EASE_IN_OUT;
    private StopMode stopMode = StopMode.AUTOMATIC;
    private boolean skippable = true;
    private boolean loop;
    @Nullable
    private CutsceneDefinition next;
    private String nextId = "";
    private final List<CutsceneAction> actions = new ArrayList<>();
    private final List<CutsceneSound> sounds = new ArrayList<>();
    private final List<CutsceneEndCallback> endCallbacks = new ArrayList<>();
    private final List<CutsceneCallback> skipCallbacks = new ArrayList<>();
    @Nullable
    private FrameSource frameSource;
    private double anchorMaxDistance = 128.0;
    private boolean startFromPlayer;
    private boolean endAtPlayer;

    public CutsceneDefinition() {
        this.path = new CameraPathBuilder(this);
    }

    public static CutsceneDefinition create() {
        return new CutsceneDefinition();
    }

    public CutsceneDefinition id(String value) {
        this.id = value == null ? "" : value;
        return this;
    }

    public String getId() {
        return id;
    }

    public CameraPathBuilder getPath() {
        return path;
    }

    public CutsceneDefinition setDuration(int ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Duration must be a positive number");
        }
        this.duration = ticks;
        return this;
    }

    public CutsceneDefinition setDurationSeconds(double seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("Duration must be a positive number");
        }
        this.duration = (int) Math.floor(seconds * 20);
        return this;
    }

    public CutsceneDefinition setDurationAuto() {
        this.duration = 0;
        return this;
    }

    public CutsceneDefinition setCurve(Object curveType) {
        this.curve = CurveType.parse(curveType);
        return this;
    }

    public CutsceneDefinition setTimeEasing(Object easingType) {
        this.timeEasing = EasingType.parse(easingType);
        return this;
    }

    public CutsceneDefinition setLookEasing(Object easingType) {
        this.lookEasing = EasingType.parse(easingType);
        return this;
    }

    public CutsceneDefinition setEasing(Object easingType) {
        setTimeEasing(easingType);
        setLookEasing(easingType);
        return this;
    }

    public CutsceneDefinition setStopMode(Object mode) {
        this.stopMode = StopMode.parse(mode);
        return this;
    }

    public CutsceneDefinition skippable(boolean value) {
        this.skippable = value;
        return this;
    }

    public CutsceneDefinition loop() {
        return loop(true);
    }

    public CutsceneDefinition loop(boolean value) {
        this.loop = value;
        return this;
    }

    public CutsceneDefinition setNext(CutsceneDefinition cutscene) {
        if (cutscene == null) {
            throw new IllegalArgumentException("Next cutscene cannot be null");
        }
        this.next = cutscene;
        return this;
    }

    public CutsceneDefinition setNextId(String cutsceneId) {
        this.nextId = cutsceneId == null ? "" : cutsceneId;
        return this;
    }

    public CutsceneDefinition anchored(Object source) {
        if (source == null) {
            frameSource = null;
        } else if (source instanceof FrameSource fs) {
            frameSource = fs;
        } else if (source instanceof CutsceneFrame frame) {
            frameSource = FrameSource.fixed(frame);
        } else {
            String text = String.valueOf(source).trim();
            if (text.equalsIgnoreCase("player")) {
                frameSource = FrameSource.player();
            } else if (text.equalsIgnoreCase("player_eyes")) {
                frameSource = FrameSource.playerEyes();
            } else if (text.equalsIgnoreCase("structure")) {
                frameSource = FrameSource.structure(null);
            } else if (text.startsWith("structure:")) {
                frameSource = FrameSource.structure(ResourceLocation.parse(text.substring("structure:".length())));
            } else {
                frameSource = FrameSource.anchor(text, anchorMaxDistance);
            }
        }
        return this;
    }

    public CutsceneDefinition anchoredTo(double x, double y, double z, double yaw) {
        frameSource = FrameSource.fixed(CutsceneFrame.of(x, y, z, yaw));
        return this;
    }

    public CutsceneDefinition anchoredToPlayer() {
        frameSource = FrameSource.player();
        return this;
    }

    public CutsceneDefinition anchoredToStructure() {
        frameSource = FrameSource.structure(null);
        return this;
    }

    public CutsceneDefinition anchoredToStructure(String structureId) {
        frameSource = FrameSource.structure(ResourceLocation.parse(structureId));
        return this;
    }

    public CutsceneDefinition anchorMaxDistance(double distance) {
        anchorMaxDistance = distance;
        if (frameSource instanceof FrameSource.Anchor anchor) {
            frameSource = FrameSource.anchor(anchor.anchorId(), distance);
        }
        return this;
    }

    public CutsceneDefinition startFromPlayer() {
        return startFromPlayer(true);
    }

    public CutsceneDefinition startFromPlayer(boolean value) {
        startFromPlayer = value;
        return this;
    }

    public CutsceneDefinition endAtPlayer() {
        return endAtPlayer(true);
    }

    public CutsceneDefinition endAtPlayer(boolean value) {
        endAtPlayer = value;
        return this;
    }

    @Nullable
    public FrameSource getFrameSource() {
        return frameSource;
    }

    public boolean isStartFromPlayer() {
        return startFromPlayer;
    }

    public boolean isEndAtPlayer() {
        return endAtPlayer;
    }

    public CutsceneDefinition onEnd(CutsceneEndCallback callback) {
        endCallbacks.add(callback);
        return this;
    }

    public CutsceneDefinition onSkip(CutsceneCallback callback) {
        skipCallbacks.add(callback);
        return this;
    }

    public CutsceneDefinition execute(CutsceneCallback callback) {
        path.execute(callback);
        return this;
    }

    public CutsceneDefinition executeAt(int ticks, CutsceneCallback callback) {
        return executeAt(ticks, callback, null);
    }

    public CutsceneDefinition executeAt(int ticks, CutsceneCallback callback, @Nullable Map<String, Object> options) {
        if (ticks < 0) {
            throw new IllegalArgumentException("Ticks must be a non-negative number");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback must be a function");
        }
        boolean alwaysRun = options != null && Boolean.parseBoolean(String.valueOf(options.getOrDefault("alwaysRun", false)));
        actions.add(CutsceneAction.atTick(ticks, callback, alwaysRun));
        return this;
    }

    public CutsceneDefinition executeAtSecond(double seconds, CutsceneCallback callback) {
        return executeAt((int) Math.floor(seconds * 20), callback, null);
    }

    public CutsceneDefinition executeAtSecond(double seconds, CutsceneCallback callback, @Nullable Map<String, Object> options) {
        return executeAt((int) Math.floor(seconds * 20), callback, options);
    }

    public CutsceneDefinition addAction(CutsceneAction action) {
        actions.add(action);
        return this;
    }

    public CutsceneDefinition sound(int tick, String soundId) {
        return sound(tick, soundId, null);
    }

    public CutsceneDefinition sound(int tick, String soundId, @Nullable Map<String, Object> options) {
        sounds.add(applySoundOptions(CutsceneSound.play(tick, ResourceLocation.parse(soundId)), options));
        return this;
    }

    public CutsceneDefinition soundAtSecond(double seconds, String soundId) {
        return sound((int) Math.floor(seconds * 20), soundId, null);
    }

    public CutsceneDefinition soundAtSecond(double seconds, String soundId, @Nullable Map<String, Object> options) {
        return sound((int) Math.floor(seconds * 20), soundId, options);
    }

    public CutsceneDefinition stopSound(int tick, String soundHandle) {
        sounds.add(CutsceneSound.stopEntry(tick, soundHandle));
        return this;
    }

    public CutsceneDefinition stopSoundAtSecond(double seconds, String soundHandle) {
        return stopSound((int) Math.floor(seconds * 20), soundHandle);
    }

    public CutsceneDefinition music(int tick, String soundId) {
        return music(tick, soundId, null);
    }

    public CutsceneDefinition music(int tick, String soundId, @Nullable Map<String, Object> options) {
        CutsceneSound base = CutsceneSound.play(tick, ResourceLocation.parse(soundId)).withCategory(SoundSource.MUSIC).withStopOnEnd(true);
        sounds.add(applySoundOptions(base, options));
        return this;
    }

    public CutsceneDefinition addSound(CutsceneSound sound) {
        sounds.add(sound);
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public int effectiveDuration() {
        if (duration > 0) {
            return duration;
        }
        return durationFromSpeed(AUTO_DURATION_SPEED);
    }

    public int durationFromSpeed(double blocksPerSecond) {
        if (blocksPerSecond <= 0) {
            throw new IllegalArgumentException("Speed must be positive");
        }
        double seconds = path.pathLength() / blocksPerSecond;
        return Math.max(20, (int) Math.ceil(seconds * 20));
    }

    public CurveType getCurve() {
        return curve;
    }

    public EasingType getTimeEasing() {
        return timeEasing;
    }

    public EasingType getLookEasing() {
        return lookEasing;
    }

    public StopMode getStopMode() {
        return stopMode;
    }

    public boolean isSkippable() {
        return skippable;
    }

    public boolean isLooping() {
        return loop;
    }

    @Nullable
    public CutsceneDefinition getNext() {
        if (next != null) {
            return next;
        }
        if (!nextId.isEmpty()) {
            return CutsceneRegistry.get(nextId);
        }
        return null;
    }

    public String getNextId() {
        return nextId;
    }

    public List<CutsceneAction> getActions() {
        return actions;
    }

    public List<CutsceneSound> getSounds() {
        return sounds;
    }

    public List<CutsceneEndCallback> getEndCallbacks() {
        return endCallbacks;
    }

    public List<CutsceneCallback> getSkipCallbacks() {
        return skipCallbacks;
    }

    public CutsceneData build() {
        if (path.getKeyframes().isEmpty()) {
            throw new IllegalStateException("Cutscene must have at least one camera keyframe");
        }
        int total = effectiveDuration();
        List<CutsceneSound> sorted = new ArrayList<>(sounds);
        for (CutsceneSound sound : sorted) {
            if (sound.tick() > total) {
                throw new IllegalStateException("Sound entry at tick " + sound.tick() + " is beyond the cutscene duration of " + total);
            }
        }
        sorted.sort(Comparator.comparingInt(CutsceneSound::tick));
        return new CutsceneData(List.copyOf(path.getKeyframes()), total, curve, timeEasing, lookEasing, stopMode, skippable, loop, List.copyOf(sorted), id);
    }

    public static CutsceneDefinition fromData(CutsceneData data) {
        CutsceneDefinition def = new CutsceneDefinition();
        def.id = data.id();
        def.path.addKeyframes(data.keyframes());
        def.duration = data.duration();
        def.curve = data.curve();
        def.timeEasing = data.timeEasing();
        def.lookEasing = data.lookEasing();
        def.stopMode = data.stopMode();
        def.skippable = data.skippable();
        def.loop = data.loop();
        def.sounds.addAll(data.sounds());
        return def;
    }

    public CutsceneDefinition copy() {
        CutsceneDefinition copy = new CutsceneDefinition();
        copy.id = id;
        copy.path.addKeyframes(path.getKeyframes());
        copy.duration = duration;
        copy.curve = curve;
        copy.timeEasing = timeEasing;
        copy.lookEasing = lookEasing;
        copy.stopMode = stopMode;
        copy.skippable = skippable;
        copy.loop = loop;
        copy.next = next;
        copy.nextId = nextId;
        copy.actions.addAll(actions);
        copy.sounds.addAll(sounds);
        copy.endCallbacks.addAll(endCallbacks);
        copy.skipCallbacks.addAll(skipCallbacks);
        copy.frameSource = frameSource;
        copy.anchorMaxDistance = anchorMaxDistance;
        copy.startFromPlayer = startFromPlayer;
        copy.endAtPlayer = endAtPlayer;
        return copy;
    }

    public boolean play(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return CutsceneApi.play(serverPlayer, this);
        }
        return false;
    }

    public int playForPlayers(Iterable<?> players) {
        return CutsceneApi.playFor(players, this);
    }

    public int playForAll(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return CutsceneApi.playAll(serverLevel, this);
        }
        return 0;
    }

    public int playNear(Level level, double x, double y, double z, double radius) {
        if (level instanceof ServerLevel serverLevel) {
            return CutsceneApi.playNear(serverLevel, new Vec3(x, y, z), radius, this);
        }
        return 0;
    }

    private static CutsceneSound applySoundOptions(CutsceneSound sound, @Nullable Map<String, Object> options) {
        if (options == null) {
            return sound;
        }
        CutsceneSound result = sound;
        Object volume = options.get("volume");
        if (volume instanceof Number n) {
            result = result.withVolume(n.floatValue());
        }
        Object pitch = options.get("pitch");
        if (pitch instanceof Number n) {
            result = result.withPitch(n.floatValue());
        }
        Object category = options.get("category");
        if (category != null) {
            result = result.withCategory(CutsceneSound.parseSource(category));
        }
        Vec3 pos = readVec(options.get("pos"));
        if (pos == null && options.get("x") instanceof Number x && options.get("y") instanceof Number y && options.get("z") instanceof Number z) {
            pos = new Vec3(x.doubleValue(), y.doubleValue(), z.doubleValue());
        }
        if (pos != null) {
            result = result.withPosition(pos);
        }
        Object attach = options.get("attachToCamera");
        if (attach != null) {
            result = result.withAttachToCamera(Boolean.parseBoolean(String.valueOf(attach)));
        }
        Object stopOnEnd = options.get("stopOnEnd");
        if (stopOnEnd != null) {
            result = result.withStopOnEnd(Boolean.parseBoolean(String.valueOf(stopOnEnd)));
        }
        Object handle = options.get("id");
        if (handle != null) {
            result = result.withId(String.valueOf(handle));
        }
        return result;
    }

    @Nullable
    private static Vec3 readVec(@Nullable Object value) {
        if (value instanceof Vec3 vec) {
            return vec;
        }
        if (value instanceof List<?> list && list.size() >= 3 && list.get(0) instanceof Number x && list.get(1) instanceof Number y && list.get(2) instanceof Number z) {
            return new Vec3(x.doubleValue(), y.doubleValue(), z.doubleValue());
        }
        if (value instanceof double[] arr && arr.length >= 3) {
            return new Vec3(arr[0], arr[1], arr[2]);
        }
        return null;
    }
}
