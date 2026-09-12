package me.nebu.sapphire.chatfilter;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.commands.SapphireCommand;
import me.nebu.sapphire.util.Configs;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ChatFilter {

    private static List<Filter> filters;

    private static boolean enabled = false;
    private static boolean notifyStaff = false;

    public static void load() {
        Logger logger = Sapphire.getInstance().getLogger();
        FileConfiguration config = Configs.getChatFilterConfig();
        long now = System.currentTimeMillis();
        logger.info("Loading chat filter");

        filters = new ArrayList<>();

        ConfigurationSection chatfilterSection = config.getConfigurationSection("chatfilter");

        if (chatfilterSection == null) {
            logger.severe("Chat filter section is missing, disabling.");
            return;
        }

        enabled = chatfilterSection.getBoolean("enabled", false);
        notifyStaff = chatfilterSection.getBoolean("notify-staff", false);

        if (!enabled) {
            logger.info("Chat filter is disabled.");
            return;
        }

        ConfigurationSection filters = chatfilterSection.getConfigurationSection("filters");
        if (filters == null) {
            logger.info("Chat filter is enabled, but there are no filters configured. Disabling.");
            enabled = false;
            return;
        }

        filters.getKeys(false).forEach(filter -> {
            ConfigurationSection filterSection = filters.getConfigurationSection(filter);

            if (filterSection == null) {
                logger.warning("Failed to load filter '" + filter + "'.");
                return;
            }

            String punishment = filterSection.getString("punishment");
            List<String> patterns = filterSection.getStringList("patterns");

            if (patterns.isEmpty()) {
                logger.warning("Filter " + filter + " was enabled, but no patterns were defined.");
                return;
            }

            ChatFilter.filters.add(new Filter(filter, punishment, patterns));
        });

        logger.info("Successfully loaded in " + (System.currentTimeMillis() - now) + "ms.");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static Filter filter(String content) {
        for (Filter filter : filters) {
            if (filter.filter(content)) return filter;
        }

        return null;
    }

    public static void warnStaff(Player player, String message, Filter filter) {
        Bukkit.getOnlinePlayers()
                .stream().filter(p -> p.hasPermission("sapphire.chatfilter.warnings"))
                .forEach(p -> p.sendMessage(Messenger.normal(player.getName() + " has triggered filter '" + filter.getName() + "'. Message: " + message)));
    }

}
