package xyz.ramenrrami.ironPunisher.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import xyz.ramenrrami.ironPunisher.IronPunisher;
import xyz.ramenrrami.ironPunisher.enums.Reasons;

import java.util.Calendar;
import java.util.List;

public class PunishCommand implements CommandExecutor, TabCompleter {
    private IronPunisher ironPunisher;
    public PunishCommand(IronPunisher ironPunisher) { this.ironPunisher = ironPunisher; }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("punisher.punish")) { sender.sendMessage(Component.text("You have no permission for this command.", NamedTextColor.RED)); }

        if (args.length == 2) {
            if (Bukkit.getOfflinePlayer(args[1]) != null) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                Player online = Bukkit.getPlayer(args[0]);

                try {
                    Reasons reasons = Reasons.valueOf(args[1].toUpperCase());

                    int punishDays = reasons.getDays();
                    String punishName = reasons.getDisplayReason();

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, 24 * punishDays);

                    player.ban(LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(
                            "<red>You are banned for: <yellow>" + punishDays + "\n\n<reset><gray>Reason: <white>" + punishName + "\n\n<reset><red>You may appeal for this ban at: <yellow>"
                            + ironPunisher.getConfig().getString("discord")
                    )), calendar.getTime(), null);

                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("Invalid command.", NamedTextColor.RED));
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
