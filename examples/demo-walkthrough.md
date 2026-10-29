# Demonstration walkthrough

1. Open `run.cmd`, choose Start a new run. Confirm score 0, stage 1/3, lives 3, 24 bricks.
2. Move with A/D or arrows before launch: the docked ball follows the paddle and neither leaves the arena.
3. Press Space. Return the ball at different paddle positions to demonstrate different bounce directions.
4. Destroy a cyan brick (100 points), a gold brick (200 points), and show the hit flash. Every fifth destruction without a missed ball adds 150 points.
5. Destroy ten bricks with the ball to reach 100% charge and press X while playing. Two lasers fire automatically for six seconds, followed by eight seconds of cooldown. Charge stays at zero throughout firing and cooldown; only later ball kills recharge it. Pause to show that both timers freeze.
6. Switch focus to another application. Confirm automatic pause. Return and resume.
7. Clear the stage. Explain why physics stops and why the three offered upgrades are distinct. Select one and demonstrate its effect in stage 2; purple armour needs two hits.
8. Finish stage 3 to show victory, or deliberately miss three balls to show the loss screen. Replay resets score, upgrades and lives.
9. Show `docs/lecture-to-code.md`, then run `test.cmd`. Explain a collision test, upgrade test and the clock scheduling tests.
10. Build with `tools\maven.cmd clean package`, then launch `java -jar target/advpro-2-1.0.0.jar` using JDK 24+.

The game has no external input-file format. These interactive examples replace unnecessary sample data files. Complete steps 5 and 7-8 manually before the onsite demonstration; automated model tests cover their rules but are not a substitute for a full human playthrough.
