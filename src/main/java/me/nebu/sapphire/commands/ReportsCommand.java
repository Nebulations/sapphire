package me.nebu.sapphire.commands;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportManager;
import me.nebu.sapphire.util.TimestampParser;
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

public class ReportsCommand extends Command {

    public ReportsCommand() {
        super("reports");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.reports.view")) {
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

        Inventory punishInv = GUIManager.buildReports(player, target, 5, page);

        List<Report> reports = ReportManager.getReportsFor(target.getUniqueId());

        boolean displayRightArrow = false;

        int startIndex = 10;
        for (int i = page*21; i < reports.size(); i++) {
            Report report = reports.get(i);

            if (report == null) break;

            int index = startIndex + (i % 21);
            if (index == 17 || index == 26) {
                index+=2;
                startIndex+=2;
            }

            PunishmentDetails details = PunishmentManager.getDetailsFor(report.getShortId());

            if (details == null) continue;

            ItemStack item = details.getItem();
            ItemMeta meta = item.getItemMeta();

            meta.getPersistentDataContainer()
                            .set(new NamespacedKey(Sapphire.getInstance(), "sapphire_report_id"), PersistentDataType.STRING, report.getId());

            meta.displayName(Component.text(details.getName())
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("ID: " + report.getId()).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("Reported by: " + Bukkit.getOfflinePlayer(report.getReported()).getName()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Reported on: " + TimestampParser.parseDate(report.getReportedDate())).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Reported for: " + details.getName()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("Status: " + report.getStatus() + ", " + report.getState()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text(""));
            lore.add(Component.text("Click to edit report").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            item.setItemMeta(meta);

            punishInv.setItem(index, item);

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
