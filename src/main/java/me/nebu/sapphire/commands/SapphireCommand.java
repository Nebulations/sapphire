package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.PunishmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SapphireCommand extends Command {

    public SapphireCommand() {
        super("sapphire");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.sapphire")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(Messenger.error(Messenger.INVALID_ARGUMENTS));
            return false;
        }

        switch (args[0]) {
            case "version":
                sender.sendMessage(Messenger.normal("Version " + Sapphire.getInstance().getPluginMeta().getVersion()));
                break;
            case "reload":
                long now = System.currentTimeMillis();
                sender.sendMessage(Messenger.normal("Reloading"));
                Sapphire.startServices();
                sender.sendMessage(Messenger.normal("Successfully reloaded in " + (System.currentTimeMillis()-now) + "ms."));
                break;
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return List.of("version", "reload");
        }

        return List.of();
    }
}
