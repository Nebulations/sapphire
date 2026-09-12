package me.nebu.sapphire.punishments;

import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.actions.ActionDetails;
import me.nebu.sapphire.actions.ActionManager;
import me.nebu.sapphire.util.Cache;
import me.nebu.sapphire.util.Configs;
import me.nebu.sapphire.util.GeneralSettings;
import me.nebu.sapphire.util.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

public class PunishmentManager {

    private static List<PunishmentDetails> punishments;
    private static volatile Set<UUID> recentlyPunished;

    public static void load() {
        Logger logger = Sapphire.getInstance().getLogger();
        FileConfiguration config = Configs.getPunishmentsConfig();
        long now = System.currentTimeMillis();
        logger.info("Loading punishments");

        punishments = new ArrayList<>();
        recentlyPunished = new HashSet<>();
        ConfigurationSection punishmentsSection = config.getConfigurationSection("punishments");

        // No punishments detected
        if (punishmentsSection == null) {
            logger.warning("No punishments were detected. Are you sure your punishments are configured correctly? (Section not found)");
            return;
        }

        Set<String> punishmentKeys = punishmentsSection.getKeys(false);

        // No punishments detected
        if (punishmentKeys.isEmpty()) {
            logger.warning("The punishment section was detected, but no punishments were found.");
            return;
        }

        punishmentKeys.forEach(key -> {
            ConfigurationSection subsection = config.getConfigurationSection("punishments." + key);

            // This is theoretically impossible, but just to be sure.
            if (subsection == null) {
                logger.warning("Failed to load punishment for: " + key);
                return;
            }

            // Punishment section should be clear now.
            String name = subsection.getString("name");
            String id = subsection.getString("id");
            String reason = subsection.getString("reason", "");
            String material = subsection.getString("gui-item");
            boolean reminder = subsection.getBoolean("reminder", false);

            List<Offense> offenses = new ArrayList<>();

            ConfigurationSection offensesSubsection = subsection.getConfigurationSection("offenses");

            if (offensesSubsection == null) return;

            Set<String> offensesKeys = offensesSubsection.getKeys(false);

            offensesKeys.forEach(offenseKey -> {
                ConfigurationSection offenseSubsection = offensesSubsection.getConfigurationSection(offenseKey);

                if (offenseSubsection == null) return;

                PunishmentType type = PunishmentType.valueOf(offenseSubsection.getString("type", "").toUpperCase());
                String duration = offenseSubsection.getString("duration", "0m");

                Offense offense = new Offense(type, duration);

                offenses.add(offense);
            });

            punishments.add(new PunishmentDetails(name, id, reason, material, reminder, offenses));
        });

        logger.info("Successfully loaded " + punishments.size() + " punishments in " + (System.currentTimeMillis() - now) + " ms.");
    }

    public static synchronized boolean wasPunishedRecently(UUID player) {
        return recentlyPunished.contains(player);
    }

    public static List<PunishmentDetails> getPunishmentDetails() {
        return punishments;
    }

