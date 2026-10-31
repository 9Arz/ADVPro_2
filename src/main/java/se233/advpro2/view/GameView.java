package se233.advpro2.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import se233.advpro2.model.*;
import java.util.function.Consumer;

public class GameView extends BorderPane {
    private final Pane arena = new Pane();
    private final VBox overlay = new VBox(18);
    private final Label score = new Label(), lives = new Label(), stage = new Label();
    private final Label status = new Label(), charge = new Label(), build = new Label();
    private final Rectangle paddle = new Rectangle();
    private final AnimatedSprite ball;
    private Rectangle[] bricks = new Rectangle[0];
    private Label[] brickLabels = new Label[0];
    private final Rectangle[] lasers = new Rectangle[40];
    private final Label[] popups = new Label[20];
    private final Button pause = new Button("Pause  [P]");
    private GameState lastState;
    private int lastStage = -1, tick;
    private Runnable startAction, menuAction, pauseAction;
    private Consumer<Upgrade> upgradeAction;

    public GameView() {
        getStyleClass().add("root-pane");
        Image sheet = new Image(GameView.class.getResourceAsStream("/se233/advpro2/ball-sprites-new.png"));
        if (sheet.isError()) throw new GameException("The ball sprite sheet could not be loaded.");
        ball = new AnimatedSprite(sheet);
        Label brand = new Label("ORBIT / BREAKER"); brand.getStyleClass().add("brand");
        Label caption = new Label("A ROGUELIKE ARCADE RUN"); caption.getStyleClass().add("eyebrow");
        VBox heading = new VBox(5, caption, brand);
        HBox hud = new HBox(32, heading, metric("SCORE", score), metric("STAGE", stage), metric("LIVES", lives));
        hud.setAlignment(Pos.CENTER_LEFT); hud.setPadding(new Insets(24, 28, 22, 28)); setTop(hud);
        arena.setMinSize(800, 540); arena.setPrefSize(800, 540); arena.setMaxSize(800, 540);
        arena.getStyleClass().add("arena");
        overlay.setAlignment(Pos.CENTER); overlay.setMaxSize(660, 360); overlay.getStyleClass().add("overlay");
        StackPane field = new StackPane(arena, overlay); field.setPadding(new Insets(0, 0, 0, 28));
        setCenter(field);
        Label sideTitle = new Label("YOUR RUN"); sideTitle.getStyleClass().add("eyebrow");
        Label tip = new Label("Clear every brick. Choose an upgrade. Survive three stages."); tip.setWrapText(true);
        Label controls = new Label("A / D or arrow keys   Move\nSPACE   Launch\nP / ESC   Pause\nX   Activate dual laser");
        controls.getStyleClass().add("small");
        Label laserTitle = new Label("DUAL LASER"); laserTitle.getStyleClass().add("eyebrow");
        charge.setWrapText(true); build.setWrapText(true); build.getStyleClass().add("small");
        Label legend = new Label("Cyan   Normal / 100 pts\nPurple   Armour / 2 hits\nGold   Bonus / 200 pts\n\nEvery 5 bricks without a miss:\n+150 combo points.\nBall kills: +10 charge."); legend.getStyleClass().add("small");
        Button menu = new Button("Return to menu"); menu.setOnAction(e -> menuAction.run());
        pause.setOnAction(e -> pauseAction.run()); pause.setMaxWidth(Double.MAX_VALUE); menu.setMaxWidth(Double.MAX_VALUE);
        pause.setFocusTraversable(false); menu.setFocusTraversable(false);
        VBox sidebar = new VBox(18, sideTitle, tip, controls, laserTitle, charge, build, legend, pause, menu);
        sidebar.setPrefWidth(240); sidebar.setPadding(new Insets(8, 28, 0, 24)); setRight(sidebar);
        status.getStyleClass().add("status"); status.setPadding(new Insets(18, 28, 20, 28)); setBottom(status);
    }
    private VBox metric(String title, Label value) {
        Label label = new Label(title); label.getStyleClass().add("eyebrow"); value.getStyleClass().add("metric");
        return new VBox(5, label, value);
    }
    public void bind(Runnable start, Runnable menu, Runnable pause, Consumer<Upgrade> upgrade) {
        startAction = start; menuAction = menu; pauseAction = pause; upgradeAction = upgrade;
    }
    private Button action(String title, Runnable action) {
        Button button = new Button(title); button.getStyleClass().add("primary");
        button.setFocusTraversable(false); button.setOnAction(e -> action.run()); return button;
    }
    private void rebuildArena(GameSession game) {
        arena.getChildren().clear();
        bricks = new Rectangle[game.getBrickCount()]; brickLabels = new Label[bricks.length];
        for (int i = 0; i < bricks.length; i++) {
            Brick brick = game.getBrick(i);
            Rectangle shape = new Rectangle(brick.getX(), brick.getY(), Brick.WIDTH, Brick.HEIGHT);
            shape.setArcWidth(7); shape.setArcHeight(7); bricks[i] = shape;
            Label label = new Label(); label.setLayoutX(brick.getX() + 34); label.setLayoutY(brick.getY() + 2);
            label.setStyle("-fx-text-fill: #07111f; -fx-font-weight: bold;"); brickLabels[i] = label;
            arena.getChildren().addAll(shape, label);
        }
        for (int i = 0; i < lasers.length; i++) {
            lasers[i] = new Rectangle(Laser.WIDTH, Laser.HEIGHT, Color.web("#f5cd71"));
            lasers[i].setVisible(false); arena.getChildren().add(lasers[i]);
        }
        for (int i = 0; i < popups.length; i++) {
            popups[i] = new Label();
            popups[i].setStyle("-fx-text-fill: #f5cd71; -fx-font-weight: bold; -fx-font-size: 16;");
            popups[i].setVisible(false); arena.getChildren().add(popups[i]);
        }
        paddle.setHeight(Paddle.HEIGHT); paddle.setArcWidth(12); paddle.setArcHeight(12); paddle.setFill(Color.web("#8cf4df"));
        arena.getChildren().addAll(paddle, ball);
    }
    public void render(GameSession game) {
        tick++;
        if (lastStage != game.getStage() || bricks.length != game.getBrickCount()) { rebuildArena(game); lastStage = game.getStage(); }
        score.setText(String.format("%06d", game.getScore()));
        lives.setText(Integer.toString(game.getLives())); stage.setText(game.getStage() + " / 3");
        status.setText(game.getMessage());
        if (game.getLaserTime() > 0) {
            charge.setText(String.format("FIRING  %.1f s", game.getLaserTime()));
        } else if (game.getLaserCooldown() > 0) {
            charge.setText(String.format("COOLDOWN  %.1f s", game.getLaserCooldown()));
        } else {
            charge.setText(game.getCharge() + "% charged" + (game.getCharge() == 100 ? " - press X" : ""));
        }
        build.setText("Paddle " + (int)game.getPaddle().getWidth() + " px / " + (int)game.getPaddle().getSpeed() + " px/s\nCombo " + game.getCombo() + "   |   Bricks " + game.remainingBricks());
        paddle.setX(game.getPaddle().getX()); paddle.setY(Paddle.Y); paddle.setWidth(game.getPaddle().getWidth());
        ball.draw(game.getBall().getX(), game.getBall().getY(), game.getBall().isBouncing(), tick);
        for (int i = 0; i < bricks.length; i++) {
            Brick brick = game.getBrick(i);
            boolean visible = brick.getHealth() > 0 || brick.getFlash() > 0;
            bricks[i].setVisible(visible); brickLabels[i].setVisible(brick.getHealth() > 0);
            bricks[i].setOpacity(brick.getHealth() == 0 ? brick.getFlash() / 0.18 : 1);
            bricks[i].setFill(brick.getFlash() > 0 ? Color.WHITE : Color.web(brick.getMaxHealth() > 1 ? "#b6a0fb" : brick.getPoints() == 200 ? "#f5cd71" : "#59c8df"));
            brickLabels[i].setText(brick.getMaxHealth() > 1 ? Integer.toString(brick.getHealth()) : "");
        }
        for (int i = 0; i < lasers.length; i++) {
            Laser laser = game.getLaser(i);
            lasers[i].setVisible(laser != null && laser.isActive());
            if (laser != null) { lasers[i].setX(laser.getX()); lasers[i].setY(laser.getY()); }
        }
        for (int i = 0; i < popups.length; i++) {
            ScorePopup popup = game.getPopup(i);
            boolean active = popup != null && popup.isActive();
            popups[i].setVisible(active);
            if (active) {
                popups[i].setText("+" + popup.getPoints());
                popups[i].setLayoutX(popup.getX() - 18); popups[i].setLayoutY(popup.getY());
                popups[i].setOpacity(popup.getOpacity());
            }
        }
        pause.setDisable(game.getState() != GameState.READY && game.getState() != GameState.PLAYING && game.getState() != GameState.PAUSED);
        pause.setText(game.getState() == GameState.PAUSED ? "Resume  [P]" : "Pause  [P]");
        if (lastState != game.getState()) { renderOverlay(game); lastState = game.getState(); }
    }
    private void renderOverlay(GameSession game) {
        overlay.getChildren().clear();
        GameState state = game.getState();
        boolean visible = state == GameState.MENU || state == GameState.PAUSED || state == GameState.UPGRADE || state == GameState.WON || state == GameState.LOST;
        overlay.setVisible(visible); overlay.setManaged(visible);
        if (!visible) return;
        String title = "BREAK. CHOOSE. REPEAT.";
        String detail = "Three stages. One evolving build. Make every life count.";
        if (state == GameState.PAUSED) { title = "TAKE A BREATHER"; detail = "Your run is paused. Resume when ready."; }
        if (state == GameState.UPGRADE) { title = "CHOOSE YOUR UPGRADE"; detail = "Stage " + game.getStage() + " cleared. Pick one permanent boost."; }
        if (state == GameState.WON) { title = "ORBIT RESTORED"; detail = "All stages cleared. Final score: " + game.getScore(); }
        if (state == GameState.LOST) { title = "RUN COMPLETE"; detail = "Final score: " + game.getScore() + ". A new build awaits."; }
        Label heading = new Label(title); heading.getStyleClass().add("overlay-title");
        Label subtitle = new Label(detail); subtitle.setWrapText(true);
        overlay.getChildren().addAll(heading, subtitle);
        if (state == GameState.UPGRADE) {
            for (Upgrade upgrade : game.getOffers()) {
                Button choice = action(upgrade.getTitle() + "  /  " + upgrade.getDescription(), () -> upgradeAction.accept(upgrade));
                choice.setPrefWidth(540); overlay.getChildren().add(choice);
            }
        } else if (state == GameState.PAUSED) overlay.getChildren().add(action("Resume run", pauseAction));
        else overlay.getChildren().add(action(state == GameState.MENU ? "Start a new run" : "Play again", startAction));
    }
}
