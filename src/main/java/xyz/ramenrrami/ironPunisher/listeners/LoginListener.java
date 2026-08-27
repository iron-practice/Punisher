package xyz.ramenrrami.ironPunisher.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import xyz.ramenrrami.ironPunisher.IronPunisher;
import xyz.ramenrrami.ironPunisher.model.Punishment;
import xyz.ramenrrami.ironPunisher.service.PunishmentService;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class LoginListener implements Listener {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'").withZone(ZoneOffset.UTC);
    private final IronPunisher plugin;

    public LoginListener(IronPunisher plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            Optional<Punishment> result = plugin.getPunishmentService().findActiveBan(event.getUniqueId()).join();
            if (result.isEmpty()) return;

            Punishment punishment = result.get();
            String expires = punishment.isPermanent() ? "Permanent" : DATE_FORMAT.format(punishment.expiresAt());
            String message = plugin.getConfig().getString("messages.login-ban",
                    "<red>You are banned from this network.</red>\\n\\n<gray>Reason: <white><reason></white>\\n<gray>Expires: <white><expires></white>")
                    .replace("<reason>", punishment.reason())
                    .replace("<expires>", expires);

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    MiniMessage.miniMessage().deserialize(message));
        } catch (PunishmentService.DatabaseOperationException e) {
            plugin.getLogger().severe("Database error while checking " + event.getUniqueId() + ": " + e.getCause().getMessage());
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text("We could not verify your punishment status. Please try again later."));
        }
    }
}
