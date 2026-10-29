package se233.advpro2.view;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** Four movement frames in row 0; four impact frames in row 1. */
public class AnimatedSprite extends ImageView {
    private int frame;
    public AnimatedSprite(Image image) {
        super(image); setFitWidth(20); setFitHeight(20); setSmooth(false);
    }
    public void draw(double x, double y, boolean bouncing, int tick) {
        frame = (tick / 5) % 4;
        setViewport(new Rectangle2D(frame * 24, bouncing ? 24 : 0, 24, 24));
        setLayoutX(x - 10); setLayoutY(y - 10);
    }
}
