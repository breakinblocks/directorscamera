ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("showcase")
            .then(Commands.literal("orbit").executes(ctx => {
                const p = ctx.getSource().getPlayerOrException();
                DirectorsCamera.presets.orbit(p.x, p.y + 1, p.z, 8, 6).play(p);
                return 1;
            }))
            .then(Commands.literal("reveal").executes(ctx => {
                const p = ctx.getSource().getPlayerOrException();
                DirectorsCamera.presets.reveal(p.x, p.y, p.z, 12, 5).play(p);
                return 1;
            }))
            .then(Commands.literal("panorama").executes(ctx => {
                const p = ctx.getSource().getPlayerOrException();
                DirectorsCamera.presets.panorama(p.x, p.y + 1.6, p.z, 20, 1, 5).loop().setStopMode("PLAYER").play(p);
                return 1;
            }))
            .then(Commands.literal("everyone").executes(ctx => {
                const p = ctx.getSource().getPlayerOrException();
                const flyby = DirectorsCamera.presets.flyby(p.x - 20, p.y + 10, p.z, p.x + 20, p.y + 10, p.z, 4);
                const count = DirectorsCamera.playNear(p.level, p.x, p.y, p.z, 50, flyby);
                p.tell(`Playing for ${count} players`);
                return count;
            }))
            .then(Commands.literal("shake").executes(ctx => {
                const p = ctx.getSource().getPlayerOrException();
                DirectorsCamera.positionedShake(p.level, p.x + 6, p.y, p.z, 30, DirectorsCamera.shakeData(4, 10, 8, 3, 2));
                return 1;
            }))
    );
});
