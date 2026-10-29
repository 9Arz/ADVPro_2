package se233.advpro2.model;

public class Ball {
    public static final double RADIUS = 8;
    private double x, y, vx, vy;
    private double bounceTime;
    public void move(double seconds) { x += vx * seconds; y += vy * seconds; bounceTime = Math.max(0, bounceTime - seconds); }
    void place(double x, double y) { this.x = x; this.y = y; }
    void velocity(double vx, double vy) { this.vx = vx; this.vy = vy; }
    void bounceX() { vx = -vx; bounceTime = 0.14; }
    void bounceY() { vy = -vy; bounceTime = 0.14; }
    void bounceFromPaddle(double offset, double speed) {
        vx = speed * Math.max(-0.85, Math.min(0.85, offset * 0.85));
        vy = -Math.sqrt(speed * speed - vx * vx);
        bounceTime = 0.14;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public boolean isBouncing() { return bounceTime > 0; }
}
