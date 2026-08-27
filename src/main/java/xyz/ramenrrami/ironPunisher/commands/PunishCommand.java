package xyz.ramenrrami.ironPunisher.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.ramenrrami.ironPunisher.IronPunisher;
import xyz.ramenrrami.ironPunisher.enums.Reasons;
import xyz.ramenrrami.ironPunisher.model.Punishment;
import xyz.ramenrrami.ironPunisher.model.PunishmentType;
import xyz.ramenrrami.ironPunisher.service.DurationParser;
import xyz.ramenrrami.ironPunisher.service.PlayerResolver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PunishCommand implements CommandExecutor, TabCompleter {
    private final IronPunisher plugin;

    public PunishCommand(IronPunisher plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("punisher.punish")) {
            sender.sendMessage(Component.text("You have no permission for this command.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /ban <player> <reason> <duration>", NamedTextColor.RED));
            return true;
        }

        String targetName = args[0];
        String durationInput = args[args.length - 1];
        String reasonInput = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)).trim();
        if (reasonInput.isBlank()) {
            sender.sendMessage(Component.text("A reason is required.", NamedTextColor.RED));
            return true;
        }

        DurationParser.Duration duration;
        try {
            duration = DurationParser.parse(durationInput);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text(ex.getMessage(), NamedTextColor.RED));
            return true;
        }

        String reason = resolveReason(reasonInput);
        if (reason.length() > 255) {
            sender.sendMessage(Component.text("The reason is too long (maximum 255 characters).", NamedTextColor.RED));
            return true;
        }
        String createdBy = sender.getName();
        sender.sendMessage(Component.text("Resolving " + targetName + "...", NamedTextColor.GRAY));

        PlayerResolver.resolve(targetName).whenComplete((profile, error) -> {
            if (error != null || profile == null || profile.getUniqueId() == null) {
                send(sender, Component.text("Could not resolve the UUID for " + targetName + ".", NamedTextColor.RED));
                return;
            }

            String resolvedPlayerName = profile.getName() == null ? targetName : profile.getName();
            final String playerName = resolvedPlayerName.length() > 16
                    ? resolvedPlayerName.substring(0, 16)
                    : resolvedPlayerName;
            final Instant expires = duration.expiresAt();
            plugin.getPunishmentService().ban(profile.getUniqueId(), playerName, reason, expires, createdBy)
                    .whenComplete((id, databaseError) -> {
                        if (databaseError != null) {
                            plugin.getLogger().severe("Could not save punishment: " + databaseError.getMessage());
                            send(sender, Component.text("Database error while creating the punishment.", NamedTextColor.RED));
                            return;
                        }

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Player online = Bukkit.getPlayer(profile.getUniqueId());
                            if (online != null) {
                                plugin.kickPlayer(online, new Punishment(id, profile.getUniqueId(), online.getName(), reason,
                                        PunishmentType.BAN, Instant.now(), expires, createdBy, plugin.getServerId(), true));
                            }

                            String message = plugin.getConfig().getString("messages.punish-success",
                                            "<green><player> has been banned for <duration>.</green>")
                                    .replace("<player>", playerName)
                                    .replace("<reason>", reason)
                                    .replace("<duration>", duration.display());
                            sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
                        });
                    });
        });

        return true;
    }

    private String resolveReason(String input) {
        for (Reasons reason : Reasons.values()) {
            if (reason.name().equalsIgnoreCase(input) || reason.getDisplayReason().equalsIgnoreCase(input)) {
                return reason.getDisplayReason();
            }
        }
        return input;
    }

    private void send(CommandSender sender, Component message) {
        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) suggestions.add(player.getName());
        } else if (args.length >= 2 && args[args.length - 1].isEmpty()) {
            if (args.length == 2) for (Reasons reason : Reasons.values()) suggestions.add(reason.name().toLowerCase());
        }

        if (args.length >= 3) suggestions.addAll(DurationParser.suggestions());
        return suggestions;
    }
}
