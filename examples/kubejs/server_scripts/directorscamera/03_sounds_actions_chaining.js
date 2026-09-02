function bossReveal(player, bossX, bossY, bossZ) {
    const approach = DirectorsCamera.cutscene()
        .id("mypack:boss_approach")
        .setDurationSeconds(6)
        .setTimeEasing("EASE_OUT")
        .setLookEasing("EASE_IN_OUT")
        .skippable(true);

    approach.getPath()
        .addLookingAt(bossX - 30, bossY + 12, bossZ, bossX, bossY + 2, bossZ)
        .addLookingAt(bossX - 12, bossY + 5, bossZ + 4, bossX, bossY + 2, bossZ)
        .addLookingAt(bossX - 6, bossY + 3, bossZ, bossX, bossY + 2, bossZ);

    approach
        .music(0, "minecraft:music_disc.pigstep", { volume: 0.6, id: "theme" })
        .soundAtSecond(4.5, "minecraft:entity.ender_dragon.growl", { pos: [bossX, bossY, bossZ], volume: 1.2 })
        .soundAtSecond(5.5, "minecraft:entity.wither.spawn", { attachToCamera: true, pos: [bossX, bossY, bossZ] })
        .executeAtSecond(5, p => {
            p.level.spawnLightning(bossX, bossY, bossZ, true);
        })
        .executeAtSecond(6, p => {
            p.stages.add("boss_revealed");
        }, { alwaysRun: true });

    const circle = DirectorsCamera.cutscene()
        .id("mypack:boss_circle")
        .setDurationSeconds(8)
        .setEasing("LINEAR")
        .setStopMode("PLAYER");
    circle.getPath().addOrbit(bossX, bossY + 4, bossZ, 10, 180, 540, 32, true);
    circle.stopSound(0, "theme");
    circle.onEnd((p, reason) => {
        p.tell("Cutscene ended: " + reason);
    });
    circle.onSkip(p => {
        p.tell("You skipped the boss reveal");
    });

    approach.setNext(circle);
    approach.play(player);
}

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("bossreveal").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            bossReveal(player, player.x + 20, player.y, player.z);
            return 1;
        })
    );
});
