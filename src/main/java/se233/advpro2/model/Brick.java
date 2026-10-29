package se233.advpro2.model;

public class Brick {
    public static final double WIDTH = 80, HEIGHT = 24;
    private final double x, y;
    private final int maxHealth, points;
    private int health;
    private double flash;
    public Brick(double x, double y, int health, int points) {
        if (health < 1 || points < 1) throw new GameException("Brick health and points must be positive.");
        this.x = x; this.y = y; this.health = health; this.maxHealth = health; this.points = points;
    }
    public boolean hit() {
        if (health == 0) return false;
        health--; flash = 0.18;
        return health == 0;
    }
    void tick(double seconds) { flash = Math.max(0, flash - seconds); }
    public boolean overlaps(double left, double top, double width, double height) {
        return health > 0 && left + width > x && left < x + WIDTH && top + height > y && top < y + HEIGHT;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getPoints() { return points; }
    public double getFlash() { return flash; }
}
