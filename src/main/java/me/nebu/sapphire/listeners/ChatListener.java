package me.nebu.sapphire.listeners;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.chatfilter.ChatFilter;
import me.nebu.sapphire.chatfilter.Filter;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.punishments.PunishmentType;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.GeneralSettings;
import me.nebu.sapphire.util.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChatListener implements Listener {

    // Note-taking for moderators
    @EventHandler
    public void noteCheck(PlayerChatEvent e) {
        Player player = e.getPlayer();

        String targetStringUUID = player.getPersistentDataContainer()
                .get(new NamespacedKey(Sapphire.getInstance(), "sapphire_taking_notes"), PersistentDataType.STRING);
        String punishId = player.getPersistentDataContainer()
                .get(new NamespacedKey(Sapphire.getInstance(), "sapphire_punishment_reason"), PersistentDataType.STRING);

        if (targetStringUUID == null || punishId == null) return;

        e.setCancelled(true);

        player.getPersistentDataContainer()
                .remove(new NamespacedKey(Sapphire.getInstance(), "sapphire_punishment_reason"));
        player.getPersistentDataContainer()
                .remove(new NamespacedKey(Sapphire.getInstance(), "sapphire_taking_notes"));

        OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(targetStringUUID));

        if (!target.hasPlayedBefore()) return;

        String notes = e.getMessage();

        if (notes.equalsIgnoreCase("cancel")) {
            player.sendMessage(Messenger.normal("Canceled punishment."));
            return;
        }

        GUIManager.confirm(player, () -> {
            boolean status = PunishmentManager.punish(target, player, false, punishId, notes);
            if (!status) {
                player.sendMessage(Messenger.error("An error occurred while issuing a punishment for this user. Please contact a server administrator."));
                return;
            }
            player.sendMessage(Messenger.normal("Issued punishment '" + punishId + "' to " + target.getName() + ". Notes: " + notes));
        });
    }

    // Handle player muted chat
    @EventHandler
    public void mutedCheck(AsyncChatEvent e) {
        Player player = e.getPlayer();

        List<Punishment> history = PunishmentManager.getHistory(player);
        Optional<Punishment> mute = history.stream()
                .filter(punishment -> (!punishment.isExpired() && punishment.getType().equals(PunishmentType.MUTE)))
                .findFirst();

        if (mute.isEmpty()) return;

        e.setCancelled(true);

        player.sendMessage(PunishmentManager.getDisconnectMessage(mute.get()));
    }

    // Handle globally muted chat
    @EventHandler
    public void mutedChat(AsyncChatEvent e) {
        if (e.getPlayer().hasPermission("sapphire.mutechat.immune")) return;
        if (!Cache.CHAT_MUTED) return;

        e.setCancelled(true);
        e.getPlayer().sendMessage(Placeholders.ofName(GeneralSettings.MUTECHAT_SPEAK_ATTEMPT, e.getPlayer().getName()));
    }

    // Handle clicking in chat to punish
    @EventHandler(priority = EventPriority.LOWEST)
    public void chatPunish(AsyncChatEvent e) {
        e.renderer((source, sourceDisplayName, message, viewer) -> {
            if (viewer instanceof Player player && player.hasPermission("sapphire.staff")) {
                Component modifiedName = sourceDisplayName
                        .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to punish this user.").color(NamedTextColor.AQUA)))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "punish " + e.getPlayer().getName()));

                return ChatRenderer.defaultRenderer().render(source, modifiedName, message, viewer);
            }
            return ChatRenderer.defaultRenderer().render(source, sourceDisplayName, message, viewer);
        });
    }

    // Handle chat filter
    @EventHandler(priority = EventPriority.LOWEST)
    public void chatFilter(AsyncChatEvent e) {
        if (!ChatFilter.isEnabled()) return;

        String message = ((TextComponent) e.message()).content();

        Filter filter = ChatFilter.filter(message);

        if (filter == null) return;
        Player player = e.getPlayer();

        if (player.hasPermission("sapphire.chatfilter.immune." + filter.getName()) || player.hasPermission("sapphire.chatfilter.immune.*")) return;

        e.setCancelled(true);

        ChatFilter.warnStaff(player, message, filter);

        if (filter.getPunishment() == null) return;

        PunishmentDetails details = PunishmentManager.getPunishmentDetails().stream()
                .filter(p -> p.getId().equalsIgnoreCase(filter.getPunishment()))
                .findFirst().orElse(null);

        if (details == null) {
            Sapphire.getInstance().getLogger().severe("Failed to apply punishment " + filter.getPunishment() + " to " + player.getName() + ".");
            return;
        }

        PunishmentManager.punish(player, player, true, details.getId(), "Chat filter trigger. (" + filter.getName() + ")");
    }

}
