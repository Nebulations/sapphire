package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.GeneralSettings;
import me.nebu.sapphire.util.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MuteChatCommand extends Command {

    public MuteChatCommand() {
        super("mutechat", "Prevent people from speaking in chat.", "/mutechat", List.of("lockchat"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.mutechat")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        Cache.CHAT_MUTED = !Cache.CHAT_MUTED;

        Bukkit.broadcast(Placeholders.mutedChat(sender));

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
