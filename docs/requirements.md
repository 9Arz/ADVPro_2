# Requirements analysis (before implementation)

Sources: `2026_233_project_II.pdf`, all four pages; `se233v2026 (6).pdf`, 413 PDF pages. Printed lecture pages are 21 less than PDF page numbers for the chapters cited below. No ADVPro_1 source, separate assignment, or external example-code archive was supplied. The coursebook contains example listings. Its embedded AI advice is source material, not an additional instruction from the user.

## Required behavior

- Start menu initializes paddle, ball, bricks, score, and three lives.
- Single-player 2D keyboard-controlled paddle; moving ball bounces off walls and paddle.
- Bricks take damage according to type; destroyed bricks award points.
- Multiple stages; clearing required bricks pauses play for random upgrade choices.
- At least three meaningful upgrades; chosen effects persist into subsequent stages.
- Missed ball costs a life; zero lives ends the run; winning and losing have feedback.
- Show score, stage, lives, damage/destruction, stage completion, upgrade and life-loss feedback.
- Sound is optional. Original artwork and exact Arkanoid balance are not required.

## Design decisions (not invented assignment requirements)

Three stages, flat Brick[] arrays, normal/armoured/bonus bricks, five upgrades with three distinct random offers, fixed 800 by 540 arena, three initial lives. Paddle width/speed, slower ball, extra life, and score boost persist. A charged six-second dual laser and a consecutive-destruction combo are additional features. No audio or downloaded artwork. Original sprite sheet has distinct movement and bounce rows. Layout uses JavaFX panes and controls, with CSS.

## Lecture boundary and architecture

Verified: chapter 1 MVC, keyboard events, enums (script 1.16), arrays (script 1.24), ArrayList (section 1.4.4), Random (case-study controller); chapter 2 custom exceptions and lambdas; chapter 3 Thread, Runnable, synchronization and Platform.runLater; chapter 4 game loops, movement, collision, sprite viewports, logging; chapter 5 JUnit 5; chapter 6 Maven, modules, Shade and JarLauncher. The supplied book also covers streams, but this project does not need them. No 2D array is necessary or assumed. Avoid reflection, reactive libraries, advanced generics, parallel streams, executors, and complex patterns.

Model: Ball, Paddle, Brick, Laser, GameSession, Upgrade, GameState, GameException. Controller: GameController handles events/errors, GameLoop schedules ticks. View: GameView builds controls and renders state; AnimatedSprite selects sheet viewports. Launcher and JarLauncher are entry points. All model mutation and rendering happen on the FX thread; a basic sleeping worker schedules at most one pending tick using synchronized flags.

## Rubric mapping and final verification checklist

| Requirement / rubric | Implementation | Verification |
|---|---|---|
| Core game and multiple stages | GameSession, GameView | Rules tested; gameplay GUI checked; full manual run pending |
| Random meaningful upgrades | Upgrade, GameSession | All five effects and distinct offers tested |
| Sprite movement / bounce and special attack | AnimatedSprite, Laser | Sheet/viewports reviewed; laser model tests pass |
| Meaningful OOP classes and relations | GameSession owns Paddle, Ball, Brick[], Laser[] | Source reviewed |
| Score and hit feedback | damageBrick, GameView | Scoring/flash tested, HUD inspected |
| Central exception handling, custom exception | GameException, GameController | Invalid input tests pass; central handler reviewed |
| Events/actions/scoring at different log levels | Log4j INFO/DEBUG/WARN/ERROR | Runtime logs and configuration inspected |
| Core, action, score, lives, upgrade tests | JUnit model and loop tests | 35 tests passed |
| Compile/run/test/executable JAR | Maven plugins, launchers | Compile/test/package passed; Maven and JAR windows opened |
| Subjective extra feature | Combo charging dual laser | Implemented quality judged by lecturer |

Both rubric variants are considered; group size is unknown. No guaranteed mark is claimed. Submission: 2 November 2026; demonstration: 3 November 2026. Student names, IDs, pairing and actual submission remain the student's responsibility.

## Final checklist

- [x] All specified functional behaviors implemented; model tests cover all run transitions.
- [x] Simple arrays/OOP/events/threading mapped to verified lecture sections.
- [x] MVC responsibilities separated; no application logic placeholders.
- [x] Requested Maven versions, module and JAR entry points configured.
- [x] Actual compilation, 35 unit tests and packaging passed.
- [x] Actual JavaFX GUI and executable JAR startup verified on Windows.
- [x] README, scripts, original resources, mapping and demonstration guide supplied.
- [ ] Full human three-stage playthrough and all upgrade UI paths.
- [ ] Verification on another OS/JDK or constrained display.
- [ ] Comparison with ADVPro_1 (reference not supplied).
- [ ] Student/pair demonstration and subjective rubric assessment.

Laser balance revision: ball-only charge of 10% per destroyed brick; six seconds firing, eight seconds cooldown, then refill from zero. No charging during firing/cooldown; life/stage transitions cannot bypass recovery. Verified by model regression tests and Maven package.
