package me.nebu.sapphire.punishments;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PunishmentDetails {

    private ItemStack item;

    private final String name;
    private final String id;
    private final String reason;

    private final boolean reminder;

    private final List<Offense> offenses;

    public PunishmentDetails(String name, String id, String reason, String material, boolean reminder, List<Offense> offenses) {
        this.name = name;
        this.id = id;
        this.reason = reason;
        this.reminder = reminder;
        this.offenses = offenses;

        try {
            item = new ItemStack(Material.valueOf(material.toUpperCase()));
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(name)
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
            );

            List<Component> lore = new ArrayList<>(List.of(
                    Component.text("ID: " + id)
                            .color(NamedTextColor.DARK_GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text(" ")
            ));

            // Limit to 40~ words max per line.
            for (String text : Messenger.warpText(reason)) {
                lore.add(Component.text(text)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
            }

            lore.add(Component.text(" "));
            lore.add(Component.text("Left-click to issue punishment")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Right-click to issue punishment with a note")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);

            item.setItemMeta(meta);

        } catch (Exception e) {
            // If somehow the punishment fails to parse, display a failed
            // punishment item instead. This disables the punishment until
            // the issue is fixed.
            Sapphire.getInstance().getLogger().severe("Failed to load punishment item for " + id + ": '" + material.toUpperCase() + "' does not exist.");
            item = new ItemStack(Material.STRUCTURE_VOID);

            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Failed to create punishment")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
            );

            meta.lore(List.of(
                    Component.text("Check console logs for more information.")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            item.setItemMeta(meta);
        }

        // Sets custom nbt to easily detect that this is a punishment
        // item.
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(
                new NamespacedKey(Sapphire.getInstance(), "sapphire_punish_id"),
                PersistentDataType.STRING,
                id
        );

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ARMOR_TRIM);

        item.setItemMeta(meta);
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public boolean shouldRemindPlayers() {
        return reminder;
    }

    public List<Offense> getOffenses() {
        return offenses;
    }

}
