package se233.advpro2.model;

public class Paddle {
    public static final double Y = 490, HEIGHT = 14;
    private double x = 344, width = 112, speed = 460;
    public void move(int direction, double seconds) {
        x = Math.max(0, Math.min(GameSession.WIDTH - width, x + direction * speed * seconds));
    }
    void widen() { width += 28; x = Math.min(x, GameSession.WIDTH - width); }
    void accelerate() { speed += 80; }
    void center() { x = (GameSession.WIDTH - width) / 2; }
    public double getX() { return x; }
    public double getWidth() { return width; }
    public double getSpeed() { return speed; }
}
