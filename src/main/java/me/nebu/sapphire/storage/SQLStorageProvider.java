package me.nebu.sapphire.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.discord.DiscordBot;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLStorageProvider implements StorageProvider {

    private final ConnectionSource connectionSource;

    private Dao<Punishment, String> punishments;
    private Dao<Report, String> reports;

    public SQLStorageProvider(ConnectionSource source) {
        com.j256.ormlite.logger.LoggerFactory.setLogBackendFactory(new com.j256.ormlite.logger.NullLogBackend.NullLogBackendFactory());

        this.connectionSource = source;

        if (source == null)
            throw new IllegalStateException("Connection source cannot be null.");

        try {
            TableUtils.createTableIfNotExists(source, Punishment.class);
            punishments = DaoManager.createDao(source, Punishment.class);

            TableUtils.createTableIfNotExists(source, Report.class);
            reports = DaoManager.createDao(source, Report.class);
        } catch (Exception e) {
            e.printStackTrace();
            Sapphire.disable();
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connectionSource != null) connectionSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Punishment> getPunishmentHistory(UUID player) {
        try {
            return punishments.queryForEq("player_uuid", player.toString());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Punishment getPunishment(String punishmentId) {
        try {
            return punishments.queryForId(punishmentId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean issuePunishment(UUID player, Punishment punishment) {
        try {
            punishments.create(punishment);
            if (DiscordBot.isEnabled()) DiscordBot.postPunishment(punishment);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean revertPunishment(String punishmentId) {
        try {
            Punishment punishment = getPunishment(punishmentId);

            if (punishment == null) {
                Sapphire.getInstance().getLogger().severe("Failed to load punishment " + punishmentId + ".");
                return false;
            }

            punishment.setReverted(true);

            return editPunishment(punishmentId, punishment);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean editPunishment(String punishmentId, Punishment newPunishment) {
        try {
            punishments.update(newPunishment);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Report getReportById(String reportId) {
        try {
            return reports.queryForId(reportId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Report> getReportsFor(UUID player) {
        try {
            return reports.queryForEq("reported_uuid", player.toString());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Report> getReportsBy(UUID player) {
        try {
            return reports.queryForEq("reporter_uuid", player.toString());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Report> getPendingReports() {
        try {
            return reports.queryForEq("status", ReportStatus.PENDING.name());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean submitReport(Report report) {
        try {
            reports.create(report);
            if (DiscordBot.isEnabled()) DiscordBot.postReport(report);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateReport(Report report) {
        try {
            reports.update(report);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Report getReportClaimedBy(UUID id) {
        try {
            List<Report> r = reports.queryForEq("claimed_uuid", id.toString());

            if (r.isEmpty()) return null;

            return r.getFirst();
        } catch (Exception e) {
            return null;
        }
    }
}
