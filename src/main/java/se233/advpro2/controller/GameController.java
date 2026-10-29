package se233.advpro2.controller;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se233.advpro2.model.*;
import se233.advpro2.view.GameView;

public class GameController {
    private static final Logger LOG = LogManager.getLogger(GameController.class);
    private final GameSession game;
    private final GameView view;
    private boolean left, right, a, d, spaceHeld, pauseHeld, laserHeld;
    public GameController(GameSession game, GameView view) {
        this.game = game; this.view = view;
        view.bind(() -> execute(() -> { clearKeys(); game.start(); }),
                () -> execute(() -> { clearKeys(); game.menu(); }),
                () -> execute(() -> { clearKeys(); game.togglePause(); }),
                upgrade -> execute(() -> { clearKeys(); game.chooseUpgrade(upgrade); }));
        view.render(game);
    }
    public void attach(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.LEFT) left = true;
            if (key == KeyCode.RIGHT) right = true;
            if (key == KeyCode.A) a = true;
            if (key == KeyCode.D) d = true;
            if (key == KeyCode.SPACE && !spaceHeld) { spaceHeld = true; execute(() -> game.launch()); }
            if ((key == KeyCode.P || key == KeyCode.ESCAPE) && !pauseHeld) { pauseHeld = true; execute(() -> game.togglePause()); }
            if (key == KeyCode.X && !laserHeld) { laserHeld = true; execute(() -> game.activateLaser()); }
            event.consume();
        });
        scene.setOnKeyReleased(event -> {
            KeyCode key = event.getCode();
            if (key == KeyCode.LEFT) left = false;
            if (key == KeyCode.RIGHT) right = false;
            if (key == KeyCode.A) a = false;
            if (key == KeyCode.D) d = false;
            if (key == KeyCode.SPACE) spaceHeld = false;
            if (key == KeyCode.P || key == KeyCode.ESCAPE) pauseHeld = false;
            if (key == KeyCode.X) laserHeld = false;
        });
    }
    public void tick() {
        execute(() -> game.tick(1.0 / 60, ((right || d) ? 1 : 0) - ((left || a) ? 1 : 0)));
    }
    public void focusLost() { clearKeys(); execute(() -> game.pause()); }
    private void clearKeys() { left = right = a = d = spaceHeld = pauseHeld = laserHeld = false; }
    private void execute(Runnable action) {
        try { action.run(); view.render(game); }
        catch (GameException exception) {
            game.pause(); LOG.error("Game action failed", exception);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("This action could not be completed"); alert.setContentText(exception.getMessage()); alert.showAndWait();
        }
    }
}
