package xyz.ramenrrami.ironPunisher.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.ramenrrami.ironPunisher.enums.Reasons;

import java.util.Calendar;
import java.util.List;

public class PunishCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("punisher.punish")) { sender.sendMessage(Component.text("You have no permission for this command.", NamedTextColor.RED)); }

        if (args.length == 2) {
            if (Bukkit.getOfflinePlayer(args[1]) != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                Player online = Bukkit.getPlayer(args[0]);

                if (args[1].equalsIgnoreCase(Reasons.SERVER_ADVERTISEMENT.getDisplayReason())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, Reasons.SERVER_ADVERTISEMENT.getDays());
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "", calendar.getTime(), null);
                } else if (args[1].equalsIgnoreCase(Reasons.DOXXING.getDisplayReason())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, Reasons.DOXXING.getDays());
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "", calendar.getTime(), null);
                } else if (args[1].equalsIgnoreCase(Reasons.INAPPROPRIATE_BEHAVIOR.getDisplayReason())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, Reasons.INAPPROPRIATE_BEHAVIOR.getDays());
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "", calendar.getTime(), null);
                } else if (args[1].equalsIgnoreCase(Reasons.SCAMMING.getDisplayReason())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, Reasons.SCAMMING.getDays());
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "", calendar.getTime(), null);
                } else if (args[1].equalsIgnoreCase(Reasons.UNFAIR_ADVANTAGE.getDisplayReason())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, Reasons.UNFAIR_ADVANTAGE.getDays());
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "", calendar.getTime(), null);
                } else {
                    sender.sendMessage(Component.text("Invalid Command.", NamedTextColor.RED));
                }
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