    public static boolean punish(OfflinePlayer player, Player issuer, boolean consoleIssued, String shortId, String notes) {
        Punishment punishment = new Punishment(
                player.getUniqueId(),
                issuer.getUniqueId(),
                consoleIssued,
                false,
                Sapphire.generateId(),
                shortId,
                Objects.requireNonNull(resolveType(player, shortId)),
                System.currentTimeMillis(),
                resolveDurationFor(player, shortId),
                notes
        );

        if (player.isOnline())
            handlePlayerDisconnect(player, punishment);

        recentlyPunished.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                recentlyPunished.remove(player.getUniqueId());
            }
        }.runTaskLater(Sapphire.getInstance(), GeneralSettings.PUNISH_COOLDOWN);

        PunishmentDetails details = getDetailsFor(punishment);
        if (details.shouldRemindPlayers()) {
            MiniMessage mm = MiniMessage.miniMessage();

            List<Component> components = Cache.REMINDER_MESSAGE.stream()
                    .map(message -> mm.deserialize(Placeholders.ofPunishmentMessage(
                            message, consoleIssued ? "Console" : issuer.getName(), punishment, details)))
                    .toList();

            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> components.forEach(p::sendMessage));
        }

        return Sapphire.getStorageProvider().issuePunishment(player.getUniqueId(), punishment);
    }

    private static PunishmentType resolveType(OfflinePlayer player, String shortId) {
        List<Punishment> punishments = Sapphire.getStorageProvider().getPunishmentHistory(player.getUniqueId());

        // Get amount of punishments given of the specified short ID
        int count = getPunishmentAmount(punishments, shortId);

        // Find punishment information associated with short ID.
        for (int i = 0; i < getPunishmentDetails().size(); i++) {
            PunishmentDetails details = getPunishmentDetails().get(i);

            if (!details.getId().equalsIgnoreCase(shortId)) continue;

            Offense offense = details.getOffenses().size() > count
                    ? details.getOffenses().get(count)
                    : details.getOffenses().getLast();

            return offense.getType();
        }

        return null;
    }

    private static long resolveDurationFor(OfflinePlayer player, String shortId) {
        List<Punishment> punishments = Sapphire.getStorageProvider().getPunishmentHistory(player.getUniqueId());

        // Get amount of punishments given of the specified short ID
        int count = getPunishmentAmount(punishments, shortId);

        // Find punishment information associated with short ID.
        for (int i = 0; i < getPunishmentDetails().size(); i++) {
            PunishmentDetails details = getPunishmentDetails().get(i);

            if (!details.getId().equalsIgnoreCase(shortId)) continue;

            Offense offense = details.getOffenses().size() <= count
                    ? details.getOffenses().getLast()
                    : details.getOffenses().get(count);

            return offense.getParsedDuration();
        }

        return Long.MIN_VALUE;
    }

    public static int getPunishmentAmount(List<Punishment> punishments, String id) {
        return punishments.stream()
                .filter(p -> p.getShortId().equals(id) && !p.isReverted())
                .toList().size();
    }

    private static void handlePlayerDisconnect(OfflinePlayer player, Punishment punishment) {
        ActionDetails actionDetails = ActionManager.getActionFor(punishment.getType());

        Player onlinePlayer = (Player) player;

        if (actionDetails == null) return;

        MiniMessage mm = MiniMessage.miniMessage();

        List<String> unformattedMessages = actionDetails.getMessages();

        // Play should be kicked -> Format messages accordingly
        if (actionDetails.shouldKickPlayer()) {
            onlinePlayer.kick(getDisconnectMessage(punishment));
        } else {
            for (String unformattedMessage : unformattedMessages) {
                onlinePlayer.sendMessage(mm.deserialize(translatePlaceholders(unformattedMessage, punishment)));
            }

            if (actionDetails.getSound() != null) {
                onlinePlayer.playSound(onlinePlayer, actionDetails.getSound(), actionDetails.getVolume(), actionDetails.getPitch());
            }
        }
    }

    public static Component getDisconnectMessage(Punishment punishment) {
        ActionDetails actionDetails = ActionManager.getActionFor(punishment.getType());
        Component message = Component.empty();

        if (actionDetails == null) return message;

        MiniMessage mm = MiniMessage.miniMessage();

        for (String unformattedMessage : actionDetails.getMessages()) {
            Component line = mm.deserialize("<reset>" + translatePlaceholders(unformattedMessage, punishment));
            message = message.appendNewline().append(line);
        }

        return message;
    }

    private static String translatePlaceholders(String text, Punishment punishment) {
        String issuer = punishment.isConsoleIssued() ? "Console" : Bukkit.getOfflinePlayer(punishment.getIssuer()).getName();

        if (issuer == null) return text;

        Optional<PunishmentDetails> punishmentDetails = PunishmentManager.getPunishmentDetails()
                .stream().filter(p -> p.getId().equalsIgnoreCase(punishment.getShortId()))
                .findFirst();

        return punishmentDetails.map(details -> Placeholders.ofPunishmentMessage(text, issuer, punishment, details)).orElse(text);
    }

    public static List<Punishment> getHistory(OfflinePlayer player) {
        return Sapphire.getStorageProvider().getPunishmentHistory(player.getUniqueId());
    }

    public static PunishmentDetails getDetailsFor(Punishment punishment) {
        return getDetailsFor(punishment.getShortId());
    }

    public static PunishmentDetails getDetailsFor(String shortId) {
        return punishments.stream().
                filter(p -> p.getId().equalsIgnoreCase(shortId))
                .findFirst().orElse(null);
    }

    public static Punishment getPunishment(String id) {
        return Sapphire.getStorageProvider().getPunishment(id);
    }

    public static boolean revert(String id) {
        return Sapphire.getStorageProvider().revertPunishment(id);
    }

}
