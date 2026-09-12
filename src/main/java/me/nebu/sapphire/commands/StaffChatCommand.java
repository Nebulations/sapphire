package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StaffChatCommand extends Command {

    public StaffChatCommand() {
        super("staffchat", "Chat in-game to staff only.", "/staffchat <message>", List.of("sc", "s", "schat"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.staffchat")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(Messenger.error(Messenger.INVALID_ARGUMENTS));
            return false;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (String text : args) {
            messageBuilder.append(text).append(" ");
        }

        String message = messageBuilder.toString();

        // Remove redundant space at the end.
        String finalMessage = message.substring(0, message.length() - 1);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("sapphire.staffchat"))
                .forEach(target -> target.sendMessage(Placeholders.staffChat(finalMessage, sender)));

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return new ArrayList<>();
    }
}
