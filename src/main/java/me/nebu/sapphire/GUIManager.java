package me.nebu.sapphire;

import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportState;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.TimestampParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GUIManager {

    public static Inventory buildDefault(String guiName, Player player, int rows) {
        if (rows > 6 || rows < 2) throw new IllegalStateException("Row amount has to be between 3 and 6.");

        // GUI layout
        Inventory template = Bukkit.createInventory(player, 9*rows, Component.text(guiName));

        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderItemMeta = borderItem.getItemMeta();
        borderItemMeta.setHideTooltip(true);
        borderItem.setItemMeta(borderItemMeta);

        List<Integer> emptySlots = calculateEmptySlots(rows);
        for (int i = 0; i < template.getSize(); i++) {
            if (!emptySlots.contains(i)) template.setItem(i, borderItem);
        }

        return template;
    }

    public static Inventory buildPunish(Player player, OfflinePlayer target, int rows, int page) {
        if (rows > 6 || rows < 2) throw new IllegalStateException("Row amount has to be between 3 and 6.");

        // Default template
        Inventory template = buildDefault("Punish " + target.getName(), player, rows);

        ItemStack slot0 = template.getItem(0);
        if (slot0 != null) {
            ItemMeta meta = slot0.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(Sapphire.getInstance(), "page_index"), PersistentDataType.INTEGER, page);
            meta.getPersistentDataContainer().set(new NamespacedKey(Sapphire.getInstance(), "player"), PersistentDataType.STRING, Objects.requireNonNull(target.getName()));
            slot0.setItemMeta(meta);

            template.setItem(0, slot0);
        }

        // Player head
        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta targetHeadSkullMeta = (SkullMeta) targetHead.getItemMeta();

        targetHeadSkullMeta.setOwningPlayer(target);
        targetHeadSkullMeta.displayName(
                Component.text("History of " + target.getName())
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        targetHeadSkullMeta.lore(List.of(
                Component.text("Click to view " + target.getName() + "'s history.")
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.AQUA)
        ));


        targetHead.setItemMeta(targetHeadSkullMeta);

        template.setItem(4, targetHead);
        return template;
    }

    public static Inventory buildReport(Player player, OfflinePlayer target, int rows, int page) {
        Inventory template = buildDefault("Report " + target.getName(), player, rows);

        ItemStack slot0 = template.getItem(0);
        if (slot0 != null) {
            ItemMeta meta = slot0.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(Sapphire.getInstance(), "page_index"), PersistentDataType.INTEGER, page);
            slot0.setItemMeta(meta);

            template.setItem(0, slot0);
        }

        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta targetHeadSkullMeta = (SkullMeta) targetHead.getItemMeta();

        targetHeadSkullMeta.setOwningPlayer(target);

        targetHeadSkullMeta.displayName(
                Component.text("Report " + target.getName())
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        targetHead.setItemMeta(targetHeadSkullMeta);

        template.setItem(4, targetHead);

        return template;
    }

    public static Inventory buildReports(Player player, OfflinePlayer target, int rows, int page) {
        Inventory template = buildDefault("Reports of " + target.getName(), player, rows);

        ItemStack slot0 = template.getItem(0);
        if (slot0 != null) {
            ItemMeta meta = slot0.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(Sapphire.getInstance(), "page_index"), PersistentDataType.INTEGER, page);
            slot0.setItemMeta(meta);

            template.setItem(0, slot0);
        }

        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta targetHeadSkullMeta = (SkullMeta) targetHead.getItemMeta();

        targetHeadSkullMeta.setOwningPlayer(target);

        targetHeadSkullMeta.displayName(
                Component.text("Viewing " + target.getName() + "'s reports")
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        targetHead.setItemMeta(targetHeadSkullMeta);
        template.setItem(4, targetHead);

        return template;
    }

    public static Inventory buildReportView(Player player, Report report) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(report.getReported());

        Inventory template = buildDefault("Viewing " + report.getId() + " (" + target.getName() + ")", player, 5);

        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta targetHeadSkullMeta = (SkullMeta) targetHead.getItemMeta();

        targetHeadSkullMeta.setOwningPlayer(target);

        targetHeadSkullMeta.displayName(
                Component.text(String.valueOf(target.getName()))
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false)
        );

        PunishmentDetails details = PunishmentManager.getDetailsFor(report.getShortId());

        if (details == null) {
            player.sendMessage(Messenger.error("An error occurred while fetching details for reason '" + report.getShortId() + "'"));
            return template;
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("ID: " + report.getId()).color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Reported by: " + Bukkit.getOfflinePlayer(report.getReported()).getName()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Reported on: " + TimestampParser.parseDate(report.getReportedDate())).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Reported for: " + details.getName()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Status: " + report.getState() + ", " + report.getStatus()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("Left-click to copy ID").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

        targetHeadSkullMeta.lore(lore);

        targetHead.setItemMeta(targetHeadSkullMeta);
        template.setItem(4, targetHead);

        if (report.getState().equals(ReportState.CLOSED)) {
            ItemStack reopen = new ItemStack(Material.ORANGE_WOOL);
            ItemMeta reopenMeta = reopen.getItemMeta();
            reopenMeta.displayName(Component.text("Report closed").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));

            reopenMeta.lore(List.of(
                    Component.text("This report was closed.")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(""),
                    Component.text("Click to reopen").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
            ));

            reopen.setItemMeta(reopenMeta);

            template.setItem(22, reopen);
        } else {
            ItemStack confirm = new ItemStack(Material.LIME_WOOL);
            ItemMeta confirmMeta = confirm.getItemMeta();
            confirmMeta.displayName(Component.text("Confirm report").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            confirm.setItemMeta(confirmMeta);
            template.setItem(20, confirm);

            ItemStack deny = new ItemStack(Material.RED_WOOL);
            ItemMeta denyMeta = deny.getItemMeta();
            denyMeta.displayName(Component.text("Deny report").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            deny.setItemMeta(denyMeta);
            template.setItem(22, deny);

            ItemStack abandon = new ItemStack(Material.LIGHT_GRAY_WOOL);
            ItemMeta abandonMeta = abandon.getItemMeta();
            abandonMeta.displayName(Component.text("Abandon report").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            abandon.setItemMeta(abandonMeta);
            template.setItem(24, abandon);
        }

        return template;
    }

    public static Inventory viewInventory(Player player, Player target) {
        Inventory inv = Bukkit.createInventory(player, 54, Component.text("Inventory of " + target.getName()));

        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderItemMeta = borderItem.getItemMeta();
        borderItemMeta.setHideTooltip(true);
        borderItem.setItemMeta(borderItemMeta);

        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, borderItem);

        Inventory playerInventory = target.getInventory();

        for (int i = 0; i < playerInventory.getContents().length; i++) {
            ItemStack item = playerInventory.getContents()[i];

            // Hotbar
            if (i <= 8) {
                inv.setItem(i + 45, item);
                continue;
            }

            // Inventory
            if (i <= 35) {
                inv.setItem(i + 9, item);
                continue;
            }

            // Armor
            if (i <= 39) {
                inv.setItem(3 + 36 - i, item);
                continue;
            }

            // Offhand
            if (i == 40) {
                inv.setItem(8, item);
                break;
            }
        }

        return inv;
    }

    public static void confirm(Player player, Runnable callback) {
        Inventory inv = Bukkit.createInventory(player, InventoryType.DROPPER, Component.text("Confirm Action"));

        ItemStack pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.text("Confirm").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        pane.setItemMeta(meta);

        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, pane);

        Cache.CONFIRMATION_SCREENS.put(player.getUniqueId(), callback);

        player.openInventory(inv);
    }

    private static List<Integer> calculateEmptySlots(int rows) {
        // 3 = 10 -> 16
        // 4 = 10 -> 16, 19 -> 25
        // 5 = 10 -> 16, 19 -> 25, 28 -> 34
        // 6 = 10 -> 16, 19 -> 25, 28 -> 34, 37 -> 43

        List<Integer> output = new ArrayList<>();

        if (rows >= 3) { output.addAll(intsBetween(10, 16)); }
        if (rows >= 4) { output.addAll(intsBetween(19, 25)); }
        if (rows >= 5) { output.addAll(intsBetween(28, 34)); }
        if (rows >= 6) { output.addAll(intsBetween(37, 43)); }

        return output;
    }

    private static List<Integer> intsBetween(int a, int b) {
        List<Integer> result = new ArrayList<>();
        for (int i = a; i < (b + 1); i++) {
            result.add(i);
        }

        return result;
    }

    public static ItemStack buildArrow(boolean pointingRight) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(
                Component.text(pointingRight ? "Next page" : "Last page")
                        .color(NamedTextColor.DARK_AQUA)
                        .decoration(TextDecoration.ITALIC, false)
        );

        item.setItemMeta(meta);
        return item;
    }

}
