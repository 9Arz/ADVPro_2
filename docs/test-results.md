# Verification results

## Laser balance update - 29 October 2026

`tools\maven.cmd -o package` passed after the cooldown change: **35 tests, 0 failures, 0 errors, 0 skipped** (GameSessionTest 29, BrickTest 4, GameLoopTest 2). The executable JAR was rebuilt. This update was compile/model-tested; the changed cooldown label has not been visually rechecked.

New regression tests cover ten-brick charging, rejected early/repeated activation, no charge from laser kills or ball kills during firing/cooldown, eight-second recovery, zero automatic refill, post-recovery ball charging, pause/ready timer freezing, life/stage transitions and new-run reset. Test fixtures use surviving off-axis bricks so laser destruction cannot invalidate the recharge check.

The records below describe the original build and its GUI checks.


Verified 29 October 2026 on Windows with JDK 26.0.1 and cached Maven 3.8.5. All dependencies and plugin versions are those requested in the prompt. Initial offline builds used `-o`; the JavaFX launch plugin needed an online dependency download before it could run.

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| se233.advpro2.controller.GameLoopTest | 2 | 0 | 0 | 0 |
| se233.advpro2.model.BrickTest | 4 | 0 | 0 | 0 |
| se233.advpro2.model.GameSessionTest | 25 | 0 | 0 | 0 |

**Total: 31 tests passed.** Counts are extracted from actual Maven Surefire XML reports. `target/surefire-reports` contains detailed output after building.

## Executed checks

- `mvn -o clean compile`: passed, release 24 compilation.
- `mvn -o clean test`: passed, 31 tests.
- `mvn -o clean package`: passed, tests reran and shaded JAR was produced.
- `mvn javafx:run`: launched a real JavaFX window after fetching missing plugin dependencies.
- `java -jar target/advpro-2-1.0.0.jar`: opened successfully; real GUI inspected and closed with exit code 0.
- `tools\maven.cmd -o validate`: passed using the cached Maven/JDK fallback without Maven on PATH.
- `run.cmd`: launched the real application using the cached Maven/JDK helper. The application was left open for the user.
- The separate Maven launch was closed normally and reported BUILD SUCCESS with exit code 0.
- GUI checked: legible layout, gameplay/HUD, score changes, life loss, resume button, Space launch, loss screen and replay reset. `docs/images/gameplay.png` is a capture of the actual application.

## Test coverage

Normal/boundary/error cases include initialization, launch, paddle limits, docked ball tracking, left/right/top bounce, paddle return direction and speed, no second upward paddle bounce, side collision, brick destruction, bonus scoring, combo/charge, single-life loss, game over, pause/resume, stage progression, all five upgrade effects, three distinct offers, defensive copy of offers, invalid upgrade choices, armoured brick damage, final victory, laser eligibility/projectile spawning/scoring/expiry, restart, menu, malformed tick input and missing random source. Brick tests cover repeated-hit scoring safety, strict overlap edges, invalid health/points and flash expiry. Clock tests cover queue bounds and shutdown scheduling.

The first test run exposed an inaccurate collision fixture: a 0.01-second test tick had two physics slices, allowing the ball to hit a second adjacent row. The single-hit fixture now uses one 0.005-second slice; expected scoring results were preserved. Final tests all pass.

## Practical limits

No complete three-stage human playthrough, exhaustive GUI upgrade selection, automated accessibility test, other OS, other JDK version, low-resolution display, or prolonged performance test was performed. These are not marked passed. Unit tests use model states to verify stage and win/lose behavior without a window. JavaFX classpath startup warns about the unnamed module; the executable JAR nevertheless opened and exited successfully. Maven/JDK and Shade metadata warnings are documented in README.
