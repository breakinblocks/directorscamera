ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("playrecording").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            const recording = DirectorsCut.recordingOf(player.mainHandItem);
            if (!recording) {
                player.tell("Hold a Director's Camera with a recording");
                return 0;
            }
            recording
                .setStopMode("PLAYER")
                .soundAtSecond(0, "minecraft:block.note_block.chime")
                .onEnd((p, reason) => p.tell("Recording playback ended: " + reason));
            recording.play(player);
            return 1;
        })
    );
});

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("orbittemplate").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            const stack = player.mainHandItem;
            if (stack.id != "directorscut:directors_camera") {
                player.tell("Hold a Director's Camera");
                return 0;
            }
            const template = DirectorsCut.cutscene().setDurationSeconds(6);
            template.getPath().addOrbit(player.x, player.y + 2, player.z, 6, 0, 360, 12, true);
            DirectorsCut.setRecording(stack, template);
            player.tell("Wrote an orbit template onto your camera");
            return 1;
        })
    );
});
