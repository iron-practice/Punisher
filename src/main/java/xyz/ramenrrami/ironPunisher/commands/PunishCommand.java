package xyz.ramenrrami.ironPunisher.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PunishCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("punisher.punish")) { sender.sendMessage(Component.text("You have no permission for this command.", NamedTextColor.RED)); }

        if (args.length == 2) {
            if (Bukkit.getOfflinePlayer(args[1]) != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 3) return List.of();

        String input = args[2].toLowerCase();
        return List.of("").stream()
                .filter(s -> s.startsWith(input))
                .toList();
    }

}
