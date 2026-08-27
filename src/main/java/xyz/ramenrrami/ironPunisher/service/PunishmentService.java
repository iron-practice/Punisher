package xyz.ramenrrami.ironPunisher.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.ramenrrami.ironPunisher.IronPunisher;
import xyz.ramenrrami.ironPunisher.database.PunishmentRepository;
import xyz.ramenrrami.ironPunisher.model.Punishment;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class PunishmentService {
    private final IronPunisher plugin;
    private final PunishmentRepository repository;

    public PunishmentService(IronPunisher plugin, PunishmentRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public CompletableFuture<Optional<Punishment>> findActiveBan(UUID uuid) {
        return supplyAsync(() -> repository.findActiveBan(uuid));
    }

    public CompletableFuture<Long> ban(UUID uuid, String playerName, String reason,
                                       Instant expiresAt, String createdBy) {
        return supplyAsync(() -> repository.insertBan(uuid, playerName, reason, expiresAt, createdBy,
                plugin.getServerId()));
    }

    public CompletableFuture<Boolean> unban(UUID uuid) {
        return supplyAsync(() -> repository.deactivate(uuid));
    }

    public CompletableFuture<Integer> expireOldPunishments() {
        return supplyAsync(repository::expireOldPunishments);
    }

    public CompletableFuture<Void> enforceRecentBans(Instant since) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (Punishment punishment : repository.findRecentActiveBans(since, plugin.getServerId())) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Player player = Bukkit.getPlayer(punishment.uuid());
                        if (player != null && player.isOnline()) {
                            plugin.kickPlayer(player, punishment);
                        }
                    });
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Could not synchronize recent punishments: " + e.getMessage());
            }
        }, plugin.getDatabaseExecutor());
    }

    private <T> CompletableFuture<T> supplyAsync(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                throw new DatabaseOperationException(e);
            }
        }, plugin.getDatabaseExecutor());
    }

    @FunctionalInterface
    private interface SqlSupplier<T> { T get() throws SQLException; }

    public static final class DatabaseOperationException extends RuntimeException {
        public DatabaseOperationException(Throwable cause) { super(cause); }
    }
}
