package me.colrealpro.mcdiscord.discord;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.discord.eventListeners.MessageReceiveListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class DiscordBot {
    private final JDABuilder builder;
    private JDA bot;

    public DiscordBot(String token) {
        EnumSet intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT
        );

        this.builder = JDABuilder.createLight(token, intents);
    }

    public JDA getBot() {
        return bot;
    }

    public void start() {
        builder.addEventListeners(new MessageReceiveListener());

        try {
            bot = builder.build();
        } catch (Exception e) {
            MCDiscord.LOGGER.error("Failed to start Discord bot: ", e);
        }
    }

    public void stop() {
        MCDiscord.LOGGER.info("Stopping Discord bot");
        bot.shutdown();
        MCDiscord.LOGGER.info("Discord bot successfully stopped");
    }
}