package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class VanishCommand extends Command {

    public VanishCommand() {
        super("vanish", "Hide yourself from other players.", "/vanish", List.of("v"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.vanish")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.error(Messenger.PLAYER_ONLY));
            return false;
        }

        boolean isVanished = Cache.VANISHED_PLAYERS.contains(player.getUniqueId());

        if (isVanished)
            Cache.VANISHED_PLAYERS.remove(player.getUniqueId());
        else
            Cache.VANISHED_PLAYERS.add(player.getUniqueId());

        String message = !isVanished ? "You are now hidden." : "You are now visible.";
        player.sendMessage(Messenger.normal(message));

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (p.hasPermission("sapphire.vanish.immune")) return;

            if (!isVanished) {
                p.hidePlayer(Sapphire.getInstance(), player);
            } else {
                p.showPlayer(Sapphire.getInstance(), player);
            }
        });

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
