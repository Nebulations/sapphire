package me.nebu.sapphire.listeners;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentManager;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class HistoryGUIListener implements Listener {

    @EventHandler
    public void historyGUI(InventoryClickEvent e) {
        String title = e.getView().getOriginalTitle();
        if (!title.startsWith("History of ")) return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();

        if (item == null) return;

        if (item.getType().equals(Material.ARROW) && (e.getRawSlot() == 3 || e.getRawSlot() == 5) && e.getWhoClicked() instanceof Player player) {
            int page = Objects.requireNonNull(e.getInventory().getItem(0))
                    .getPersistentDataContainer()
                    .get(new NamespacedKey(Sapphire.getInstance(), "page_index"), PersistentDataType.INTEGER);
            String name = Objects.requireNonNull(e.getInventory().getItem(0))
                    .getPersistentDataContainer()
                    .get(new NamespacedKey(Sapphire.getInstance(), "player"), PersistentDataType.STRING);

            if (e.getRawSlot() == 3) page--;
            else if (e.getRawSlot() == 5) page++;
            player.performCommand("history " + name + " page:" + page);

            return;
        }

        Player player = (Player) e.getWhoClicked();

        String id = item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey(Sapphire.getInstance(), "sapphire_history_id"),
                PersistentDataType.STRING
        );

        if (id == null) return;

        player.closeInventory();
        if (e.isLeftClick()) {
            player.sendMessage(Messenger.normal("Click to copy '" + id + "' to the clipboard.")
                    .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id)));
        } else if (e.isRightClick()) {
            if (!player.hasPermission("sapphire.revert")) {
                player.sendMessage(Messenger.error("You do not have permission to revert punishments."));
                return;
            }

            if (PunishmentManager.getPunishment(id).isReverted()) {
                player.sendMessage(Messenger.error("This punishment has already been reverted."));
                return;
            }

            GUIManager.confirm(player, () -> {
                boolean status = PunishmentManager.revert(id);

                if (status) {
                    player.sendMessage(Messenger.normal("Successfully reverted punishment '" + id + "'."));
                } else {
                    player.sendMessage(Messenger.error("An error occurred. Please contact a server administrator."));
                }
            });
        }
    }

}
