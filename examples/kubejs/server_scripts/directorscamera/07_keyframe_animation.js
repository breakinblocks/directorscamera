const CAMERA_ANIMATION = JSON.stringify({
    loop: true,
    animation_length: 4,
    bedrock_conventions: false,
    bones: {
        camera: {
            position: {
                "0.0": [0, 6, -10],
                "2.0": { post: [10, 8, 0], lerp_mode: "catmullrom" },
                "4.0": [0, 6, -10]
            },
            rotation: ["math.sin(query.anim_time * 45) * 10", "query.anim_time * 90", "0"]
        }
    }
});

DirectorsCamera.parseAnimation("mypack:scripted_orbit", CAMERA_ANIMATION, false);

ServerEvents.commandRegistry(event => {
    const { commands: Commands } = event;
    event.register(
        Commands.literal("animcutscene").executes(ctx => {
            const player = ctx.getSource().getPlayerOrException();
            const origin = DirectorsCamera.vec(player.x, player.y, player.z);
            const system = DirectorsCamera.animationSystem();
            system.startAnimation("main", DirectorsCamera.ticker("mypack:scripted_orbit").setLoopMode("ONCE").build());
            const pose = DirectorsCamera.pose();

            const cutscene = DirectorsCamera.cutscene().setDurationSeconds(4).setCurve("LINEAR").setEasing("LINEAR");
            for (let tick = 0; tick <= 80; tick += 4) {
                system.applyAnimations(pose, 0);
                cutscene.getPath().addKeyframe(pose.cameraPos("camera", origin));
                for (let i = 0; i < 4; i++) {
                    system.tick();
                }
            }
            cutscene.play(player);
            return 1;
        })
    );
});
