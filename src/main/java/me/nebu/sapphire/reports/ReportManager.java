package me.nebu.sapphire.reports;

import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.storage.StorageProvider;
import me.nebu.sapphire.util.GeneralSettings;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ReportManager {

    private static final Set<UUID> reportList = new HashSet<>();

    public static boolean report(OfflinePlayer reported, Player reporter, String shortId) {
        StorageProvider storageProvider = Sapphire.getStorageProvider();

        Report report = new Report(
                Sapphire.generateId(),
                ReportStatus.PENDING,
                ReportState.CLOSED,
                reporter.getUniqueId(),
                reported.getUniqueId(),
                System.currentTimeMillis(),
                shortId
        );

        reportList.add(reporter.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                reportList.remove(reporter.getUniqueId());
            }
        }.runTaskLater(Sapphire.getInstance(), GeneralSettings.REPORT_COOLDOWN);

        return storageProvider.submitReport(report);
    }

    public static boolean canReport(UUID player) {
        return !reportList.contains(player);
    }

    public static List<Report> getReportsFor(UUID reported) {
        return Sapphire.getStorageProvider().getReportsFor(reported);
    }

    public static Report getOldestReport() {
        List<Report> pending = Sapphire.getStorageProvider().getPendingReports();
        if (pending.isEmpty()) return null;

        return pending.getFirst();
    }

    public static Report getClaimedReport(UUID claimedId) {
        return Sapphire.getStorageProvider().getReportClaimedBy(claimedId);
    }

    public static boolean update(Report report) {
        return Sapphire.getStorageProvider().updateReport(report);
    }

    public static Report getReportById(String id) {
        return Sapphire.getStorageProvider().getReportById(id);
    }

}
