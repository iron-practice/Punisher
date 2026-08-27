package xyz.ramenrrami.ironPunisher.enums;

public enum Reasons {

    SERVER_ADVERTISEMENT("server advertisement"),
    UNFAIR_ADVANTAGE("unfair advantage"),
    DOXXING("doxxing"),
    INAPPROPRIATE_BEHAVIOR("inappropriate behavior"),
    SCAMMING("scamming");

    private final String displayReason;

    Reasons(String displayReason) { this.displayReason = displayReason; }

    public String getDisplayReason() {
        return displayReason;
    }
}
