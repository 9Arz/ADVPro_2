package se233.advpro2.model;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Pure Java game rules. Coordinates use pixels, time uses seconds. */
public class GameSession {
    public static final int WIDTH = 800, HEIGHT = 540, STAGES = 3;
    private static final Logger LOG = LogManager.getLogger(GameSession.class);
    private final Random random;
    private GameState state = GameState.MENU;
    private GameState beforePause = GameState.READY;
    private Paddle paddle = new Paddle();
    private Ball ball = new Ball();
    private Brick[] bricks = new Brick[0];
    private Laser[] lasers = new Laser[40];
    private Upgrade[] offers = new Upgrade[0];
    private int stage, score, lives, combo, charge, scoreBonus;
    private double speedFactor = 1, laserTime, laserCooldown, shotDelay;
    private String message = "Break bricks. Build your run.";

    public GameSession() { this(new Random()); }
    public GameSession(Random random) {
        if (random == null) throw new GameException("A random generator is required.");
        this.random = random;
    }
    public void start() {
        paddle = new Paddle(); ball = new Ball();
        stage = 1; score = 0; lives = 3; combo = 0; charge = 0; scoreBonus = 0; speedFactor = 1;
        laserTime = 0; laserCooldown = 0;
        prepareStage();
        LOG.info("New run: three lives");
    }
    public void menu() { state = GameState.MENU; clearLasers(); message = "Break bricks. Build your run."; }
    private void prepareStage() {
        int rows = stage + 2;
        bricks = new Brick[rows * 8];
        for (int i = 0; i < bricks.length; i++) {
            int row = i / 8, column = i % 8;
            int health = (stage > 1 && (row + column) % 3 == 0) ? 2 : 1;
            int points = (i % 7 == 0) ? 200 : 100;
            bricks[i] = new Brick(52 + column * 88, 60 + row * 34, health, points);
        }
        offers = new Upgrade[0]; clearLasers(); paddle.center(); dockBall();
        state = GameState.READY;
        message = "Stage " + stage + " ready - press SPACE to launch";
        LOG.info("Stage {} prepared with {} bricks", stage, bricks.length);
    }
    private void dockBall() { ball.place(paddle.getX() + paddle.getWidth() / 2, Paddle.Y - Ball.RADIUS - 1); ball.velocity(0, 0); }
    private double ballSpeed() { return (320 + (stage - 1) * 35) * speedFactor; }
    public void launch() {
        if (state != GameState.READY) return;
        ball.velocity(ballSpeed() * 0.35, -ballSpeed() * Math.sqrt(1 - 0.35 * 0.35));
        state = GameState.PLAYING; message = "Keep the ball in play. Charge the dual laser!";
        LOG.debug("Ball launched at speed {}", ballSpeed());
    }
    public void togglePause() {
        if (state == GameState.PAUSED) { state = beforePause; message = "Resumed"; }
        else if (state == GameState.PLAYING || state == GameState.READY) {
            beforePause = state; state = GameState.PAUSED; message = "Paused";
        }
    }
    public void pause() { if (state == GameState.PLAYING || state == GameState.READY) togglePause(); }
    public void tick(double seconds, int direction) {
        if (!Double.isFinite(seconds) || seconds < 0 || seconds > 0.1 || direction < -1 || direction > 1)
            throw new GameException("Tick needs 0-0.1 seconds and a direction from -1 to 1.");
        if (state != GameState.READY && state != GameState.PLAYING) return;
        // Small fixed slices prevent a fast ball from crossing a brick between checks.
        int steps = Math.max(1, (int) Math.ceil(seconds * 120));
        double step = seconds / steps;
        for (int i = 0; i < steps; i++) {
            if (state != GameState.READY && state != GameState.PLAYING) break;
            paddle.move(direction, step);
            for (Brick brick : bricks) brick.tick(step);
            if (state == GameState.READY) { dockBall(); continue; }
            moveBall(step);
            if (state != GameState.PLAYING) break;
            updateLasers(step);
            checkStageComplete();
        }
    }
    private void moveBall(double seconds) {
        double oldX = ball.getX(), oldY = ball.getY(), r = Ball.RADIUS;
        ball.move(seconds);
        if (ball.getX() < r && ball.getVx() < 0) { ball.place(r, ball.getY()); ball.bounceX(); }
        if (ball.getX() > WIDTH - r && ball.getVx() > 0) { ball.place(WIDTH - r, ball.getY()); ball.bounceX(); }
        if (ball.getY() < r && ball.getVy() < 0) { ball.place(ball.getX(), r); ball.bounceY(); }
        if (ball.getVy() > 0 && oldY + r <= Paddle.Y && ball.getY() + r >= Paddle.Y
                && ball.getX() + r > paddle.getX() && ball.getX() - r < paddle.getX() + paddle.getWidth()) {
            ball.place(ball.getX(), Paddle.Y - r);
            double offset = (ball.getX() - paddle.getX() - paddle.getWidth() / 2) / (paddle.getWidth() / 2);
            ball.bounceFromPaddle(offset, ballSpeed());
            LOG.debug("Paddle return at offset {}", offset);
        }
        for (Brick brick : bricks) {
            if (!brick.overlaps(ball.getX() - r, ball.getY() - r, 2 * r, 2 * r)) continue;
            // Use the previous edge to identify which face the ball crossed.
            if (oldY + r <= brick.getY()) { ball.place(ball.getX(), brick.getY() - r); ball.bounceY(); }
            else if (oldY - r >= brick.getY() + Brick.HEIGHT) { ball.place(ball.getX(), brick.getY() + Brick.HEIGHT + r); ball.bounceY(); }
            else if (oldX < brick.getX()) { ball.place(brick.getX() - r, ball.getY()); ball.bounceX(); }
            else { ball.place(brick.getX() + Brick.WIDTH + r, ball.getY()); ball.bounceX(); }
            damageBrick(brick, true);
            break;
        }
        if (ball.getY() - r > HEIGHT) loseLife();
    }
    private void damageBrick(Brick brick, boolean hitByBall) {
        if (brick.hit()) {
            combo++;
            // Laser kills cannot refill their own weapon, even after the shot has travelled.
            if (hitByBall && laserTime == 0 && laserCooldown == 0) {
                charge = Math.min(100, charge + 10);
            }
            int earned = brick.getPoints() + scoreBonus;
            if (combo % 5 == 0) earned += 150;
            score += earned;
            message = "+" + earned + " points" + (combo % 5 == 0 ? " - combo bonus!" : "");
            LOG.info("Brick destroyed: +{}, score {}", earned, score);
        } else { message = "Armour cracked"; LOG.debug("Brick damaged; {} hit remaining", brick.getHealth()); }
    }
    private void loseLife() {
        lives--; combo = 0; clearLasers();
        LOG.warn("Ball missed: {} lives remain", lives);
        if (lives == 0) { state = GameState.LOST; message = "Run over. Try a different upgrade build!"; }
        else { state = GameState.READY; dockBall(); message = "Life lost - press SPACE when ready"; }
    }
    private void checkStageComplete() {
        for (Brick brick : bricks) if (brick.getHealth() > 0) return;
        clearLasers();
        if (stage == STAGES) { state = GameState.WON; message = "All three stages cleared!"; LOG.info("Run won: {} points", score); }
        else {
            state = GameState.UPGRADE;
            Upgrade[] pool = Upgrade.values();
            offers = new Upgrade[3];
            for (int i = 0; i < offers.length; i++) {
                int pick = i + random.nextInt(pool.length - i);
                Upgrade temp = pool[i]; pool[i] = pool[pick]; pool[pick] = temp;
                offers[i] = pool[i];
            }
            message = "Stage cleared! Choose one upgrade for this run.";
            LOG.info("Stage {} complete", stage);
        }
    }
    public void chooseUpgrade(Upgrade upgrade) {
        if (state != GameState.UPGRADE) throw new GameException("Upgrades can only be chosen after a stage.");
        boolean offered = false;
        for (Upgrade offer : offers) if (offer == upgrade) offered = true;
        if (!offered) throw new GameException("Choose one of the three offered upgrades.");
        switch (upgrade) {
            case WIDE_PADDLE: paddle.widen(); break;
            case QUICK_PADDLE: paddle.accelerate(); break;
            case SLOW_BALL: speedFactor *= 0.85; break;
            case EXTRA_LIFE: lives++; break;
            case SCORE_BOOST: scoreBonus += 50; break;
        }
        stage++; prepareStage();
        message = upgrade.getTitle() + " acquired - SPACE launches stage " + stage;
        LOG.info("Upgrade selected: {}", upgrade);
    }
    public void activateLaser() {
        if (state != GameState.PLAYING || charge < 100 || laserTime > 0 || laserCooldown > 0) return;
        charge = 0; laserTime = 6; shotDelay = 0;
        message = "Dual laser active for 6 seconds!";
        LOG.info("Dual laser activated");
    }
    private void updateLasers(double seconds) {
        if (laserCooldown > 0) {
            laserCooldown = Math.max(0, laserCooldown - seconds);
        }
        if (laserTime > 0) {
            laserTime = Math.max(0, laserTime - seconds);
            if (laserTime == 0) {
                laserCooldown = 8;
                clearLasers();
                message = "Laser cooling down - recharge after 8 seconds of play";
                LOG.debug("Dual laser cooldown started");
                return;
            }
            shotDelay -= seconds;
            if (shotDelay <= 0) {
                addLaser(paddle.getX() + 6); addLaser(paddle.getX() + paddle.getWidth() - 10);
                shotDelay = 0.25;
            }
        }
        for (Laser laser : lasers) {
            if (laser == null || !laser.isActive()) continue;
            laser.move(seconds);
            for (Brick brick : bricks) {
                if (brick.overlaps(laser.getX(), laser.getY(), Laser.WIDTH, Laser.HEIGHT)) {
                    damageBrick(brick, false); laser.deactivate(); break;
                }
            }
        }
    }
    private void addLaser(double x) {
        for (int i = 0; i < lasers.length; i++) {
            if (lasers[i] == null || !lasers[i].isActive()) { lasers[i] = new Laser(x, Paddle.Y); return; }
        }
    }
    private void clearLasers() {
        // A missed ball or stage change must not bypass the weapon's recovery time.
        if (laserTime > 0) laserCooldown = 8;
        lasers = new Laser[40]; laserTime = 0; shotDelay = 0;
    }
    public GameState getState() { return state; }
    public Paddle getPaddle() { return paddle; }
    public Ball getBall() { return ball; }
    public int getBrickCount() { return bricks.length; }
    public Brick getBrick(int index) { return bricks[index]; }
    public int getLaserCount() { return lasers.length; }
    public Laser getLaser(int index) { return lasers[index]; }
    public Upgrade[] getOffers() { return offers.clone(); }
    public int getStage() { return stage; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getCombo() { return combo; }
    public int getCharge() { return charge; }
    public double getLaserTime() { return laserTime; }
    public double getLaserCooldown() { return laserCooldown; }
    public String getMessage() { return message; }
    public int remainingBricks() {
        int remaining = 0;
        for (Brick brick : bricks) if (brick.getHealth() > 0) remaining++;
        return remaining;
    }
}
