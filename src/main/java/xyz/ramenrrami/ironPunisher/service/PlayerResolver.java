package xyz.ramenrrami.ironPunisher.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.util.concurrent.CompletableFuture;

public final class PlayerResolver {
    private PlayerResolver() { }

    public static CompletableFuture<PlayerProfile> resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return CompletableFuture.completedFuture(online.getPlayerProfile());

        return Bukkit.createPlayerProfile(name).update()
                .thenApply(PlayerProfile.class::cast);
    }
}
