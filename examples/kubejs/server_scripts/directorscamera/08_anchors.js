DirectorsCameraEvents.register(event => {
    const reveal = event.create("mypack:throne_room");
    reveal
        .setDurationSeconds(9)
        .setCurve("CATMULLROM")
        .setEasing("EASE_IN_OUT")
        .anchored("throne_room")
        .startFromPlayer()
        .endAtPlayer();

    reveal.getPath()
        .addLookingAt(0, 4, -6, 0, 2, 6)
        .addLookingAt(-5, 5, 0, 0, 2, 6)
        .addLookingAt(0, 3, 3, 0, 2, 6);

    reveal.soundAtSecond(3, "minecraft:block.beacon.activate", { pos: [0, 2, 6] });
    reveal.executeAtSecond(5, player => {
        const frame = DirectorsCamera.frameOf(player);
        if (frame) {
            const spot = frame.pos(0, 1, 5);
            player.level.spawnLightning(spot.x(), spot.y(), spot.z(), true);
        }
    });

    const entrance = event.create("mypack:room_entrance");
    entrance.setDurationSeconds(4).anchoredToPlayer().startFromPlayer();
    entrance.getPath()
        .addPoint(0, 3, -4, 0, 30, 0)
        .addPoint(0, 2, 6, 0, 10, 0);
});

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("throneroom").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            if (!DirectorsCamera.playAnchored(player, "mypack:throne_room", "throne_room")) {
                player.tell("No throne_room anchor nearby");
                return 0;
            }
            return 1;
        })
    );
    event.register(
        Commands.literal("virtualanchor").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            DirectorsCamera.registerAnchor(player.level, "throne_room", player.x, player.y, player.z, player.yaw);
            player.tell("Registered a virtual throne_room anchor at your position");
            return 1;
        })
    );
});
