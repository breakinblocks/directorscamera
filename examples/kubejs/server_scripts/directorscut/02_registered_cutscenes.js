DirectorsCutEvents.register(event => {
    const spawnTour = event.create("mypack:spawn_tour");
    spawnTour
        .setDurationSeconds(15)
        .setCurve("CATMULLROM")
        .setEasing("EASE_IN_OUT")
        .setStopMode("AUTOMATIC");

    spawnTour.getPath()
        .addLookingAt(20, 80, 20, 0, 64, 0)
        .addSpiral(0, 78, 0, 20, 8, -6, 1, 48)
        .addLookingAt(4, 68, 4, 0, 64, 0);

    const orbit = event.create("mypack:spawn_orbit");
    orbit.setDurationSeconds(10).setEasing("LINEAR");
    orbit.getPath().addOrbit(0, 70, 0, 14, 0, 360, 36, true);

    spawnTour.setNext(orbit);

    event.add("mypack:quick_pan", DirectorsCut.presets.pan(0, 70, 0, -90, 90, 4));
});

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("tour").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            DirectorsCut.play(player, "mypack:spawn_tour");
            return 1;
        })
    );
});
