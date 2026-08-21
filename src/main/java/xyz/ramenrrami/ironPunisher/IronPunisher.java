package xyz.ramenrrami.ironPunisher;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.ramenrrami.ironPunisher.commands.PunishCommand;

public final class IronPunisher extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getCommand("punish").setExecutor(new PunishCommand(this));
        getCommand("punish").setTabCompleter(new PunishCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
