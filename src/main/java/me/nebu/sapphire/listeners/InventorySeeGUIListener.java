package me.nebu.sapphire.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventorySeeGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getOriginalTitle().startsWith("Inventory of ")) {
            e.setCancelled(true);
        }
    }

}
