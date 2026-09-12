package me.nebu.sapphire.listeners;

import me.nebu.sapphire.GUIManager;
import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportManager;
import me.nebu.sapphire.reports.ReportState;
import me.nebu.sapphire.reports.ReportStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ViewingReportGUIListener implements Listener {

    @EventHandler
    public void viewingGUI(InventoryClickEvent e) {
        String title = e.getView().getOriginalTitle();
        if (!title.startsWith("Viewing ")) return;
        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();

        if (item == null) return;

        String reportId = title.replace("Viewing ", "")
                .substring(0, 8);

        Report report = ReportManager.getReportById(reportId);
        if (report == null) { return; }

        Player player = (Player) e.getWhoClicked();

        int slot = e.getRawSlot();

        // Report is closed and was handled previously
        if (slot == 22 && (report.getState().equals(ReportState.CLOSED)
                && !report.getStatus().equals(ReportStatus.PENDING))) {
            if (!player.hasPermission("sapphire.reports.modify")) {
                player.sendMessage(Messenger.error("You do not have permission to modify reports."));
                return;
            }
            report.setState(ReportState.OPENED);
            report.setStatus(ReportStatus.CLAIMED);
            report.setClaimed(player.getUniqueId());

            boolean status = ReportManager.update(report);
            if (status) {
                player.performCommand("editreport " + report.getId());
            } else {
                player.sendMessage(Messenger.error("An error occurred. Please contact a server administrator."));
            }
            return;
        }

        if (!(slot == 20 || slot == 22 || slot == 24)) return;

        GUIManager.confirm(player, () -> {
            switch (slot) {
                case 20:
                    report.setStatus(ReportStatus.CONFIRM);
                    break;
                case 22:
                    report.setStatus(ReportStatus.DENY);
                    break;
                case 24:
                    report.setStatus(ReportStatus.CANCELLED);
                    break;
            }

            report.setState(ReportState.CLOSED);
            report.setClaimed(null);

            boolean status = ReportManager.update(report);
            if (status) {
                player.sendMessage(Messenger.normal("Successfully updated & closed report " + reportId + "."));
            } else {
                player.sendMessage(Messenger.error("An error occurred. Please contact a server administrator."));
            }
        });
    }

}
