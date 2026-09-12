package me.nebu.sapphire.reports;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "reports")
public class Report {

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(columnName = "status")
    private String status;

    @DatabaseField(columnName = "state")
    private String state;

    @DatabaseField(columnName = "reporter_uuid")
    private String reporter;

    @DatabaseField(columnName = "reported_uuid")
    private String reported;

    @DatabaseField(columnName = "claimed_uuid")
    private String claimed;

    @DatabaseField(columnName = "reported_on")
    private long reportedDate;

    @DatabaseField(columnName = "reported_for")
    private String shortId;

    public Report() {}

    public Report(String id, ReportStatus status, ReportState state, UUID reporter, UUID reported, long reportedDate, String reportedFor) {
        this.id = id;
        this.status = status.name();
        this.state = state.name();
        this.reporter = reporter.toString();
        this.reported = reported.toString();
        this.reportedDate = reportedDate;
        this.shortId = reportedFor;
    }

    public String getId() {
        return id;
    }

    public ReportStatus getStatus() {
        return ReportStatus.valueOf(status.toUpperCase());
    }
    public void setStatus(ReportStatus status) {
        this.status = status.name();
    }

    public ReportState getState() {
        return ReportState.valueOf(state.toUpperCase());
    }
    public void setState(ReportState state) {
        this.state = state.name();
    }

    public UUID getReporter() {
        return UUID.fromString(reporter);
    }
    public UUID getReported() {
        return UUID.fromString(reported);
    }

    public UUID getClaimedBy() {
        return UUID.fromString(claimed);
    }
    public void setClaimed(UUID claimed) {
        this.claimed = claimed == null ? null : claimed.toString();
    }

    public String getShortId() {
        return shortId;
    }

    public long getReportedDate() {
        return reportedDate;
    }
}
