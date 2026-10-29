package se233.advpro2.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BrickTest {
    @Test void repeatedHitsCannotAwardDestructionTwice() {
        Brick brick = new Brick(10, 20, 2, 100);
        assertFalse(brick.hit()); assertTrue(brick.hit()); assertFalse(brick.hit()); assertEquals(0, brick.getHealth());
    }
    @Test void touchingEdgeIsNotOverlapButCrossingItIs() {
        Brick brick = new Brick(10, 20, 1, 100);
        assertFalse(brick.overlaps(0, 20, 10, 10)); assertTrue(brick.overlaps(1, 20, 10, 10));
        brick.hit(); assertFalse(brick.overlaps(10, 20, 10, 10));
    }
    @Test void invalidHealthAndPointsRejected() {
        assertThrows(GameException.class, () -> new Brick(0, 0, 0, 100));
        assertThrows(GameException.class, () -> new Brick(0, 0, 1, -1));
    }
    @Test void hitFlashExpires() { Brick b = new Brick(0, 0, 1, 100); b.hit(); b.tick(0.2); assertEquals(0, b.getFlash()); }
}
