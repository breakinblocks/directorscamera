# Examples

Drop the `kubejs/` and `data/` folders into a pack instance (the data files can also go into any datapack) to try them.

| File | Shows |
|---|---|
| `kubejs/server_scripts/directorscut/01_basic_cutscene.js` | Building a cutscene in a command with raw points and `addEntity`, then playing it. |
| `kubejs/server_scripts/directorscut/02_registered_cutscenes.js` | Registering named definitions in `DirectorsCutEvents.register`, chaining with `setNext`, adding a preset, playing by id. |
| `kubejs/server_scripts/directorscut/03_sounds_actions_chaining.js` | Music with a handle, positional and camera-attached sounds, timed server actions, `alwaysRun`, `PLAYER` stop mode, `onEnd` and `onSkip`. |
| `kubejs/server_scripts/directorscut/04_events.js` | `beforePlay` cancellation and per-player tweaks, `started`, `tick`, `ended`. |
| `kubejs/server_scripts/directorscut/05_presets_and_nearby_players.js` | The presets, playing for everyone within a radius, positioned screen shake. |
| `kubejs/server_scripts/directorscut/06_camera_item_recordings.js` | Playing the recording on a held director's camera with extra sounds, and writing a generated path onto the held camera. |
| `kubejs/server_scripts/directorscut/07_keyframe_animation.js` | Parsing a keyframe animation from script, sampling it through a pose, and turning the samples into a cutscene. |
| `kubejs/server_scripts/directorscut/08_anchors.js` | Anchor-relative definitions with `startFromPlayer` / `endAtPlayer`, `frameOf` inside an action, a player-relative entrance shot, `playAnchored`, and virtual anchors. |
| `kubejs/client_scripts/directorscut_client.js` | Client-side start and end events, and the expression evaluator. |
| `data/mypack/directorscut/cutscenes/room_reveal.json` | A data-driven definition anchored to a `throne_room` anchor block with offsets in anchor space. |
| `data/mypack/directorscut/cutscenes/spawn_flyover.json` | A data-driven definition with generators, sounds, a command action and a chained `next`. |
| `data/mypack/directorscut/cutscenes/spawn_hold.json` | A short `PLAYER` stop-mode hold shot used as the chain target. |
| `data/mypack/directorscut/cutscenes/spawn_panorama.json` | A looping panorama: one `panorama` generator, linear easings, `loop` and `PLAYER` stop mode. |
| `data/mypack/directorscut/animations/camera_moves.json` | Two keyframe animations, one expression-driven loop and one Catmull-Rom rise, with Bedrock axis conventions disabled. |

In-game commands the scripts add: `/flyover`, `/tour`, `/bossreveal`, `/showcase orbit|reveal|panorama|everyone|shake`, `/playrecording`, `/orbittemplate`, `/animcutscene`, `/throneroom`, `/virtualanchor`. The Director's Camera and the Cutscene Anchor are in the Tools and Utilities creative tab. For the anchor examples, place an anchor, look at it and run `/directorscut anchor set throne_room`, or use `/virtualanchor` where you stand.
