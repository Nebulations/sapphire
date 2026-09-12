package me.nebu.sapphire.storage;

import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.reports.Report;

import java.util.List;
import java.util.UUID;

public interface StorageProvider {

    // Punishment related methods
    List<Punishment> getPunishmentHistory(UUID player);
    Punishment getPunishment(String punishmentId);
    boolean issuePunishment(UUID player, Punishment punishment);
    boolean revertPunishment(String punishmentId);
    boolean editPunishment(String punishmentId, Punishment newPunishment);

    // Report related methods
    Report getReportById(String reportId);
    List<Report> getPendingReports();
    List<Report> getReportsFor(UUID player);
    List<Report> getReportsBy(UUID player);
    Report getReportClaimedBy(UUID id);
    boolean updateReport(Report report);
    boolean submitReport(Report report);

    void shutdown();
}
