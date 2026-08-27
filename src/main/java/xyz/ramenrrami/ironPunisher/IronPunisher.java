package xyz.ramenrrami.ironPunisher;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.ramenrrami.ironPunisher.commands.PunishCommand;
import xyz.ramenrrami.ironPunisher.commands.UnpunishCommand;
import xyz.ramenrrami.ironPunisher.database.DatabaseManager;
import xyz.ramenrrami.ironPunisher.database.PunishmentRepository;
import xyz.ramenrrami.ironPunisher.listeners.LoginListener;
import xyz.ramenrrami.ironPunisher.model.Punishment;
import xyz.ramenrrami.ironPunisher.service.PunishmentService;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class IronPunisher extends JavaPlugin {
    private DatabaseManager databaseManager;
    private PunishmentRepository punishmentRepository;
    private PunishmentService punishmentService;
    private ExecutorService databaseExecutor;
    private String serverId;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        serverId = getConfig().getString("server-id", "server-01");
        databaseExecutor = Executors.newFixedThreadPool(Math.max(2, getConfig().getInt("database.async-threads", 4)), runnable -> {
            Thread thread = new Thread(runnable, "IronPunisher-Database");
            thread.setDaemon(true);
            return thread;
        });

        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            punishmentRepository = new PunishmentRepository(databaseManager);
            punishmentService = new PunishmentService(this, punishmentRepository);
        } catch (Exception e) {
            getLogger().severe("IronPunisher could not initialize: " + e.getMessage());
            if (databaseExecutor != null) databaseExecutor.shutdownNow();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerCommands();
        getServer().getPluginManager().registerEvents(new LoginListener(this), this);
        startSynchronizationTasks();
        getLogger().info("IronPunisher enabled. Shared punishment database is active on " + serverId + ".");
    }

    private void registerCommands() {
        PunishCommand punishCommand = new PunishCommand(this);
        PluginCommand ban = getCommand("ban");
        if (ban != null) {
            ban.setExecutor(punishCommand);
            ban.setTabCompleter(punishCommand);
        }
        PluginCommand unpunish = getCommand("unpunish");
        if (unpunish != null) unpunish.setExecutor(new UnpunishCommand(this));
    }

    private void startSynchronizationTasks() {
        long interval = Math.max(20L, getConfig().getLong("sync.interval-ticks", 40L));
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            punishmentService.expireOldPunishments().exceptionally(error -> {
                getLogger().warning("Could not expire punishments: " + error.getMessage());
                return 0;
            });
            punishmentService.enforceRecentBans(Instant.now().minusSeconds(Math.max(10L, getConfig().getLong("sync.lookback-seconds", 120L))));
        }, interval, interval);
    }

    public void kickPlayer(Player player, Punishment punishment) {
        String expires = punishment.isPermanent() ? "Permanent" : punishment.expiresAt().toString();
        String message = getConfig().getString("messages.kick-ban", "<red>You are banned.</red>\\n\\n<gray>Reason: <white><reason></white>\\n<gray>Expires: <white><expires></white>")
                .replace("<reason>", punishment.reason())
                .replace("<expires>", expires)
                .replace("<discord>", getConfig().getString("discord", ""));
        player.kick(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void onDisable() {
        if (databaseExecutor != null) databaseExecutor.shutdownNow();
        if (databaseManager != null) databaseManager.close();
    }

    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public PunishmentService getPunishmentService() { return punishmentService; }
    public ExecutorService getDatabaseExecutor() { return databaseExecutor; }
    public String getServerId() { return serverId; }
}
