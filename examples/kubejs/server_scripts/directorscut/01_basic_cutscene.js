ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("flyover").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            const cutscene = DirectorsCut.cutscene()
                .setDurationSeconds(8)
                .setCurve("CATMULLROM")
                .setEasing("EASE_IN_OUT");

            cutscene.getPath()
                .addPoint(player.x + 10, player.y + 12, player.z + 10, -135, 35, 0)
                .addPoint(player.x, player.y + 20, player.z - 15, 0, 55, 0)
                .addPoint(player.x - 12, player.y + 8, player.z, 90, 25, 0)
                .addEntity(player);

            cutscene.play(player);
            return 1;
        })
    );
});
