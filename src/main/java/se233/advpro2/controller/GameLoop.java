package se233.advpro2.controller;

import javafx.application.Platform;

/** A sleeping worker schedules at most one tick; only the FX thread touches the game. */
public class GameLoop implements Runnable {
    private boolean running = true;
    private boolean pending;
    private final Runnable tick;
    public GameLoop(Runnable tick) { this.tick = tick; }
    public synchronized void stop() { running = false; }
    public synchronized boolean isRunning() { return running; }
    synchronized boolean reserveTick() {
        if (!running || pending) return false;
        pending = true; return true;
    }
    synchronized void finishTick() { pending = false; }
    @Override public void run() {
        while (isRunning()) {
            if (reserveTick()) {
                Platform.runLater(() -> {
                    try { if (isRunning()) tick.run(); }
                    finally { finishTick(); }
                });
            }
            try { Thread.sleep(16); }
            catch (InterruptedException exception) { stop(); Thread.currentThread().interrupt(); }
        }
    }
}
