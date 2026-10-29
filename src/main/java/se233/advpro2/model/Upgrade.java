package se233.advpro2.model;

public enum Upgrade {
    WIDE_PADDLE("Wide paddle", "+28 px paddle width"),
    QUICK_PADDLE("Quick reflexes", "+80 px/s paddle speed"),
    SLOW_BALL("Time dilation", "Ball speed reduced by 15%"),
    EXTRA_LIFE("Second chance", "+1 life for this run"),
    SCORE_BOOST("Treasure hunter", "+50 points per destroyed brick");

    private final String title;
    private final String description;
    Upgrade(String title, String description) { this.title = title; this.description = description; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
