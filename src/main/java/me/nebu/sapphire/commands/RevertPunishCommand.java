package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RevertPunishCommand extends Command {

    public RevertPunishCommand() {
        super("revert");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.revert")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.error(Messenger.PLAYER_ONLY));
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(Messenger.error(Messenger.INVALID_ARGUMENTS));
            return false;
        }

        String id = args[0];

        Punishment punishment = PunishmentManager.getPunishment(id);

        if (punishment == null) {
            sender.sendMessage(Messenger.error("Punishment '" + id + "' does not exist."));
            return false;
        }

        if (punishment.isReverted()) {
            sender.sendMessage(Messenger.error("This punishment has already been reverted."));
            return false;
        }

        boolean status = PunishmentManager.revert(punishment.getId());

        if (status) {
            player.sendMessage(Messenger.normal("Successfully reverted punishment '" + id + "'."));
        } else {
            player.sendMessage(Messenger.error("An error occurred. Please contact an administrator."));
        }

        return status;
    }
}
