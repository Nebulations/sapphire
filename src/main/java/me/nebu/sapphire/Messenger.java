package me.nebu.sapphire;

import me.nebu.sapphire.util.Configs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Messenger {

    private static Component prefix;
    private static Component fillerColor;
    private static Component errorColor;

    public static final String NO_PERMISSION = "You do not have permission to run this command.";
    public static final String INVALID_ARGUMENTS = "Invalid amount of arguments provided.";
    public static final String PLAYER_ONLY = "This command can only be executed by players.";

    public static void load() {
        FileConfiguration config = Configs.getConfig();
        Logger logger = Sapphire.getInstance().getLogger();

        ConfigurationSection section = config.getConfigurationSection("general.messages");

        if (section == null) {
            logger.severe("Failed to load custom messages from the config. Disabling");
            Sapphire.disable();
            return;
        }

        MiniMessage mm = MiniMessage.miniMessage();
        Messenger.prefix = mm.deserialize(section.getString("prefix", "<aqua>[Sapphire] "));
        Messenger.fillerColor = mm.deserialize(section.getString("filler-color", "<white>"));
        Messenger.errorColor = mm.deserialize(section.getString("error-color", "<red>"));
    }

    public static Component normal(String message) {
        return prefix.append(fillerColor.append(Component.text(message)));
    }

    public static Component error(String message) {
        return prefix.append(errorColor.append(Component.text(message)));
    }

    public static List<String> warpText(String text) {
        StringBuilder sb = new StringBuilder();
        boolean appendNextWord = false;

        List<String> newText = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ' && appendNextWord) {
                newText.add(sb.toString());

                appendNextWord = false;
                sb.delete(0, sb.length());
                continue;
            }

            sb.append(c);

            if (sb.length() == 40) {
                appendNextWord = true;
            }
        }

        // Add remaining text to the component.
        if (!sb.isEmpty()) {
            newText.add(sb.toString());
        }

        return newText;
    }
}
