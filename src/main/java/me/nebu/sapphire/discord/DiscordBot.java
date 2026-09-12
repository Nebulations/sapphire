package me.nebu.sapphire.discord;

import me.nebu.sapphire.Sapphire;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.punishments.PunishmentType;
import me.nebu.sapphire.reports.Report;
import me.nebu.sapphire.reports.ReportManager;
import me.nebu.sapphire.util.Configs;
import me.nebu.sapphire.util.TimestampParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.logging.Logger;

public class DiscordBot {

    private static String punishmentsChannel;
    private static String reportsChannel;
    private static String token;
    private static JDA jda;

    private static boolean enabled = false;
    private static String staffRoleId = "";

    public static void load() {
        Logger logger = Sapphire.getInstance().getLogger();
        FileConfiguration config = Configs.getConfig();
        long now = System.currentTimeMillis();
        logger.info("Loading discord integration");

        ConfigurationSection discordConfig = config.getConfigurationSection("discord");

        if (discordConfig == null) {
            logger.warning("Failed to find discord section in configuration file. Discord bot will not be enabled.");
            return;
        }

        if (!discordConfig.getBoolean("enabled", false)) {
            logger.info("Discord integration is disabled, disabling feature.");
            return;
        }

        discordConfig.getKeys(false).forEach(key -> {
            if (key.equalsIgnoreCase("punishments")) {
                punishmentsChannel = discordConfig.getString(key);
            } else if (key.equalsIgnoreCase("reports")) {
                reportsChannel = discordConfig.getString(key);
            } else if (key.equalsIgnoreCase("token")) {
                token = discordConfig.getString(key);
            } else if (key.equalsIgnoreCase("staff-role")) {
                staffRoleId = discordConfig.getString(key);
            }
        });

        jda = JDABuilder.createLight(token).build();
        jda.updateCommands()
                .addCommands(Commands.slash("history", "Fetch a player's history based off their username")
                        .addOption(OptionType.STRING, "username", "The username to fetch", true)
                        .setContexts(InteractionContextType.GUILD)
                ).queue();

        jda.addEventListener(new SlashCommandInteractions());

        enabled = true;
        logger.info("Successfully loaded in " + (System.currentTimeMillis() - now) + "ms.");
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static String getStaffRole() {
        return staffRoleId;
    }

    public static void postPunishment(Punishment punishment) {
        TextChannel channel = jda.getTextChannelById(punishmentsChannel);
        if (channel == null) {
            Sapphire.getInstance().getLogger().severe("Discord integration is enabled, but the channel with ID '" + punishmentsChannel + "' does not exist.");
            return;
        }

        Color color = Color.BLACK;
        if (punishment.getType().equals(PunishmentType.WARN)) {
            color = Color.YELLOW;
        } else if (punishment.getType().equals(PunishmentType.BAN)) {
            color = Color.RED;
        } else if (punishment.getType().equals(PunishmentType.MUTE)) {
            color = Color.CYAN;
        }

        String type = switch (punishment.getType()) {
            case WARN -> "Warning";
            case MUTE -> "Mute";
            case BAN -> "Ban";
        };

        PunishmentDetails details = PunishmentManager.getDetailsFor(punishment.getShortId());

        channel.sendMessageEmbeds(new EmbedBuilder()
                        .setThumbnail("https://mc-heads.net/body/" + punishment.getPlayer())
                        .setDescription("""
                                **Punishment | %s**
                                
                                Reason: %s
                                Punishment ID: `%s`
                                Username: %s
                                Expires on: %s
                                %s
                                """.formatted(
                                type,

                                details.getName(),
                                punishment.getId(),
                                Bukkit.getOfflinePlayer(punishment.getPlayer()).getName(),
                                punishment.getType().equals(PunishmentType.WARN) ? "N/A" : TimestampParser.parseDate(punishment.getExpiration()) + " (<t:" + punishment.getExpiration()/1000 + ":R>)",

                                punishment.getNotes().isEmpty() ? "" : "\nNotes:\n```" + punishment.getNotes() + "```"
                        ))
                        .setFooter("Issued by " + Bukkit.getOfflinePlayer(punishment.getIssuer()).getName(),
                                "https://mc-heads.net/avatar/" + punishment.getIssuer())
                        .setTimestamp(Instant.ofEpochMilli(punishment.getIssuedDate()))
                        .setColor(color)
                        .build())
                .addActionRow(Button.link("https://namemc.com/search?q=" + punishment.getPlayer(), "\uD83D\uDCCB View profile on NameMC"))
                .queue();
    }

    public static void postReport(Report report) {
        TextChannel channel = jda.getTextChannelById(reportsChannel);
        if (channel == null) {
            Sapphire.getInstance().getLogger().severe("Discord integration is enabled, but the channel with ID '" + reportsChannel + "' does not exist.");
            return;
        }

        PunishmentDetails details = PunishmentManager.getDetailsFor(report.getShortId());

        channel.sendMessageEmbeds(new EmbedBuilder()
                        .setDescription("""
                                **Report | %s**
                                
                                Reason: %s
                                Report ID: %s
                                Accused player: %s
                                """.formatted(
                                details.getName(),

                                details.getReason(),
                                report.getId(),
                                Bukkit.getOfflinePlayer(report.getReported()).getName()
                        ))
                        .setFooter("Reported by " + Bukkit.getOfflinePlayer(report.getReporter()).getName(),
                                "https://mc-heads.net/avatar/" + report.getReporter())
                        .setTimestamp(Instant.ofEpochMilli(report.getReportedDate()))
                        .setColor(Color.PINK)
                        .build())
                .addActionRow(Button.link("https://namemc.com/search?q=" + report.getReported(), "\uD83D\uDCCB View profile on NameMC"))
                .queue();
    }

    public static void shutdown() {
        // Discord extension is disabled, so we don't do anything.
        if (!enabled) return;

        try {
            jda.awaitShutdown();
        } catch (Exception ignored) {}
    }

}