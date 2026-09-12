package me.nebu.sapphire.commands;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.reports.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReportCommand extends Command {

    public ReportCommand() {
        super("report");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.reports.report")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.error(Messenger.PLAYER_ONLY));
            return false;
        }

        if (!ReportManager.canReport(player.getUniqueId())) {
            player.sendMessage(Messenger.error("You have already reported a player recently. Try again later."));
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

        Inventory reportGUI = GUIManager.buildReport(player, target, 5, page);

        List<PunishmentDetails> punishments = PunishmentManager.getPunishmentDetails();

        boolean displayRightArrow = false;

        int startIndex = 10;
        for (int i = page*21; i < punishments.size(); i++) {
            PunishmentDetails details = punishments.get(i);

            if (details == null) {
                Sapphire.getInstance().getLogger().severe("Failed to load punishment details for a punishment.");
                continue;
            }

            ItemStack item = details.getItem();
            ItemMeta meta = item.getItemMeta();

            meta.getPersistentDataContainer().set(
                    new NamespacedKey(Sapphire.getInstance(), "sapphire_report_shortid"),
                    PersistentDataType.STRING, details.getId()
            );

            meta.displayName(
                    Component.text(details.getName())
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.YELLOW)
            );

            List<Component> lore = new ArrayList<>();

            lore.add(Component.text(""));

            for (String text : Messenger.warpText(details.getReason())) {
                lore.add(Component.text(text)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }

            lore.add(Component.text(""));

            lore.add(Component.text("Click to report")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            item.setItemMeta(meta);

            int index = startIndex + (i % 21);
            if (index == 17 || index == 26) {
                index+=2;
                startIndex+=2;
            }

            reportGUI.setItem(index, item);

            if (index >= 34) {
                displayRightArrow = true;
                break;
            }
        }

        if (page*21 != 0) reportGUI.setItem(3, GUIManager.buildArrow(false));
        if (displayRightArrow) reportGUI.setItem(5, GUIManager.buildArrow(true));
        player.openInventory(reportGUI);

        return false;
    }

}
