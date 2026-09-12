package me.nebu.sapphire.commands;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportManager;
import me.nebu.sapphire.reports.ReportState;
import me.nebu.sapphire.reports.ReportStatus;
import me.nebu.sapphire.util.Cache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class UpdateReportCommand extends Command {

    public UpdateReportCommand() {
        super("editreport");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sapphire.reports.claim")) {
            sender.sendMessage(Messenger.error(Messenger.NO_PERMISSION));
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messenger.error(Messenger.PLAYER_ONLY));
            return false;
        }

        if (args.length == 0) {
            Report claimedReport = ReportManager.getClaimedReport(player.getUniqueId());
            if (claimedReport == null) {
                player.sendMessage(Messenger.error("Please specify an ID to edit the report."));
                return false;
            }

            player.openInventory(GUIManager.buildReportView(player, claimedReport));
            return true;
        }

        if (!player.hasPermission("sapphire.reports.edit")) {
            player.sendMessage(Messenger.error("You can only edit your own report."));
            return true;
        }

        String reportId = args[0];
        Report report = ReportManager.getReportById(reportId);

        if (report == null) {
            player.sendMessage(Messenger.error("Report " + reportId + " does not exist."));
            return false;
        }

        player.openInventory(GUIManager.buildReportView(player, report));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
