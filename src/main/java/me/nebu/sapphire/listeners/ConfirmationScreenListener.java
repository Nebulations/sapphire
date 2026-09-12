package me.nebu.sapphire.listeners;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.util.Cache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConfirmationScreenListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getOriginalTitle().equals("Confirm Action")) return;

        Player player = (Player) e.getWhoClicked();

        if (!Cache.CONFIRMATION_SCREENS.containsKey(player.getUniqueId())) return;

        e.setCancelled(true);
        player.closeInventory();
        Cache.CONFIRMATION_SCREENS.get(player.getUniqueId()).run();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!e.getView().getOriginalTitle().equals("Confirm Action")) return;

        if (!e.getReason().equals(InventoryCloseEvent.Reason.PLAYER)) return;

        Player player = (Player) e.getPlayer();

        if (!Cache.CONFIRMATION_SCREENS.containsKey(player.getUniqueId())) return;

        Cache.CONFIRMATION_SCREENS.remove(player.getUniqueId());

        player.sendMessage(Messenger.normal("Canceled action."));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Cache.CONFIRMATION_SCREENS.remove(e.getPlayer().getUniqueId());
    }

}
