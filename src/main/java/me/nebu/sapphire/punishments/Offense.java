package me.nebu.sapphire.punishments;

import me.nebu.sapphire.util.TimestampParser;

public class Offense {

    private final PunishmentType type;
    private final String duration;
    private final long parsedDuration;

    public Offense(PunishmentType type, String duration) {
        this.type = type;
        this.duration = duration;
        this.parsedDuration = TimestampParser.parseDuration(duration);
    }

    public PunishmentType getType() {
        return type;
    }

    public String getDuration() {
        return duration;
    }

    public long getParsedDuration() {
        return parsedDuration;
    }

    @Override
    public String toString() {
        return "Offense{" +
                "type=" + type +
                ", duration='" + duration + '\'' +
                ", parsedDuration=" + parsedDuration +
                '}';
    }
}
