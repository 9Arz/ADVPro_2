package se233.advpro2.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameLoopTest {
    @Test void pendingTickPreventsUnboundedQueue() {
        GameLoop loop = new GameLoop(() -> {});
        assertTrue(loop.reserveTick()); assertFalse(loop.reserveTick());
        loop.finishTick(); assertTrue(loop.reserveTick());
    }
    @Test void stoppingPreventsFurtherScheduling() {
        GameLoop loop = new GameLoop(() -> {}); loop.stop();
        assertFalse(loop.isRunning()); assertFalse(loop.reserveTick());
    }
}
