package me.nebu.sapphire.commands;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PunishCommand extends Command {

    public PunishCommand() {
        super("punish");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.punish")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.error(Messenger.PLAYER_ONLY));
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(Messenger.error("No player specified."));
            return false;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Messenger.error("This player has never played before."));
            return false;
        }

        int page = 0;
        if (args.length == 2) {
            String possiblePage = args[1];

            if (possiblePage.startsWith("page:")) {
                try {
                    page = Integer.parseInt(possiblePage.replace("page:", ""));
                } catch (NumberFormatException ignored) {}
            }
        }

        Inventory punishInv = GUIManager.buildPunish(player, target, 5, page);

        List<PunishmentDetails> punishments = PunishmentManager.getPunishmentDetails();

        boolean displayRightArrow = false;

        int startIndex = 10;
        for (int i = page*21; i < punishments.size(); i++) {
            PunishmentDetails punishment = punishments.get(i);

            if (punishment == null) break;

            int index = startIndex + (i % 21);

            if (index == 17 || index == 26) {
                index+=2;
                startIndex+=2;
            }

            punishInv.setItem(index, punishment.getItem());

            if (index >= 34) {
                displayRightArrow = true;
                break;
            }
        }

        if (page*21 != 0) punishInv.setItem(3, GUIManager.buildArrow(false));
        if (displayRightArrow) punishInv.setItem(5, GUIManager.buildArrow(true));

        player.openInventory(punishInv);

        return false;
    }
}
