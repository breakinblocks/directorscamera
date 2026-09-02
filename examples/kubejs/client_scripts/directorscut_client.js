DirectorsCutClientEvents.started(event => {
    console.log("Cutscene started on the client: " + event.id);
});

DirectorsCutClientEvents.ended(event => {
    console.log("Cutscene " + event.id + " ended (" + event.reason + ")");
});

console.log("2 + 3 * 4 = " + DirectorsCut.evaluate("2 + 3 * 4", {}));
console.log("sin(v) = " + DirectorsCut.evaluate("math.sin(v)", { v: 90 }));
