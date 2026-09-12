package me.nebu.sapphire.actions;

import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.PunishmentType;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.Configs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ActionManager {

    private static List<ActionDetails> actions;

    public static void load() {
        Logger logger = Sapphire.getInstance().getLogger();
        FileConfiguration config = Configs.getPunishmentsConfig();
        long now = System.currentTimeMillis();
        logger.info("Loading actions");

        actions = new ArrayList<>();
        ConfigurationSection actionsSection = config.getConfigurationSection("actions");

        // No actions detected
        if (actionsSection == null) {
            logger.warning("No actions were detected. Are you sure your actions are configured correctly? (Section not found)");
            return;
        }

        Set<String> actionsSectionKeys = actionsSection.getKeys(false);

        // No actions detected
        if (actionsSectionKeys.isEmpty()) {
            logger.warning("The actions section was detected, but no actions were found.");
            return;
        }

        actionsSectionKeys.forEach(key -> {
            ConfigurationSection actionSection = actionsSection.getConfigurationSection(key);

            if (key.equalsIgnoreCase("reminders")) {
                Cache.REMINDER_MESSAGE = config.getStringList("actions.reminders");
                return;
            }

            // This is theoretically impossible, but just to be sure.
            if (actionSection == null) {
                logger.warning("Failed to load action for: " + key);
                return;
            }

            PunishmentType actionType;
            try {
                actionType = PunishmentType.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.severe("Failed to parse action type for '" + key + "'.");
                throw new RuntimeException(e);
            }

            boolean kickPlayer = actionSection.getBoolean("kick-player", false);

            if (actionType.equals(PunishmentType.BAN)) {
                kickPlayer = true;
            }

            Sound sound = null;
            float volume = 0f;
            float pitch = 0f;

            if (!actionType.equals(PunishmentType.BAN)) {
                try {
                    sound = Sound.valueOf(actionSection.getString("sfx.id"));
                } catch (IllegalArgumentException e) {
                    logger.severe("Failed to parse sound ID for '" + key + "'.");
                    throw new IllegalStateException(e);
                }
                volume = (float) actionSection.getDouble("sfx.volume");
                pitch = (float) actionSection.getDouble("sfx.pitch");
            }

            List<String> messages = actionSection.getStringList("messages");

            actions.add(new ActionDetails(actionType, kickPlayer, sound, volume, pitch, messages));
        });

        logger.info("Finished loading actions in " + (System.currentTimeMillis() - now) + " ms.");
    }

    public static ActionDetails getActionFor(PunishmentType punishment) {
        for (ActionDetails action : actions) {
            if (action.getType().equals(punishment)) return action;
        }

        return null;
    }

}
