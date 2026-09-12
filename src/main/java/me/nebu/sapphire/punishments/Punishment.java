package me.nebu.sapphire.punishments;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "punishments")
public class Punishment {

    @DatabaseField(id = true)
    private String id;

    @DatabaseField(columnName = "reverted")
    private boolean reverted;

    @DatabaseField(columnName = "player_uuid")
    private String player;

    @DatabaseField(columnName = "issuer_uuid")
    private String issuer;

    @DatabaseField(columnName = "console_issued")
    private boolean consoleIssued;

    @DatabaseField(columnName = "short_id")
    private String shortId;

    @DatabaseField(columnName = "type")
    private String type;

    @DatabaseField(columnName = "punished_date")
    private long punishedDate;

    @DatabaseField(columnName = "duration")
    private long duration;

    @DatabaseField(columnName = "notes")
    private String notes;

    public Punishment() {}

    public Punishment(UUID player, UUID issuer, boolean consoleIssued, boolean reverted, String id, String shortId, PunishmentType type, long date, long duration, String notes) {
        this.player = player.toString();
        this.issuer = issuer.toString();
        this.consoleIssued = consoleIssued;
        this.id = id;
        this.shortId = shortId;
        this.type = type.name();
        this.punishedDate = date;
        this.duration = duration;
        this.reverted = reverted;
        this.notes = notes;
    }

    public UUID getPlayer() {
        return UUID.fromString(player);
    }

    public UUID getIssuer() {
        return UUID.fromString(issuer);
    }

    public boolean isConsoleIssued() {
        return consoleIssued;
    }

    public boolean isReverted() {
        return reverted;
    }
    public void setReverted(boolean reverted) {
        this.reverted = reverted;
    }

    public String getId() {
        return id;
    }

    public String getShortId() {
        return shortId;
    }

    public PunishmentType getType() {
        return PunishmentType.valueOf(type);
    }

    public long getIssuedDate() {
        return punishedDate;
    }

    public long getDuration() {
        return duration;
    }

    public long getExpiration() {
        if (duration == Long.MAX_VALUE) return Long.MAX_VALUE;
        return punishedDate + duration;
    }

    public boolean isExpired() {
        if (duration == Long.MAX_VALUE) return false;

        return System.currentTimeMillis() >= (punishedDate + duration);
    }

    public String getNotes() {
        return notes;
    }
}
