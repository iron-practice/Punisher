package xyz.ramenrrami.ironPunisher.database;

import xyz.ramenrrami.ironPunisher.model.Punishment;
import xyz.ramenrrami.ironPunisher.model.PunishmentType;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PunishmentRepository {
    private final DatabaseManager database;

    public PunishmentRepository(DatabaseManager database) { this.database = database; }

    public Optional<Punishment> findActiveBan(UUID uuid) throws SQLException {
        String sql = """
                SELECT id, uuid, player_name, reason, punishment_type, created_at,
                       expires_at, created_by, server_id, active
                FROM punishments
                WHERE uuid = ? AND active = TRUE AND punishment_type = 'BAN'
                  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP(3))
                ORDER BY id DESC LIMIT 1
                """;
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public long insertBan(UUID uuid, String playerName, String reason, Instant expiresAt,
                          String createdBy, String serverId) throws SQLException {
        String deactivateSql = "UPDATE punishments SET active = FALSE WHERE uuid = ? AND active = TRUE AND punishment_type = 'BAN'";
        String insertSql = """
                INSERT INTO punishments
                    (uuid, player_name, reason, punishment_type, created_at, expires_at, created_by, server_id, active)
                VALUES (?, ?, ?, 'BAN', CURRENT_TIMESTAMP(3), ?, ?, ?, TRUE)
                """;

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deactivate = connection.prepareStatement(deactivateSql)) {
                    deactivate.setString(1, uuid.toString());
                    deactivate.executeUpdate();
                }

                try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    insert.setString(1, uuid.toString());
                    insert.setString(2, playerName);
                    insert.setString(3, reason);
                    if (expiresAt == null) insert.setNull(4, Types.TIMESTAMP);
                    else insert.setTimestamp(4, Timestamp.from(expiresAt));
                    insert.setString(5, createdBy);
                    insert.setString(6, serverId);
                    insert.executeUpdate();
                    connection.commit();

                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Database did not return the punishment id");
                        return keys.getLong(1);
                    }
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public boolean deactivate(UUID uuid) throws SQLException {
        String sql = "UPDATE punishments SET active = FALSE WHERE uuid = ? AND active = TRUE AND punishment_type = 'BAN'";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            return statement.executeUpdate() > 0;
        }
    }

    public List<Punishment> findRecentActiveBans(Instant since, String serverId) throws SQLException {
        String sql = """
                SELECT id, uuid, player_name, reason, punishment_type, created_at,
                       expires_at, created_by, server_id, active
                FROM punishments
                WHERE active = TRUE AND punishment_type = 'BAN' AND created_at >= ?
                  AND server_id <> ?
                  AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP(3))
                ORDER BY id ASC
                """;
        List<Punishment> punishments = new ArrayList<>();
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(since));
            statement.setString(2, serverId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) punishments.add(map(result));
            }
        }
        return punishments;
    }

    public int expireOldPunishments() throws SQLException {
        String sql = "UPDATE punishments SET active = FALSE WHERE active = TRUE AND expires_at IS NOT NULL AND expires_at <= CURRENT_TIMESTAMP(3)";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    private Punishment map(ResultSet result) throws SQLException {
        Timestamp createdAt = result.getTimestamp("created_at");
        Timestamp expiresAt = result.getTimestamp("expires_at");
        return new Punishment(result.getLong("id"), UUID.fromString(result.getString("uuid")),
                result.getString("player_name"), result.getString("reason"),
                PunishmentType.valueOf(result.getString("punishment_type")),
                createdAt == null ? Instant.now() : createdAt.toInstant(),
                expiresAt == null ? null : expiresAt.toInstant(), result.getString("created_by"),
                result.getString("server_id"), result.getBoolean("active"));
    }
}
