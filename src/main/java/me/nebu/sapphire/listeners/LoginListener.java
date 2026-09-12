package me.nebu.sapphire.listeners;

import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.actions.ActionDetails;
import me.nebu.sapphire.actions.ActionManager;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.punishments.PunishmentType;
import me.nebu.sapphire.util.Cache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;
import java.util.Optional;

public class LoginListener implements Listener {

    // Check if player is banned
    @EventHandler
    public void checkBanned(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        List<Punishment> history = PunishmentManager.getHistory(player);

        if (history == null) return;

        Optional<Punishment> latestBan = history.stream()
                .filter(p -> p.getType().equals(PunishmentType.BAN) && !p.isExpired() && !p.isReverted())
                .findFirst();

        // No punishment found
        if (latestBan.isEmpty()) return;

        Punishment punishment = latestBan.get();

        // Check if player should be kicked
        ActionDetails actionDetails = ActionManager.getActionFor(punishment.getType());
        if (actionDetails == null || !actionDetails.shouldKickPlayer()) return;

        e.disallow(PlayerLoginEvent.Result.KICK_OTHER, PunishmentManager.getDisconnectMessage(punishment));
    }

    @EventHandler
    public void checkVanished(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        // If the joining player is vanished, hide them from others
        if (Cache.VANISHED_PLAYERS.contains(player.getUniqueId())) {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.hasPermission("sapphire.vanish.immune")) return;
                p.hidePlayer(Sapphire.getInstance(), player);
            });
        }

        // Hide already-vanished players from the joining player
        if (!player.hasPermission("sapphire.vanish.immune")) {
            Cache.VANISHED_PLAYERS.forEach(uuid -> {
                Player vanished = Bukkit.getPlayer(uuid);
                if (vanished != null) {
                    player.hidePlayer(Sapphire.getInstance(), vanished);
                }
            });
        }
    }
}
