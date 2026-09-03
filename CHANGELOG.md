# 1.0.1

- Timed screen effects: `fadeToBlack`, `fadeToWhite` and `screenEffect` on a definition, a `screenEffects` list in definition JSON, drawn above the suppressed HUD and below the skip bar.
- Chromatic aberration as a timed screen effect, `chromaticAberration(tick, strength, fadeIn, hold, fadeOut)`, running as a post-processing pass over the world.

# 1.0.0

- Ported to Minecraft 26.1.2 / NeoForge 26.1.2, Java 25. Requires KubeJS 26.1.2-8.0 or newer for the scripting API.
- Initial release: server-driven camera cutscenes, screen shake, timed sounds, hold-to-skip, the director's camera recording item, named cutscene definitions from data files, and the KubeJS API.
- Keyframe animation system with Bedrock-style animation files, blended transitions, tickers and an expression engine; animations load from data packs and sync to clients.
- Anchors and relative cutscenes: the Cutscene Anchor block (invisible, structure-safe, optional proximity trigger), frame sources (anchor, fixed, player, structure, virtual), `startFromPlayer` and `endAtPlayer`, anchored camera recordings, and the `playanchored` and `anchor` commands.
- Panorama shots: `presets.panorama`, `addPanorama` on the path builder and a `panorama` keyframe generator in data files, plus a `loop` flag that restarts a cutscene instead of ending it.
- Saved recordings (`camera save`) persist with the world; `delete` removes them. `export` writes definitions as datapack files or KubeJS scripts and `import` loads JSON files into the world.
