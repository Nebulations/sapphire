package me.nebu.sapphire.listeners;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.reports.ReportManager;
import net.kyori.adventure.text.event.ClickEvent;
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

import java.util.Objects;

public class ReportsGUIListener implements Listener {

    @EventHandler
    public void reportsGUI(InventoryClickEvent e) {
        if (!e.getView().getOriginalTitle().startsWith("Reports of ")) return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        String name = e.getView().getOriginalTitle().replace("Reports of ", "");
        if (item.getType().equals(Material.ARROW) && (e.getRawSlot() == 3 || e.getRawSlot() == 5) && e.getWhoClicked() instanceof Player player) {
            int page = Objects.requireNonNull(e.getInventory().getItem(0))
                    .getPersistentDataContainer()
                    .get(new NamespacedKey(Sapphire.getInstance(), "page_index"), PersistentDataType.INTEGER);

            if (e.getRawSlot() == 3) page--;
            else if (e.getRawSlot() == 5) page++;
            player.performCommand("reports " + name + " page:" + page);

            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        HumanEntity player = e.getWhoClicked();

        if (!target.hasPlayedBefore()) {
            player.sendMessage(Messenger.error("This player has never played before."));
            return;
        }

        ItemMeta meta = item.getItemMeta();

        String id = meta.getPersistentDataContainer()
                .get(new NamespacedKey(Sapphire.getInstance(), "sapphire_report_id"), PersistentDataType.STRING);

        if (id == null) return;

        ((Player) player).performCommand("editreport " + id);
    }

}
