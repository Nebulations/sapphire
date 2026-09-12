package me.nebu.sapphire.listeners;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.reports.ReportManager;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.GeneralSettings;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class ReportGUIListener implements Listener {

    @EventHandler
    public void reportGUI(InventoryClickEvent e) {
        if (!e.getView().getOriginalTitle().startsWith("Report ")) return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        String name = e.getView().getOriginalTitle().replace("Report ", "");
        if (item.getType().equals(Material.ARROW) && (e.getRawSlot() == 3 || e.getRawSlot() == 5) && e.getWhoClicked() instanceof Player player) {
            int page = Objects.requireNonNull(e.getInventory().getItem(0))
                    .getPersistentDataContainer()
                    .get(new NamespacedKey(Sapphire.getInstance(), "page_index"), PersistentDataType.INTEGER);

            if (e.getRawSlot() == 3) page--;
            else if (e.getRawSlot() == 5) page++;
            player.performCommand("report " + name + " page:" + page);

            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        HumanEntity player = e.getWhoClicked();

        if (!target.hasPlayedBefore()) {
            player.sendMessage(Messenger.error("This player has never played before."));
            return;
        }

        ItemMeta meta = item.getItemMeta();

        String reason = meta.getPersistentDataContainer()
                .get(new NamespacedKey(Sapphire.getInstance(), "sapphire_report_shortid"), PersistentDataType.STRING);

        if (reason == null) return;

        player.closeInventory();

        boolean status = ReportManager.report(target, (Player) player, reason);
        if (!status) {
            player.sendMessage(Messenger.error("An error occurred while issuing a report for this user. Please contact a server administrator."));
            return;
        }

        player.sendMessage(Messenger.normal("Your report has been sent."));
    }

}
