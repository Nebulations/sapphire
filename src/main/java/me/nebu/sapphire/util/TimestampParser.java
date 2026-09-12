package me.nebu.sapphire.util;

import me.nebu.sapphire.Sapphire;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampParser {

    public static long parseDuration(String duration) {
        // No duration
        if (duration == null || duration.isBlank()) return Long.MIN_VALUE;

        // Permanent duration
        if (duration.equalsIgnoreCase("PERM") || duration.equalsIgnoreCase("-1")) return Long.MAX_VALUE;

        // 1m = 60000 ms
        // 1h = 3600000 ms
        // 1d = 86400000 ms
        // 1w = 604800000 ms
        return switch (duration.charAt(duration.length() - 1)) {
            case 'm' -> convertDuration(duration, "m", 60000L);
            case 'h' -> convertDuration(duration, "h", 3600000L);
            case 'd' -> convertDuration(duration, "d", 86400000L);
            case 'w' -> convertDuration(duration, "w", 604800000L);

            default -> Long.MIN_VALUE;
        };
    }

    private static long convertDuration(String duration, String replace, long value) {
        try {
            return Long.parseUnsignedLong(duration.replace(replace, "")) * value;
        } catch (NumberFormatException e) {
            Sapphire.getInstance().getLogger().severe("An error occurred while attempting to parse punishment duration: '" + duration + "'");
            return Long.MIN_VALUE;
        }
    }

    public static String parseDate(long time) {
        if (time == Long.MAX_VALUE)
            return "NEVER";

        return new SimpleDateFormat(GeneralSettings.DATE_FORMAT).format(new Date(time));
    }

    public static String toStringDuration(long duration) {
        if (duration == Long.MAX_VALUE) return "Permanent";

        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0)    sb.append(days).append("d ");
        if (hours > 0)   sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

}
