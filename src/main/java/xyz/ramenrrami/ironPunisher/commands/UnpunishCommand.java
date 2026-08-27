package xyz.ramenrrami.ironPunisher.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import xyz.ramenrrami.ironPunisher.IronPunisher;
import xyz.ramenrrami.ironPunisher.service.PlayerResolver;

public final class UnpunishCommand implements CommandExecutor {
    private final IronPunisher plugin;

    public UnpunishCommand(IronPunisher plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("punisher.unpunish")) {
            sender.sendMessage(Component.text("You have no permission for this command.", NamedTextColor.RED));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Component.text("Usage: /unpunish <player>", NamedTextColor.RED));
            return true;
        }

        PlayerResolver.resolve(args[0]).whenComplete((profile, error) -> {
            if (error != null || profile == null || profile.getUniqueId() == null) {
                sender.sendMessage(Component.text("Could not resolve that player.", NamedTextColor.RED));
                return;
            }
            plugin.getPunishmentService().unban(profile.getUniqueId()).whenComplete((changed, databaseError) -> {
                if (databaseError != null) {
                    plugin.getLogger().severe("Could not remove punishment: " + databaseError.getMessage());
                    Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(Component.text("Database error while removing the punishment.", NamedTextColor.RED)));
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(Component.text(changed ? "Punishment removed." : "No active punishment found.",
                        changed ? NamedTextColor.GREEN : NamedTextColor.YELLOW)));
            });
        });
        return true;
    }
}
