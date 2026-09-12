package me.nebu.sapphire.util;

import me.nebu.sapphire.Sapphire;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Configs {

    private static FileConfiguration config;
    private static YamlConfiguration punishments;
    private static YamlConfiguration chatfilter;

    public static void load() {
        Configs.config = YamlConfiguration.loadConfiguration(createIfNotExists("config.yml"));
        Configs.punishments = YamlConfiguration.loadConfiguration(createIfNotExists("punishments.yml"));
        Configs.chatfilter = YamlConfiguration.loadConfiguration(createIfNotExists("chatfilter.yml"));
    }

    private static File createIfNotExists(String fileName) {
        File file = new File(Sapphire.getInstance().getDataFolder(), fileName);
        if (!file.exists()) {
            Sapphire.getInstance().saveResource(fileName, false);
        }

        return file;
    }

    public static FileConfiguration getConfig() {
        return config;
    }

    public static YamlConfiguration getChatFilterConfig() {
        return chatfilter;
    }

    public static YamlConfiguration getPunishmentsConfig() {
        return punishments;
    }

}
