package se233.advpro2.model;

/** A "+points" number that floats up from a destroyed brick and fades out. */
public class ScorePopup {
    public static final double LIFETIME = 0.8, RISE_SPEED = 40;
    private final double x, y;
    private final int points;
    private double time = LIFETIME;
    public ScorePopup(double x, double y, int points) {
        if (points < 1) throw new GameException("Popup points must be positive.");
        this.x = x; this.y = y; this.points = points;
    }
    void tick(double seconds) { time = Math.max(0, time - seconds); }
    public boolean isActive() { return time > 0; }
    public int getPoints() { return points; }
    public double getX() { return x; }
    public double getY() { return y - (LIFETIME - time) * RISE_SPEED; }
    public double getOpacity() { return time / LIFETIME; }
}
