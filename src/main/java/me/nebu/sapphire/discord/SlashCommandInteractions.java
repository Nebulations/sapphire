package me.nebu.sapphire.discord;

import me.nebu.sapphire.Messenger;
import me.nebu.sapphire.punishments.Punishment;
import me.nebu.sapphire.punishments.PunishmentDetails;
import me.nebu.sapphire.punishments.PunishmentManager;
import me.nebu.sapphire.util.TimestampParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class SlashCommandInteractions extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("history")) return;

        Member member = event.getMember();
        if (member == null) return;

        boolean hasRole = member.getRoles().stream()
                .anyMatch(r -> r.getId().equals(DiscordBot.getStaffRole()));

        if (!hasRole) {
            event.reply(Messenger.NO_PERMISSION)
                    .setEphemeral(true)
                    .queue();
            return;
        }

        OptionMapping usernameOption = event.getOption("username");

        if (usernameOption == null) {
            event.reply("A username must be provided in order to run this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String username = usernameOption.getAsString();

        OfflinePlayer player = Bukkit.getOfflinePlayer(username);

        if (!player.hasPlayedBefore()) {
            event.reply("Unable to fetch " + player.getName() + "'s history.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("History of " + player.getName());

        List<Punishment> history = PunishmentManager.getHistory(player);

        history.forEach(punishment -> {
            PunishmentDetails details = PunishmentManager.getDetailsFor(punishment);

            String message;
            if (!punishment.getNotes().isEmpty()) {
                message = """
                        Issued by %s on %s.
                        
                        Notes:
                        ```
                        %s
                        ```
                        """.formatted(
                        (punishment.isConsoleIssued() ? "Console" : Bukkit.getOfflinePlayer(punishment.getIssuer()).getName()),
                        TimestampParser.parseDate(punishment.getIssuedDate()),
                        punishment.getNotes()
                );
            } else {
                message = "Issued by %s on %s.".formatted(
                    (punishment.isConsoleIssued() ? "Console" : Bukkit.getOfflinePlayer(punishment.getIssuer()).getName()),
                    TimestampParser.parseDate(punishment.getIssuedDate())
                );
            }

            embedBuilder.addField(new MessageEmbed.Field(
                    details.getName(),
                    message,
                    false
            ));
        });

        embedBuilder.setThumbnail("https://mc-heads.net/body/5df3c3f0-ba18-40ea-89a6-515ada7c4bd9");

        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true)
                .queue();
    }
}
