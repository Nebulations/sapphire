package me.nebu.sapphire.commands;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.util.TimestampParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HistoryCommand extends Command {

    public HistoryCommand() {
        super("history");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.history")) {
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

        Inventory historyGUI = GUIManager.buildDefault("History of " + target.getName(), player, 5);

        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta targetHeadSkullMeta = (SkullMeta) targetHead.getItemMeta();

        targetHeadSkullMeta.setOwningPlayer(target);
        targetHeadSkullMeta.displayName(
                Component.text("History of " + target.getName())
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        targetHead.setItemMeta(targetHeadSkullMeta);
        historyGUI.setItem(4, targetHead);

        List<Punishment> punishments = PunishmentManager.getHistory(target).reversed();

        boolean displayRightArrow = false;

        int startIndex = 10;
        for (int i = page*21; i < punishments.size(); i++) {
            Punishment punishment = punishments.get(i);
            if (punishment == null) break;

            PunishmentDetails details = PunishmentManager.getDetailsFor(punishment);

            ItemStack item = details.getItem();
            ItemMeta meta = item.getItemMeta();
            meta.displayName(
                    Component.text(details.getName() + (punishment.isReverted() ? " (Reverted)" : ""))
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.YELLOW)
            );

            String duration = TimestampParser.toStringDuration(punishment.getDuration());

            List<Component> lore = new ArrayList<>(List.of(
                    Component.text("ID: " + punishment.getId())
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.DARK_GRAY),
                    Component.empty(),
                    Component.text("Type: " + punishment.getType().name()).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY),
                    Component.text("Issued by: " + (punishment.isConsoleIssued() ? "Console" : Bukkit.getOfflinePlayer(punishment.getIssuer()).getName())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY),
                    Component.text("Issued on: " + TimestampParser.parseDate(punishment.getIssuedDate())).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY),
                    Component.text("Duration: " + (duration.isEmpty() ? "N/A" : duration)).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY),
                    Component.text("Expired: " + (punishment.isExpired() ? "YES" : "NO")).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GRAY)
            ));

            // Add notes if there are some
            if (punishment.getNotes() != null && !punishment.getNotes().isEmpty()) {
                lore.add(Component.text(""));
                lore.add(Component.text("Notes: " ).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));

                Messenger.warpText(punishment.getNotes()).forEach(s ->
                        lore.add(Component.text(s).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            }

            lore.add(Component.empty());
            lore.add(Component.text("Left-click to copy ID").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));
            lore.add(Component.text("Right-click to revert").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW));

            meta.getPersistentDataContainer().set(
                    new NamespacedKey(Sapphire.getInstance(), "sapphire_history_id"),
                    PersistentDataType.STRING,
                    punishment.getId()
            );

            meta.lore(lore);
            item.setItemMeta(meta);

            int index = startIndex + (i % 21);
            if (index == 17 || index == 26) {
                index+=2;
                startIndex+=2;
            }

            historyGUI.setItem(index, item);

            if (index >= 34) {
                displayRightArrow = true;
                break;
            }
        }

        if (page*21 != 0) historyGUI.setItem(3, GUIManager.buildArrow(false));
        if (displayRightArrow) historyGUI.setItem(5, GUIManager.buildArrow(true));
        player.openInventory(historyGUI);

        return false;
    }
}
