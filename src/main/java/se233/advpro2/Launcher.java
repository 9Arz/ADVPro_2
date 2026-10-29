package se233.advpro2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.advpro2.controller.*;
import se233.advpro2.model.GameSession;
import se233.advpro2.view.GameView;

public class Launcher extends Application {
    private GameLoop loop;
    private Thread worker;
    public static void main(String[] args) { launch(args); }
    @Override public void start(Stage stage) {
        GameView view = new GameView();
        GameController controller = new GameController(new GameSession(), view);
        Scene scene = new Scene(view, 1068, 700);
        scene.getStylesheets().add(Launcher.class.getResource("/se233/advpro2/style.css").toExternalForm());
        controller.attach(scene);
        stage.setTitle("Orbit Breaker | ADVPro_2"); stage.setScene(scene); stage.setResizable(false);
        stage.focusedProperty().addListener((observable, oldValue, focused) -> { if (!focused) controller.focusLost(); });
        stage.show();
        loop = new GameLoop(() -> controller.tick());
        worker = new Thread(loop, "game-clock"); worker.setDaemon(true); worker.start();
    }
    @Override public void stop() {
        if (loop != null) loop.stop();
        if (worker != null) worker.interrupt();
    }
}
