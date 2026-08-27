package xyz.ramenrrami.ironPunisher.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

public final class DurationParser {
    private DurationParser() { }

    public record Duration(Instant expiresAt, String display) {
        public boolean isPermanent() { return expiresAt == null; }
    }

    public static Duration parse(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("Duration cannot be empty.");

        String value = input.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "1d", "1day", "1days", "1tag", "1tage" -> fromDays(1, "1 day");
            case "3d", "3day", "3days", "3tag", "3tage" -> fromDays(3, "3 days");
            case "7d", "7day", "7days", "7tag", "7tage" -> fromDays(7, "7 days");
            case "2w", "2week", "2weeks", "2wochen" -> fromDays(14, "2 weeks");
            case "1mo", "1month", "1monat" -> fromMonths(1, "1 month");
            case "6mo", "6month", "6months", "6monate" -> fromMonths(6, "6 months");
            case "perma", "perm", "permanent", "permanentban" -> new Duration(null, "Permanent");
            default -> throw new IllegalArgumentException("Invalid duration. Use: 1d, 3d, 7d, 2w, 1mo, 6mo or perma.");
        };
    }

    private static Duration fromDays(long days, String display) {
        return new Duration(Instant.now().plusSeconds(days * 86_400L), display);
    }

    private static Duration fromMonths(long months, String display) {
        ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
        return new Duration(now.plusMonths(months).toInstant(), display);
    }

    public static List<String> suggestions() {
        return List.of("1d", "3d", "7d", "2w", "1mo", "6mo", "perma");
    }
}
