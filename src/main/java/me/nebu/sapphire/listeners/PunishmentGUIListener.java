package me.nebu.sapphire.listeners;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.PunishmentManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class PunishmentGUIListener implements Listener {

    @EventHandler
    public void punishGUI(InventoryClickEvent e) {
        if (!e.getView().getOriginalTitle().startsWith("Punish ")) return;
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
            player.performCommand("punish " + name + " page:" + page);

            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(e.getView().getOriginalTitle().replace("Punish ", ""));
        Player player = (Player) e.getWhoClicked();

        if (e.getRawSlot() == 4) {
            player.performCommand("history " + target.getName());
            return;
        }

        String punishId = item.getItemMeta()
                .getPersistentDataContainer()
                .get(new NamespacedKey(Sapphire.getInstance(), "sapphire_punish_id"), PersistentDataType.STRING);

        if (punishId == null) return;

        if (!target.hasPlayedBefore()) {
            player.sendMessage(Messenger.error("This player has never played before."));
            return;
        }

        player.closeInventory();

        if (PunishmentManager.wasPunishedRecently(target.getUniqueId())) {
            player.sendMessage(Messenger.error("This player has recently been punished."));
            return;
        }

        if (e.isLeftClick()) {
            GUIManager.confirm(player, () -> {
                boolean status = PunishmentManager.punish(target, player, false, punishId, "");
                if (!status) {
                    player.sendMessage(Messenger.error("An error occurred while issuing a punishment for this user. Please contact a server administrator."));
                    return;
                }
                player.sendMessage(Messenger.normal("Issued punishment '" + punishId + "' to " + target.getName() + "."));
            });
        } else {
            player.sendMessage(Messenger.normal("Add notes in chat, or enter \"cancel\" to cancel:"));

            player.getPersistentDataContainer()
                    .set(
                            new NamespacedKey(Sapphire.getInstance(), "sapphire_taking_notes"),
                            PersistentDataType.STRING,
                            target.getUniqueId().toString()
                    );
            player.getPersistentDataContainer()
                    .set(
                            new NamespacedKey(Sapphire.getInstance(), "sapphire_punishment_reason"),
                            PersistentDataType.STRING,
                            punishId
                    );
        }
    }

}
