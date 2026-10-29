package se233.advpro2.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {
    private GameSession game;
    @BeforeEach void setup() { game = new GameSession(new Random(7)); game.start(); }
    private void hit(Brick brick) {
        game.launch();
        game.getBall().place(brick.getX() + Brick.WIDTH / 2, brick.getY() + Brick.HEIGHT + Ball.RADIUS + 1);
        game.getBall().velocity(0, -320);
        // One collision slice isolates this hit from neighbouring rows.
        game.tick(0.005, 0);
    }
    private void clearStage() {
        for (int i = 0; i < game.getBrickCount(); i++) {
            Brick brick = game.getBrick(i);
            while (brick.getHealth() > 0) hit(brick);
        }
    }
    private void miss() {
        game.launch(); game.getBall().place(20, GameSession.HEIGHT + 10); game.getBall().velocity(0, 320); game.tick(0.01, 0);
    }
    @Test void startInitializesRun() {
        assertEquals(GameState.READY, game.getState()); assertEquals(3, game.getLives());
        assertEquals(24, game.remainingBricks()); assertEquals(0, game.getScore()); assertEquals(1, game.getStage());
    }
    @Test void paddleCannotLeaveEitherWall() {
        for (int i = 0; i < 30; i++) game.tick(0.1, -1);
        assertEquals(0, game.getPaddle().getX());
        for (int i = 0; i < 30; i++) game.tick(0.1, 1);
        assertEquals(800 - game.getPaddle().getWidth(), game.getPaddle().getX());
    }
    @Test void readyBallTracksPaddle() {
        game.tick(0.1, 1);
        assertEquals(game.getPaddle().getX() + game.getPaddle().getWidth() / 2, game.getBall().getX());
    }
    @Test void launchMovesBallUpward() {
        game.launch(); double y = game.getBall().getY(); game.tick(0.016, 0);
        assertEquals(GameState.PLAYING, game.getState()); assertTrue(game.getBall().getY() < y);
    }
    @Test void ballReflectsAtLeftRightAndTop() {
        game.launch();
        game.getBall().place(9, 350); game.getBall().velocity(-300, 0); game.tick(0.01, 0); assertTrue(game.getBall().getVx() > 0);
        game.getBall().place(791, 350); game.getBall().velocity(300, 0); game.tick(0.01, 0); assertTrue(game.getBall().getVx() < 0);
        game.getBall().place(400, 9); game.getBall().velocity(0, -300); game.tick(0.01, 0); assertTrue(game.getBall().getVy() > 0);
    }
    @Test void paddleReturnPreservesSpeedAndPointsUp() {
        game.launch(); game.getBall().place(420, Paddle.Y - Ball.RADIUS - 1); game.getBall().velocity(0, 320);
        game.tick(0.01, 0);
        assertTrue(game.getBall().getVy() < 0); assertTrue(game.getBall().getVx() > 0);
        assertEquals(320, Math.hypot(game.getBall().getVx(), game.getBall().getVy()), 0.001);
    }
    @Test void upwardBallDoesNotBounceOffPaddleAgain() {
        game.launch(); game.getBall().place(400, Paddle.Y - Ball.RADIUS); game.getBall().velocity(0, -320);
        game.tick(0.01, 0); assertTrue(game.getBall().getVy() < 0);
    }
    @Test void destroyingBonusBrickAwardsExactlyTwoHundred() {
        hit(game.getBrick(0)); assertEquals(200, game.getScore()); assertEquals(23, game.remainingBricks());
        assertTrue(game.getBall().getVy() > 0); assertEquals(10, game.getCharge());
    }
    @Test void sideCollisionReversesHorizontalVelocity() {
        game.launch(); Brick brick = game.getBrick(0);
        game.getBall().place(brick.getX() - Ball.RADIUS - 1, brick.getY() + 12); game.getBall().velocity(320, 0);
        game.tick(0.01, 0); assertTrue(game.getBall().getVx() < 0); assertEquals(0, brick.getHealth());
    }
    @Test void fiveBrickComboAddsBonusAndChargesLaser() {
        for (int i = 0; i < 5; i++) hit(game.getBrick(i));
        assertEquals(750, game.getScore()); assertEquals(50, game.getCharge()); assertEquals(5, game.getCombo());
    }
    @Test void missCostsOneLifeAndRequiresRelaunch() {
        hit(game.getBrick(0)); miss();
        assertEquals(2, game.getLives()); assertEquals(GameState.READY, game.getState());
        assertEquals(0, game.getCombo()); assertEquals(200, game.getScore());
        game.tick(0.1, 0); assertEquals(2, game.getLives());
    }
    @Test void thirdMissEndsRunAndStopsPhysics() {
        miss(); miss(); miss(); assertEquals(0, game.getLives()); assertEquals(GameState.LOST, game.getState());
        game.tick(0.1, 1); assertEquals(0, game.getLives());
    }
    @Test void pauseFreezesPhysicsAndLaserTimer() {
        for (int i = 0; i < 10; i++) hit(game.getBrick(i));
        game.activateLaser(); game.togglePause(); double x = game.getBall().getX();
        game.tick(0.1, 1); assertEquals(x, game.getBall().getX()); assertEquals(6, game.getLaserTime());
        game.togglePause(); assertEquals(GameState.PLAYING, game.getState());
    }
    @Test void readyPauseResumesToReady() { game.togglePause(); game.togglePause(); assertEquals(GameState.READY, game.getState()); }
    @Test void clearStageOffersThreeDistinctChoicesAndStopsPlay() {
        clearStage(); assertEquals(GameState.UPGRADE, game.getState());
        Upgrade[] choices = game.getOffers(); assertEquals(3, choices.length);
        assertNotEquals(choices[0], choices[1]); assertNotEquals(choices[1], choices[2]); assertNotEquals(choices[0], choices[2]);
        choices[0] = null; assertNotNull(game.getOffers()[0]);
        double x = game.getPaddle().getX(); game.tick(0.1, 1); assertEquals(x, game.getPaddle().getX());
    }
    @Test void upgradeAdvancesStageWithoutResettingScoreOrLives() {
        clearStage(); int score = game.getScore(); game.chooseUpgrade(game.getOffers()[0]);
        assertEquals(2, game.getStage()); assertEquals(32, game.remainingBricks()); assertEquals(score, game.getScore());
        assertEquals(GameState.READY, game.getState());
    }
    @Test void strongBrickRequiresTwoHitsAndScoresOnlyOnDestruction() {
        clearStage(); game.chooseUpgrade(game.getOffers()[0]);
        Brick brick = game.getBrick(0); int score = game.getScore();
        assertEquals(2, brick.getHealth()); hit(brick); assertEquals(1, brick.getHealth()); assertEquals(score, game.getScore());
        hit(brick); assertEquals(0, brick.getHealth()); assertTrue(game.getScore() > score);
    }
    @Test void allFiveUpgradeTypesHaveRealEffects() {
        for (Upgrade wanted : Upgrade.values()) {
            boolean found = false;
            for (int seed = 0; seed < 100 && !found; seed++) {
                game = new GameSession(new Random(seed)); game.start(); clearStage();
                for (Upgrade offer : game.getOffers()) {
                    if (offer != wanted) continue;
                    found = true; game.chooseUpgrade(wanted);
                    if (wanted == Upgrade.WIDE_PADDLE) assertEquals(140, game.getPaddle().getWidth());
                    if (wanted == Upgrade.QUICK_PADDLE) assertEquals(540, game.getPaddle().getSpeed());
                    if (wanted == Upgrade.EXTRA_LIFE) assertEquals(4, game.getLives());
                    if (wanted == Upgrade.SLOW_BALL) {
                        game.launch(); assertEquals(355 * 0.85, Math.hypot(game.getBall().getVx(), game.getBall().getVy()), 0.001);
                    }
                    if (wanted == Upgrade.SCORE_BOOST) {
                        int before = game.getScore(); hit(game.getBrick(1));
                        // Stage one ended at combo 24, so this destruction also earns a combo bonus.
                        assertEquals(300, game.getScore() - before);
                    }
                }
            }
            assertTrue(found, "Upgrade never offered: " + wanted);
        }
    }
    @Test void finalStageWinsWithoutAnotherUpgrade() {
        clearStage(); game.chooseUpgrade(game.getOffers()[0]); clearStage(); game.chooseUpgrade(game.getOffers()[0]); clearStage();
        assertEquals(GameState.WON, game.getState()); assertEquals(3, game.getStage()); assertEquals(0, game.remainingBricks());
    }
    @Test void invalidUpgradeSelectionIsRejected() {
        assertThrows(GameException.class, () -> game.chooseUpgrade(Upgrade.EXTRA_LIFE));
        clearStage(); assertThrows(GameException.class, () -> game.chooseUpgrade(null));
        for (Upgrade upgrade : Upgrade.values()) {
            boolean offered = false; for (Upgrade offer : game.getOffers()) if (offer == upgrade) offered = true;
            if (!offered) assertThrows(GameException.class, () -> game.chooseUpgrade(upgrade));
        }
    }
    @Test void laserNeedsChargeAndSpawnsPairWhenActivated() {
        game.launch(); game.activateLaser(); assertEquals(0, game.getLaserTime());
        for (int i = 0; i < 10; i++) hit(game.getBrick(i));
        game.activateLaser(); assertEquals(0, game.getCharge()); assertEquals(6, game.getLaserTime());
        game.tick(0.01, 0); int active = 0;
        for (int i = 0; i < game.getLaserCount(); i++) if (game.getLaser(i) != null && game.getLaser(i).isActive()) active++;
        assertEquals(2, active);
    }
    @Test void laserCanDestroyBricksAndExpiresAfterSixSeconds() {
        for (int i = 0; i < 10; i++) hit(game.getBrick(i));
        game.activateLaser(); int before = game.getScore();
        for (int i = 0; i < 61; i++) {
            // Keep the ball away from the bricks so only lasers can score.
            game.getBall().place(400, 400); game.getBall().velocity(0, 0); game.tick(0.1, 0);
        }
        assertTrue(game.getScore() > before); assertEquals(0, game.getLaserTime(), 0.0001);
    }
    private void waitWithoutBallHits(int ticks) {
        for (int i = 0; i < ticks; i++) {
            game.getBall().place(400, 400);
            game.getBall().velocity(0, 0);
            game.tick(0.1, 0);
        }
    }
    private void chargeAndFire() {
        for (int i = 0; i < 10; i++) hit(game.getBrick(i));
        assertEquals(100, game.getCharge());
        game.activateLaser();
    }
    @Test void nineBricksAreNotEnoughToActivateLaser() {
        for (int i = 0; i < 9; i++) hit(game.getBrick(i));
        game.activateLaser();
        assertEquals(90, game.getCharge()); assertEquals(0, game.getLaserTime());
    }
    @Test void firingAndCooldownBlockChargeAndRepeatedActivation() {
        chargeAndFire();
        hit(game.getBrick(10));
        assertEquals(0, game.getCharge());
        double remaining = game.getLaserTime();
        game.activateLaser(); assertEquals(remaining, game.getLaserTime());
        waitWithoutBallHits(61);
        assertEquals(0, game.getLaserTime());
        assertEquals(0, game.getCharge(), "Laser kills must not refill charge");
        assertTrue(game.getLaserCooldown() > 7 && game.getLaserCooldown() <= 8);
        assertEquals(1, game.getBrick(15).getHealth());
        hit(game.getBrick(15));
        assertEquals(0, game.getCharge());
        game.activateLaser(); assertEquals(0, game.getLaserTime());
        waitWithoutBallHits(81);
        assertEquals(0, game.getLaserCooldown());
        assertEquals(0, game.getCharge(), "Recovery must not grant a free refill");
        game.activateLaser(); assertEquals(0, game.getLaserTime());
        assertEquals(1, game.getBrick(14).getHealth());
        hit(game.getBrick(14)); assertEquals(10, game.getCharge());
    }
    @Test void cooldownSurvivesLifeLossAndFreezesWhileReadyOrPaused() {
        chargeAndFire(); miss();
        assertEquals(8, game.getLaserCooldown());
        game.tick(0.1, 0); assertEquals(8, game.getLaserCooldown());
        game.launch(); game.togglePause();
        game.tick(0.1, 0); assertEquals(8, game.getLaserCooldown());
        game.togglePause(); waitWithoutBallHits(1);
        assertEquals(7.9, game.getLaserCooldown(), 0.0001);
    }
    @Test void stageChangeCannotBypassCooldownAndNewRunResetsIt() {
        chargeAndFire(); clearStage();
        assertEquals(GameState.UPGRADE, game.getState());
        assertEquals(8, game.getLaserCooldown());
        game.chooseUpgrade(game.getOffers()[0]);
        assertEquals(8, game.getLaserCooldown()); assertEquals(0, game.getCharge());
        game.start(); assertEquals(0, game.getLaserCooldown()); assertEquals(0, game.getCharge());
    }
    @Test void newRunResetsEveryRunResource() {
        clearStage(); game.chooseUpgrade(game.getOffers()[0]); game.start();
        assertEquals(0, game.getScore()); assertEquals(0, game.getCharge()); assertEquals(3, game.getLives());
        assertEquals(112, game.getPaddle().getWidth()); assertEquals(460, game.getPaddle().getSpeed()); assertEquals(1, game.getStage());
    }
    @Test void invalidTickInputAndNullRandomAreRejected() {
        assertThrows(GameException.class, () -> game.tick(-1, 0)); assertThrows(GameException.class, () -> game.tick(Double.NaN, 0));
        assertThrows(GameException.class, () -> game.tick(Double.POSITIVE_INFINITY, 0)); assertThrows(GameException.class, () -> game.tick(1, 0));
        assertThrows(GameException.class, () -> game.tick(0.01, 2)); assertThrows(GameException.class, () -> new GameSession(null));
    }
    @Test void menuStopsRunAndClearsAttack() {
        game.launch(); game.menu(); game.tick(0.1, 1); assertEquals(GameState.MENU, game.getState()); assertEquals(0, game.getLaserTime());
    }
}

