package me.nebu.sapphire.commands;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InventorySeeCommand extends Command {

    public InventorySeeCommand() {
        super("inventorysee", "View the contents of a player's inventory", "/inventorysee <player>", List.of("invsee", "inv"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.inventorysee")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.error(Messenger.PLAYER_ONLY));
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(Messenger.error(Messenger.INVALID_ARGUMENTS));
            return false;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(Messenger.error("This player does not exist or has never played before."));
            return false;
        }

        player.sendMessage(Messenger.normal("Opening " + target.getName() + "'s inventory."));
        player.openInventory(GUIManager.viewInventory(player, target));

        return true;
    }
}
