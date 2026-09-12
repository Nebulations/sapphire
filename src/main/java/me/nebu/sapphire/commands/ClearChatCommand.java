package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.util.GeneralSettings;
import me.nebu.sapphire.util.Placeholders;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ClearChatCommand extends Command {

    public ClearChatCommand() {
        super("clearchat", "Clears in-game chat to hide messages.", "/clearchat", List.of("cc"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.clearchat")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        Component message = Component.empty();
        for (int i = 0; i < 100; i++) message = message.appendNewline();

        Component finalMessage = Placeholders.ofSender(GeneralSettings.CLEARCHAT_FORMAT, sender);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("sapphire.clearchat.immune")) continue;

            player.sendMessage(message);
            player.sendMessage(finalMessage);
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
