# Class responsibilities and lecture compliance

References below use **printed coursebook pages**; PDF viewer pages are printed page +21. Only verified sections/listings in the supplied `se233v2026 (6).pdf` are claimed. API helper calls are applications of these concepts, not claims that every helper appeared verbatim in a lecture listing.

| Class / enum | Responsibility | Lecture evidence |
|---|---|---|
| Launcher | JavaFX lifecycle, scene, focus handling, start/stop worker | Ch. 1 JavaFX/events; Ch. 4 script 4.14, 4.18 (pp. 220, 224) |
| JarLauncher | Plain main method delegates to Application | Ch. 6 script 6.10 (p. 348) |
| GameSession | Run state, flat brick/laser arrays, collision, scoring, lives and upgrade effects | Ch. 1 MVC (1.4); array example pp. 42-43; Ch. 4 movement/collision pp. 211-213, game loop pp. 221-227 |
| Ball | Position, velocity and directional bounce | Ch. 4 movement and collision (4.2.3-4.2.4) |
| Paddle | Movement, boundary clamp, width and speed | Ch. 4 keyboard movement (4.3.2) |
| Brick | Health, overlap test, score value and hit flash | Ch. 1 encapsulation/class relations (1.4.1); Ch. 4 collision |
| Laser | Projectile position and active lifetime | Same position/velocity and object concepts as Ball |
| Upgrade, GameState | Named upgrade choices and game states | Ch. 1 enum example, script 1.16 (p. 37) |
| GameException | One application-specific validation exception | Ch. 2 customized exceptions, 2.2.3 (pp. 81-83) |
| GameController | Keyboard flags, button actions, clear keys on focus loss, one exception handler | Ch. 1 keyboard events (1.3.3); Ch. 2 exception propagation/catching and lambdas; Ch. 4 script 4.21 (p. 227) |
| GameLoop | Sleeping Runnable worker and bounded FX scheduling | Ch. 2 background process (2.4.8) uses Platform.runLater; Ch. 3 synchronization (pp. 148-150); Ch. 4 script 4.15 (p. 222) |
| GameView | Layout, HUD, menu/upgrade/result overlays, render model | Ch. 1 panes/controls and view components (1.4.2) |
| AnimatedSprite | ImageView with separate move/bounce rows and Rectangle2D viewport | Ch. 4 interpolation, scripts 4.11, 4.2.5 (pp. 214-215) |
| Three test classes | Rule, boundary, error and scheduling assertions | Ch. 5 JUnit 5 (5.2.3), assertions pp. 265-267 |
| pom.xml, module-info.java, logging config | Dependency/build lifecycle and structured logs | Ch. 4 logging (4.1.2, 4.3.7); Ch. 6 case study and Shade pp. 345-349 |

## Data and algorithms

- One-dimensional `Brick[]`, `Laser[]`, `Upgrade[]` and view-node arrays. Integer division/modulo convert a flat brick index into its displayed row/column. This does not require a two-dimensional array.
- Simple loops scan bricks for overlap and stage completion; at most 40 bricks and 40 laser slots. No spatial tree or advanced collection is needed.
- Random choices use three loop iterations swapping a randomly selected remaining entry into the offered prefix. `Random` occurs in the chapter 1 case-study controller; the no-duplicate selection algorithm is a small application of arrays and loops.
- Ball movement adds velocity times a short time interval. A previous edge identifies the impacted face; negating one velocity component reflects the ball. Paddle hit position controls outgoing horizontal velocity. These are original game rules using the chapter 4 movement/collision concepts.
- Enum switch applies one upgrade. The UI uses basic lambdas to wire events; `Consumer<Upgrade>` is a single callback (functional interfaces/lambdas, chapter 2), not an advanced generic abstraction.
- No streams, parallel streams, reflection, reactive framework, advanced collection, executor pool, dependency injection framework or third-party physics/testing UI framework is used in application source. Dependencies may internally use techniques beyond this project.

## Thread ownership

`GameLoop` sleeps on a worker thread. `reserveTick()` uses a synchronized flag so no second update is queued until the first completes. `Platform.runLater` sends one callback to the JavaFX thread. The model, keys and scene graph are only read/mutated by that thread. `stop()` and interruption end scheduling. There is no shared mutable game model between threads and no busy loop. This adapts the lecture worker-loop example to obey JavaFX's UI-thread rule.

## Demonstration trace

Trace `GameController.tick -> GameSession.tick -> moveBall -> damageBrick -> checkStageComplete`. On the last brick, state changes to UPGRADE or WON; subsequent ticks skip physics. A choice changes a model property and prepares the next stage. On a miss, lives decrease exactly once and state changes to READY or LOST. Trace the tests alongside these methods, especially the previous-position collision checks and the pending-tick flag.

## Scope notes

The provided book actually includes streams, parallelism and more complex concurrent APIs; their presence does not require using them. The implementation deliberately stays with the simpler examples above. No separate ADVPro_1 implementation or assignment/example archive was present. Sprite artwork is original programmatically drawn geometry; no third-party asset attribution is needed. The Python/PowerShell tooling used during development is not part of the Java application or its lecture-compliance claim.
