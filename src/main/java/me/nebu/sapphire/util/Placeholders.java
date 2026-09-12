package me.nebu.sapphire.util;

import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentDetails;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class Placeholders {

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component staffChat(String message, CommandSender sender) {
        return mm.deserialize(
                GeneralSettings.STAFFCHAT_FORMAT.replace("%name%", sender.getName())
                        .replace("%message%", message)
        );
    }

    public static String ofPunishmentMessage(String text, String issuer, Punishment punishment, PunishmentDetails details) {
        return text.replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(punishment.getPlayer()).getName()))
                .replace("%issuer%", issuer)
                .replace("%reason%", details.getReason())
                .replace("%punish-id%", punishment.getId())
                .replace("%punish-name%", details.getName())
                .replace("%duration%", TimestampParser.toStringDuration(punishment.getDuration()))
                .replace("%time-left%", (punishment.getExpiration() == Long.MAX_VALUE ? "Permanent" : TimestampParser.toStringDuration(punishment.getExpiration() - System.currentTimeMillis())))
                .replace("%expires-on%", TimestampParser.parseDate(punishment.getExpiration()))
                .replace("%issued-on%", TimestampParser.parseDate(punishment.getIssuedDate()));
    }

    public static Component ofSender(String format, CommandSender sender) {
        return ofName(format, sender.getName());
    }

    public static Component ofName(String format, String name) {
        return mm.deserialize(format.replace("%name%", name));
    }

    public static Component mutedChat(CommandSender sender) {
        return mm.deserialize(GeneralSettings.MUTECHAT_FORMAT
                .replace("%name%", sender.getName())
                .replace("%muted%", Cache.CHAT_MUTED ? "muted" : "unmuted"));
    }

}
