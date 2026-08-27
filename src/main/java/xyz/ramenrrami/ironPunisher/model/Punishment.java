package xyz.ramenrrami.ironPunisher.model;

import java.time.Instant;
import java.util.UUID;

public record Punishment(long id, UUID uuid, String playerName, String reason,
                         PunishmentType type, Instant createdAt, Instant expiresAt,
                         String createdBy, String serverId, boolean active) {
    public boolean isPermanent() { return expiresAt == null; }
    public boolean isExpired(Instant now) { return expiresAt != null && !expiresAt.isAfter(now); }
}
