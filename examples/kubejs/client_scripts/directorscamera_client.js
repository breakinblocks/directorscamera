DirectorsCameraClientEvents.started(event => {
    console.log("Cutscene started on the client: " + event.id);
});

DirectorsCameraClientEvents.ended(event => {
    console.log("Cutscene " + event.id + " ended (" + event.reason + ")");
});

console.log("2 + 3 * 4 = " + DirectorsCamera.evaluate("2 + 3 * 4", {}));
console.log("sin(v) = " + DirectorsCamera.evaluate("math.sin(v)", { v: 90 }));
