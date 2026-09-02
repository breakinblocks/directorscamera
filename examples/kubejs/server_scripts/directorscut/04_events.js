DirectorsCutEvents.beforePlay(event => {
    if (event.player.level.dimension != "minecraft:overworld" && event.id == "mypack:spawn_tour") {
        event.player.tell("The spawn tour only plays in the Overworld");
        event.cancel();
        return;
    }
    if (event.player.isCreative()) {
        event.cutscene.skippable(true);
    }
});

DirectorsCutEvents.started(event => {
    console.log(`${event.player.username} started cutscene ${event.id}`);
});

DirectorsCutEvents.tick(event => {
    if (event.id == "mypack:spawn_tour" && event.tick == 100) {
        event.player.tell("Halfway through the tour");
    }
});

DirectorsCutEvents.ended(event => {
    if (event.finished) {
        event.player.stages.add("watched_" + event.id.replace(":", "_"));
    } else if (event.skipped) {
        console.log(`${event.player.username} skipped ${event.id}`);
    }
});
