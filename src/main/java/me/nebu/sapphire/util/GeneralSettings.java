package me.nebu.sapphire.util;

import me.nebu.sapphire.Sapphire;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class GeneralSettings {

    public static String STAFFCHAT_FORMAT = "";
    public static String CLEARCHAT_FORMAT = "";
    public static String MUTECHAT_FORMAT = "";
    public static String MUTECHAT_SPEAK_ATTEMPT = "";
    public static String DATE_FORMAT = "";

    public static long REPORT_COOLDOWN = 0;
    public static long PUNISH_COOLDOWN = 200;

    public static void load() {
        Logger logger = Sapphire.getInstance().getLogger();
        FileConfiguration config = Configs.getConfig();
        long now = System.currentTimeMillis();
        logger.info("Loading general settings");

        ConfigurationSection settings = config.getConfigurationSection("general");

        if (settings == null || settings.getKeys(false).isEmpty()) {
            logger.severe("Failed to load general settings: No general settings found.");
            Sapphire.disable();
            return;
        }

        STAFFCHAT_FORMAT = settings.getString("staffchat-format");
        CLEARCHAT_FORMAT = settings.getString("clearchat-format");
        MUTECHAT_FORMAT = settings.getString("mutechat-format");
        MUTECHAT_SPEAK_ATTEMPT = settings.getString("mutechat-speak-attempt");
        DATE_FORMAT = settings.getString("date-format", "");
        REPORT_COOLDOWN = settings.getLong("report-cooldown", 60)*20; // Multiply to convert to ticks

        try {
            new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            DATE_FORMAT = "dd/MM/yy HH:mm:ss";
            logger.severe("An error occurred while attempting to load the following date format: '" + DATE_FORMAT + "'. Falling back to default format.");
        }

        logger.info("Successfully loaded in " + (System.currentTimeMillis() - now) + "ms.");
    }
}
