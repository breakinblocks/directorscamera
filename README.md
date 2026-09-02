# Director's Camera

Server-driven camera cutscenes for NeoForge 26.1.2. The server describes a camera path, sends it to one or more clients, and each client plays it on a dedicated camera entity while player input is suppressed. On top of that the mod provides timed sounds, hold-to-skip, screen shake, a director's camera item for recording paths in game, data-driven cutscene definitions, a Bedrock-style keyframe animation system with an expression engine, and a KubeJS API.

- Minecraft 26.1.2, NeoForge 26.1.2 or newer, Java 25
- KubeJS 26.1.2-8.0 or newer is optional; the scripting API only appears when it is installed
- Mod id `directorscamera`

Example scripts and data files are in [`examples/`](examples/README.md).

## Contents

1. [Concepts](#concepts)
2. [Commands](#commands)
3. [Director's camera item](#directors-camera-item)
4. [Anchors and relative cutscenes](#anchors-and-relative-cutscenes)
5. [Data-driven definitions](#data-driven-definitions)
6. [KubeJS API](#kubejs-api)
7. [Keyframe animations](#keyframe-animations)
8. [Screen shake](#screen-shake)
9. [Configuration](#configuration)

## Concepts

**Cutscene definition.** A list of camera keyframes (position, yaw, pitch, roll), a duration in ticks, a curve type, two easings, a stop mode, a skippable flag, a loop flag, optional timed sounds, optional timed server actions, and an optional next cutscene to chain into. Definitions are reusable: build one, play it for as many players as you like.

**Keyframes and curves.** The camera position is interpolated through the keyframes with either straight lines (`LINEAR`) or a Catmull-Rom spline (`CATMULLROM`, the default). Rotation is interpolated per segment with shortest-arc yaw. Time easing (`LINEAR`, `EASE_IN`, `EASE_OUT`, `EASE_IN_OUT`) retimes the whole cutscene; look easing shapes the rotation inside each segment. Duration `auto` derives the length from the path distance at walking speed.

**Stop modes.**

| Mode | Behaviour |
|---|---|
| `AUTOMATIC` (default) | Ends by itself when the path finishes. |
| `PLAYER` | Holds the last shot until the player skips (hold jump) or presses the end-cutscene key (J by default). |
| `UNSTOPPABLE` | Cannot be skipped by the player; ends only from the server (`stop`, `/directorscamera fix`, logout). |

**Hold-to-skip.** Any skippable cutscene ends when the player holds their jump key for 1.5 seconds. A progress bar and hint appear in the lower right. Skipping abandons the whole chain and runs any server actions flagged `alwaysRun` that have not fired yet, so teleports and stage changes still happen.

**Looping.** `loop(true)` sends the timeline back to the start when it reaches the end instead of finishing, so the shot runs until the player skips it or the server stops it. `AUTOMATIC` stop mode never fires while a cutscene loops. Timed sounds and server actions fire once, on the first pass. A full-circle panorama loops without a seam because its first and last keyframes are the same pose; a path that ends somewhere else cuts back to the start on each pass.

**Sessions.** The server tracks a session per player: it waits for the client to confirm the start, fires timed actions on the server timeline, chains the next cutscene when the client reports the end, and cancels everything on logout, death or dimension change.

**Timed sounds** are part of the definition and are played by the client on the camera timeline, so they stay aligned with the picture even when the server lags.

## Commands

All commands require permission level 2 except `capture` and `fix`.

| Command | Effect |
|---|---|
| `/directorscamera play <id> [targets]` | Play a registered definition for the targets (default: you). |
| `/directorscamera stop [targets]` | Stop and cancel sessions. |
| `/directorscamera list` | List registered definition ids. |
| `/directorscamera fix` | Emergency stop for yourself (works in `UNSTOPPABLE` mode). |
| `/directorscamera capture` | Print your current camera pose as an `.addPoint(...)` line and copy it to the clipboard. |
| `/directorscamera capture start`, `add`, `print`, `clear` | Build a scratch path pose by pose and print it as a script. |
| `/directorscamera camera export` | Copy the held camera recording as a KubeJS script. |
| `/directorscamera camera export json` | Copy it as a definition JSON file. |
| `/directorscamera camera save <id>` | Register the held recording under an id and store it with the world, so it survives restarts. |
| `/directorscamera delete <id>` | Remove a cutscene saved with `camera save`. |
| `/directorscamera export <id\|all> [json\|script]` | Write a definition to `<game dir>/directorscamera/export/` as a datapack file (default) or a KubeJS script. |
| `/directorscamera import <file\|all>` | Load JSON definitions from `<game dir>/directorscamera/import/` and store them with the world. |
| `/directorscamera camera load <id>` | Replace the held recording with a registered definition. |
| `/directorscamera camera name <id>` | Name the recording without saving. |
| `/directorscamera camera roll <index> <degrees>` | Set the roll of one keyframe (1-based). |
| `/directorscamera camera insert <index>` | Insert your current pose before a keyframe. |
| `/directorscamera camera curve\|easing\|timeEasing\|lookEasing <value>` | Edit the recording settings. |
| `/directorscamera camera duration <ticks\|auto>` | Set the recording duration. |
| `/directorscamera camera anchor <id>` | Convert the held recording to coordinates relative to the nearest anchor with that id. |
| `/directorscamera camera anchor clear` | Convert an anchored recording back to world coordinates. |
| `/directorscamera playanchored <id> <anchor> [targets]` | Play a definition relative to the nearest anchor with that id. |
| `/directorscamera anchor set <id>` | Set the id of the anchor block you are looking at. |
| `/directorscamera anchor trigger <cutscene> <radius> [once] [cooldown]` | Make that anchor play a cutscene when players come within the radius. |
| `/directorscamera anchor trigger clear`, `reset`, `info`, `list` | Remove the trigger, clear its per-player history, describe it, or list anchor ids in the dimension. |

Ids are resource locations. A bare name such as `test` is read as `directorscamera:test`; add your own namespace (`mypack:test`) when you want one.

**Export and import.** `export` writes `directorscamera/export/data/<namespace>/directorscamera/cutscenes/<path>.json` next to a `pack.mcmeta`, so the whole `export` folder can be dropped into `world/datapacks/` or its `data` folder into `kubejs/data/`; `export <id> script` writes a KubeJS script under `directorscamera/export/kubejs/server_scripts/` instead. `import` reads any `.json` under `directorscamera/import/`; a file at `import/<namespace>/<path>.json` keeps that id, anything else gets the `directorscamera` namespace. Imported cutscenes are saved with the world like `camera save`. Exports contain keyframes, settings, anchors, sounds and command actions; script callbacks cannot be exported.

## Director's camera item

`directorscamera:directors_camera` (Tools and Utilities tab) records camera paths without typing coordinates. The recording is stored on the item, so recordings can be copied and handed around.

| Input | Action |
|---|---|
| Right-click | Record your eye position and look angles as the next keyframe. |
| Right-click while looking at a marker | Replace that keyframe with your current pose. |
| Sneak + right-click | Remove the last keyframe. |
| Left-click | Preview the recording as a real cutscene (needs 2 keyframes). |
| Sneak + left-click, twice within 2 seconds | Clear the recording. |
| Sneak + scroll | Cycle the preview duration through the timing presets and `auto`. |

While the item is held, every keyframe shows a numbered marker, a look-direction line and the interpolated path. The marker under your crosshair turns green.

Typical workflow: walk the shot, right-click at each camera position, left-click to preview, then `/directorscamera camera export` and paste the script into your pack, or `/directorscamera camera save mypack:intro` and play it by id.

## Anchors and relative cutscenes

A cutscene can be authored relative to a **frame** (a position and a yaw) instead of absolute world coordinates. At play time the server resolves the frame for that player, rotates and offsets every keyframe, sound position and action offset, and sends the client absolute data. The same definition then works in every copy of a room, at any rotation, and for a player standing anywhere.

Frame sources, chosen with `anchored(...)` on a definition or `"anchor"` in a data file:

| Source | How to select | Resolves to |
|---|---|---|
| Anchor block | `anchored("throne_room")` | The nearest `Cutscene Anchor` block with that id in the player's dimension, within `anchorMaxDistance` (default 128). |
| Fixed frame | `anchoredTo(x, y, z, yaw)` | Exactly that frame. Use it when the code that placed a structure already knows the origin. |
| Player | `anchoredToPlayer()` | The player's position and facing at play time. |
| Structure | `anchoredToStructure()` or `anchoredToStructure("minecraft:ancient_city")` | The template origin and rotation of the world-generated structure the player is standing in. |
| Virtual anchor | `DirectorsCamera.registerAnchor(level, id, x, y, z, yaw)` | Script-registered frames that the anchor lookup treats like blocks (not persisted). |

Two flags adapt the entry and exit to the player: `startFromPlayer()` prepends the player's current eye pose as the first keyframe so the shot flies in from wherever they stand, and `endAtPlayer()` appends it so the camera hands back without a cut. Chained cutscenes inherit the resolved frame.

**Cutscene Anchor block** (`directorscamera:anchor`, Tools and Utilities tab). Invisible, non-colliding and unbreakable in survival; visible as a cyan box with a facing arrow and label to creative players and to anyone holding a director's camera. Its facing sets the frame yaw (placed facing the way you look). It keeps its settings inside structure templates, so put one in a room template and every placed copy carries it. Right-click it to see its settings. Configure it while looking at it:

- `/directorscamera anchor set throne_room` gives it an id.
- `/directorscamera anchor trigger mypack:throne_room 6 true 0` plays that cutscene, anchored to this block, when a player comes within 6 blocks, once per player, no cooldown. `once false` with a cooldown in ticks replays it. The trigger radius is drawn as a red box while visible.

**Authoring an anchored path.** Stand in the room, record with the director's camera as usual, then `/directorscamera camera anchor throne_room`. The recording converts to anchor space, previews still play correctly, the markers draw relative to the nearest anchor (walk to another copy of the room to check it), and `camera export` adds `.anchored("throne_room")` to the script.

Inside server actions, `DirectorsCamera.frameOf(player)` returns the frame the running cutscene was resolved with, and `frame.pos(x, y, z)` converts a local offset to a world position for teleports or effects.

## Data-driven definitions

Files at `data/<namespace>/directorscamera/cutscenes/<id>.json` are loaded on every data reload and register as `<namespace>:<id>`. Script-registered definitions with the same id override them.

```json
{
  "durationSeconds": 12,
  "curve": "CATMULLROM",
  "easing": "EASE_IN_OUT",
  "stopMode": "AUTOMATIC",
  "skippable": true,
  "loop": false,
  "keyframes": [
    [10.5, 70, 10.5, -135, 20, 0],
    { "type": "point", "pos": [0, 72, 0], "lookAt": [0, 64, 0] },
    { "type": "orbit", "center": [0, 64, 0], "radius": 12, "startAngle": 0, "endAngle": 360, "points": 24 },
    { "type": "spiral", "center": [0, 64, 0], "startRadius": 12, "endRadius": 4, "height": 8, "turns": 1, "points": 36 },
    { "type": "spin", "pos": [0, 70, 0], "startYaw": 0, "endYaw": 360, "pitch": 15, "points": 36 },
    { "type": "panorama", "pos": [0, 70, 0], "turns": 1, "pitch": 10, "startYaw": 0 },
    { "type": "arc", "start": [0, 64, 0], "end": [20, 64, 0], "height": 6, "points": 10 }
  ],
  "sounds": [
    { "tick": 0, "sound": "minecraft:music_disc.otherside", "category": "music", "id": "theme" },
    { "second": 3, "sound": "minecraft:entity.ender_dragon.growl", "volume": 0.8, "pos": [0, 64, 0] },
    { "tick": 200, "stop": "theme" }
  ],
  "actions": [
    { "second": 7, "command": "tp @s 0 65 0 -90 0", "alwaysRun": true }
  ],
  "next": "mypack:second_part"
}
```

Keyframe entries are either `[x, y, z, yaw, pitch, roll]` arrays (trailing values optional) or generator objects (`point`, `orbit`, `spin`, `panorama`, `arc`, `spiral`). `duration` (ticks) or `durationSeconds` sets the length; omit both for `auto`. `next` is another definition id or an inline definition object. Actions run as commands from the player at permission level 2.

Relative definitions add `"anchor"` (an anchor id string, or an object such as `{ "type": "player" }`, `{ "type": "structure", "id": "minecraft:ancient_city" }`, or `{ "type": "fixed", "pos": [0, 64, 0], "yaw": 90 }`), plus optional `"anchorMaxDistance"`, `"startFromPlayer"` and `"endAtPlayer"`. Keyframes and sound positions are then offsets from the frame.

## KubeJS API

Everything is reachable through the `DirectorsCamera` global in server scripts (and, for the client events and expression helpers, client scripts).

### Global functions

| Function | Returns | Description |
|---|---|---|
| `cutscene()` | definition builder | New definition with the defaults: 100 ticks, `CATMULLROM`, `EASE_IN_OUT`, `AUTOMATIC`, skippable. |
| `path()` | path builder | Standalone path builder attached to a fresh definition. |
| `keyframe(x, y, z, yaw?, pitch?, roll?)` | keyframe | A raw keyframe object. |
| `keyframeLookingAt(x, y, z, tx, ty, tz)` | keyframe | Keyframe facing a target. |
| `vec(x, y, z)`, `vecOf(entity)`, `eyesOf(entity)` | `Vec3` | Vector helpers. |
| `offset(origin, yaw, pitch, distance)` | `Vec3` | Move along a look direction. |
| `presets.orbit / flyby / reveal / pan / zoom` | definition | Ready-made shots, see the presets table. |
| `TIMING`, `SPEED` | maps | Tick and blocks-per-second presets (`TIMING.CINEMATIC`, `SPEED.WALK`). |
| `durationFromSpeed(pathOrCutscene, blocksPerSecond)` | ticks | Duration from path length. |
| `play(player, cutsceneOrId)` | boolean | Play for one player. |
| `playFor(players, cutscene)` | count | Play for a list of players. |
| `playNear(level, x, y, z, radius, cutscene)` | count | Play for everyone within a radius. |
| `playAll(level, cutscene)` | count | Play for everyone in a level. |
| `stop(player)`, `stopNear(level, x, y, z, radius)` | | Stop cutscenes. |
| `isPlaying(player)` | boolean | Server-side session check. |
| `get(id)`, `ids()`, `register(id, cutscene)` | | Registry access. |
| `recordingOf(itemStack)` | definition or null | The recording on a director's camera as a definition. |
| `setRecording(itemStack, cutscene)` | | Write a definition's keyframes and settings onto a camera. |
| `playAnchored(player, cutsceneOrId, anchorId)` | boolean | Play relative to the nearest anchor with that id. |
| `playAnchored(player, cutsceneOrId, x, y, z, yaw)` | boolean | Play relative to an explicit frame. |
| `frame(x, y, z, yaw)`, `anchorSource(id, maxDistance)` | frame / source | Build frames and frame sources for `anchored(...)`. |
| `frameOf(player)` | frame or null | The frame the player's running cutscene was resolved with; `frame.pos(x, y, z)` converts offsets. |
| `registerAnchor(level, id, x, y, z, yaw)`, `clearVirtualAnchors(level, id)` | | Virtual anchors without a block. |
| `anchors(level, id)`, `nearestAnchor(level, id, x, y, z)`, `anchorIds(level)` | | Anchor lookups. |
| `shakeData(in, stay, out, amplitude, frequency)` | shake data | Shake parameters (ticks, blocks or degrees, cycles). |
| `shake(level, x, y, z, radius, data)` | | Random translation shake for players in range. |
| `positionedShake(level, x, y, z, radius, data)` | | Distance-attenuated tilt toward a point. |
| `animation(id)`, `animationIds()`, `parseAnimation(id, json, bedrock?)` | | Keyframe animation registry. |
| `ticker(idOrAnimation)`, `animationSystem()`, `pose()` | | Keyframe playback objects. |
| `expression(text)`, `evaluate(text, { name: value })` | | Expression engine. |

### Definition builder

All setters return the builder.

| Method | Description |
|---|---|
| `id(string)` | Name used in events and commands. |
| `getPath()` | The path builder. |
| `setDuration(ticks)`, `setDurationSeconds(seconds)`, `setDurationAuto()` | Length. |
| `setCurve("LINEAR" \| "CATMULLROM")` | Position curve. |
| `setTimeEasing(name)`, `setLookEasing(name)`, `setEasing(name)` | Easings, case-insensitive strings. |
| `setStopMode("AUTOMATIC" \| "PLAYER" \| "UNSTOPPABLE")` | End rule. |
| `skippable(boolean)` | Enable hold-to-skip (default true; `UNSTOPPABLE` forces false). |
| `loop()`, `loop(boolean)` | Restart the path instead of ending (default false). See Looping. |
| `setNext(cutscene)`, `setNextId(id)` | Chain another definition after this one finishes. |
| `anchored(idOrSource)`, `anchoredTo(x, y, z, yaw)`, `anchoredToPlayer()`, `anchoredToStructure(id?)`, `anchorMaxDistance(blocks)` | Make keyframes relative to a frame (see Anchors). |
| `startFromPlayer()`, `endAtPlayer()` | Prepend or append the player's current pose at play time. |
| `executeAt(ticks, fn, { alwaysRun })`, `executeAtSecond(seconds, fn, { alwaysRun })` | Server action at a time; `fn(player)`. |
| `execute(fn)` | Server action when the camera reaches the last keyframe added so far. |
| `sound(tick, id, options?)`, `soundAtSecond(seconds, id, options?)` | Timed sound. Options: `volume`, `pitch`, `category`, `pos` or `x`/`y`/`z`, `attachToCamera`, `stopOnEnd`, `id`. |
| `stopSound(tick, handle)`, `stopSoundAtSecond(seconds, handle)` | Stop a sound started with an `id`. |
| `music(tick, id, options?)` | Non-positional music-category sound that pauses vanilla music. |
| `onEnd(fn)` | `fn(player, reason)` on every end; reasons `finished`, `player`, `server`, `disconnect`, `timeout`. |
| `onSkip(fn)` | `fn(player)` only when the player skipped. |
| `build()` | The network data object (rarely needed). |
| `play(player)`, `playForPlayers(list)`, `playForAll(level)`, `playNear(level, x, y, z, radius)` | Play helpers. |
| `copy()` | Independent copy. |

### Path builder

| Method | Description |
|---|---|
| `addPoint(x, y, z, yaw?, pitch?, roll?)` | Raw keyframe; the format `/directorscamera capture` prints. |
| `addVec3(vec, yaw?, pitch?, roll?)` | Same from a vector. |
| `addEntity(entity, ox?, oy?, oz?)` | Keyframe at the entity's eyes with its look angles. |
| `addLookingAt(x, y, z, tx, ty, tz, roll?)` | Keyframe facing a target. |
| `addOrbit(cx, cy, cz, radius, startAngle, endAngle, points, lookAtCenter?)` | Circle around a point. |
| `addSpin(x, y, z, startYaw, endYaw, pitch, points)` | Turn in place. |
| `addPanorama(x, y, z, turns, pitch?, startYaw?)` | Spin in place through `turns` full circles, negative to turn the other way. Picks its own point count so no step is large enough to flip the shortest-arc direction. |
| `addArc(sx, sy, sz, ex, ey, ez, height, points)` | Parabolic hop. |
| `addSpiral(cx, startY, cz, startRadius, endRadius, height, turns, points)` | Spiral; equal radii and zero height give a flat orbit. |
| `addKeyframe(keyframe)`, `addKeyframes(list)`, `clear()`, `getKeyframeCount()`, `pathLength()` | Utilities. |
| `execute`, `executeAt`, `executeAtSecond`, `sound`, `soundAtSecond`, `stopSound` | Forward to the owning definition so calls can be chained inside the path. |

The path methods also accept a trailing `cutscene` argument; it is ignored when the path already belongs to that definition.

Look easing shapes each keyframe segment, not the path as a whole, so a spin, orbit or panorama built from many points pulses once per point under the default `EASE_IN_OUT`. Set `setLookEasing("LINEAR")` for a constant turn rate; `presets.orbit` and `presets.panorama` already do.

### Presets

| Preset | Shot |
|---|---|
| `presets.orbit(cx, cy, cz, radius, seconds = 5)` | Full circle looking at the centre, linear easing. |
| `presets.flyby(sx, sy, sz, ex, ey, ez, seconds = 3)` | Straight flight with a constant heading. |
| `presets.reveal(x, y, z, distance, seconds = 4)` | Descend toward a point, ease out in position and ease in in rotation. |
| `presets.pan(x, y, z, startYaw, endYaw, seconds = 3)` | Turn in place. Yaw is stored wrapped to -180..180 and each segment takes the shortest arc, so this is for partial turns; use `panorama` for a half turn or more. |
| `presets.panorama(x, y, z, seconds = 12, turns = 1, pitch = 0)` | Spin in place at a constant rate through `turns` full circles, linear easings. Add `.loop()` for an endless panorama and `.setStopMode("PLAYER")` to hold it until the end-cutscene key. |
| `presets.zoom(sx, sy, sz, tx, ty, tz, seconds = 2)` | Fly into a target. |

### Server events

```js
DirectorsCameraEvents.register(event => {
    const intro = event.create("mypack:intro");
    intro.setDurationSeconds(8).getPath().addOrbit(0, 70, 0, 10, 0, 360, 20);
});

DirectorsCameraEvents.beforePlay(event => {
    if (event.player.level.dimension != "minecraft:overworld") event.cancel();
});

DirectorsCameraEvents.started(event => console.log(`${event.player.username} started ${event.id}`));
DirectorsCameraEvents.tick(event => { if (event.tick == 100) event.player.tell("halfway"); });
DirectorsCameraEvents.ended(event => { if (event.skipped) event.player.tell("skipped"); });
```

`register` fires after server scripts load (including `/reload`), so definitions created there are always current. `beforePlay` exposes `player`, `level`, `id` and `cutscene` (a copy you may edit); cancelling blocks playback. `started`, `tick` and `ended` expose `player`, `level`, `id`, plus `tick`/`seconds` and `reason`/`finished`/`skipped` respectively.

### Client events

```js
DirectorsCameraClientEvents.started(event => console.log("cutscene " + event.id));
DirectorsCameraClientEvents.ended(event => console.log(event.id + " " + event.reason));
```

## Keyframe animations

The `keyframe` package is a Bedrock-style animation system usable on its own: per-bone position, rotation and scale channels, linear or Catmull-Rom keyframes with `pre`/`post` values, loop modes, blended transitions, and a ticker with speed, reverse and partial-tick sampling. Keyframe values may be expressions.

Animations load from `data/<namespace>/directorscamera/animations/<file>.json`. A Blockbench export with an `animations` object registers each entry as `<namespace>:<file>/<name>`; a file containing a single animation object registers as `<namespace>:<file>`. They sync to clients automatically. Add `"bedrock_conventions": false` at the top of a file to keep raw axis signs instead of Bedrock's X and Y flips.

Because this mod has no bone models, animations are applied to a pose object. `DirectorsCamera.pose()` creates bones on demand; `pose.cameraPos("camera", origin)` turns a bone into a camera keyframe (position offsets in blocks, rotation X as pitch, Y as yaw, Z as roll).

```js
const system = DirectorsCamera.animationSystem();
system.startAnimation("main", DirectorsCamera.ticker("mypack:camera/orbit").setLoopMode("LOOP").setSpeed(1.5).build());

const pose = DirectorsCamera.pose();
system.tick();
system.applyAnimations(pose, 0);
const cam = pose.cameraPos("camera", DirectorsCamera.vec(0, 64, 0));
```

`startAnimation` blends from the current pose into the new animation, `stopAnimation` fades back to rest over the ticker's to-null time, and `setVariable(name, value)` feeds expression variables. Expressions support `+ - * /`, parentheses, unary minus, `query.anim_time` (seconds), `math.pi` and the `math.*` functions (`abs`, `sin`, `cos`, `asin`, `acos`, `atan`, `atan2`, `sqrt`, `pow`, `exp`, `ln`, `floor`, `ceil`, `round`, `trunc`, `min`, `max`, `clamp`, `lerp`, `mod`, `random`). Trigonometry is in degrees.

## Screen shake

`DirectorsCamera.shake(level, x, y, z, radius, DirectorsCamera.shakeData(2, 6, 4, 0.15, 1))` applies a random screen-plane translation to every player within the radius. `positionedShake` instead tilts the view toward the point with a falloff over the radius. Shakes do not move the held item.

## Configuration

Client (`config/directorscamera-client.toml`): `showSkipHint` shows the resting hold-to-skip hint; `closeScreenOnStart` closes any open screen when a cutscene starts. Server (`serverconfig/directorscamera-server.toml`): `startTimeoutTicks` is how long a session waits for the client's start confirmation.

The end-cutscene key defaults to J and only ends `PLAYER` mode cutscenes; hold-to-skip is the intended way to skip.
