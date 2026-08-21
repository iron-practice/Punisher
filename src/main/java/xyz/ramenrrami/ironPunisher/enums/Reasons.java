package xyz.ramenrrami.ironPunisher.enums;

public enum Reasons {

    SERVER_ADVERTISEMENT("server advertisement", 7),
    UNFAIR_ADVANTAGE("unfair advantage", 14),
    DOXXING("doxxing", 30),
    INAPPROPRIATE_BEHAVIOR("inappropriate behavior", 14),
    SCAMMING("scamming", 60);


    private final String displayReason;
    private final int days;

    Reasons(String displayReason, int days) { this.displayReason = displayReason; this.days = days; }

    public String getDisplayReason(String displayReason) {
        return displayReason;
    }

    public int getDays(int days) {
        return days;
    }
}
