package me.nebu.sapphire.commands;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportManager;
import me.nebu.sapphire.reports.ReportState;
import me.nebu.sapphire.reports.ReportStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ClaimReportCommand extends Command {

    public ClaimReportCommand() {
        super("claimreport");
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

        if (ReportManager.getClaimedReport(player.getUniqueId()) != null) {
            player.sendMessage(Messenger.error("You have already claimed a report."));
            return false;
        }

        player.sendMessage(Messenger.normal("Retrieving report..."));
        Report report = ReportManager.getOldestReport();
        if (report == null) {
            player.sendMessage(Messenger.error("There are no reports currently available."));
            return false;
        }

        report.setState(ReportState.OPENED);
        report.setStatus(ReportStatus.CLAIMED);
        report.setClaimed(player.getUniqueId());

        boolean status = ReportManager.update(report);
        if (status) {
            player.sendMessage(Messenger.normal("Claimed report " + report.getId() + "."));
            player.performCommand("editreport " + report.getId());
        } else {
            player.sendMessage(Messenger.error("An error occurred while attempting to claim a report. Please contact a server administrator."));
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
