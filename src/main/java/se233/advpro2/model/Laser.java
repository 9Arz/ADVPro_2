package se233.advpro2.model;

public class Laser {
    public static final double WIDTH = 4, HEIGHT = 18;
    private final double x;
    private double y;
    private boolean active = true;
    public Laser(double x, double y) { this.x = x; this.y = y; }
    void move(double seconds) { y -= 620 * seconds; if (y + HEIGHT < 0) active = false; }
    void deactivate() { active = false; }
    public boolean isActive() { return active; }
    public double getX() { return x; }
    public double getY() { return y; }
}
