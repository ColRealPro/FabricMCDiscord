package me.colrealpro.mcdiscord;

import me.colrealpro.mcdiscord.config.ConfigHandler;
import me.colrealpro.mcdiscord.discord.DiscordBot;
import me.colrealpro.mcdiscord.server.commands.ReloadConfigCommand;
import me.colrealpro.mcdiscord.server.commands.ShowChannelCommand;
import me.colrealpro.mcdiscord.server.commands.UnverifyCommand;
import me.colrealpro.mcdiscord.server.commands.VerifyCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class MCDiscord implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("MCDiscord");
    public static ConfigHandler config;
    private static MinecraftServer server;
    public static DiscordBot discordBot;
    private static boolean stopping = false;
    private static boolean initialized = false;
    private static boolean debug = false;
    private static String guildID;
    private static String channelID;

    @Override
    public void onInitialize() {
        // Initialize config

        config = loadConfig("config.yml");

        if (config == null || !config.isLoaded()) {
            LOGGER.error("Failed to load config, stopping initialization...");
            return;
        }

        debug = config.getDirectConfig().getBoolean("Debug");

        // Register commands

        new ShowChannelCommand().build();
        new ReloadConfigCommand().build();
        new VerifyCommand().build();
        new UnverifyCommand().build();

        LOGGER.info("Commands successfully registered!");

        // REALLY REALLY REALLY hacky method of getting the current minecraft server

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            MCDiscord.server = server;

            if (!initialized) {
                initialized = true;
                continueInitialization();
            }
        });
    }

    private void continueInitialization() {
        // Start Discord bot

        String token = config.getDirectConfig().getString("BotToken");
        boolean usingChannelID = config.getDirectConfig().getBoolean("UseChannelID");
        guildID = config.getDirectConfig().getString("GuildID");

        discordBot = new DiscordBot(token);
        discordBot.start();

        try {
            discordBot.getBot().awaitReady();
        } catch (InterruptedException e) {
            LOGGER.info("Interrupted while waiting for bot to be ready, continuing...");
        }

        if (usingChannelID) {
            channelID = config.getDirectConfig().getString("MessagesChannel");
        } else {
            String channelName = config.getDirectConfig().getString("MessagesChannel");
            Guild guild = discordBot.getBot().getGuildById(guildID);

//            LOGGER.info(Arrays.toString(discordBot.getBot().getGuilds().toArray()));

            if (guild == null) {
                LOGGER.error("Failed to find guild with ID: {}", guildID);
                discordBot.stop();
                return;
            }

//            LOGGER.info(Arrays.toString(guild.getTextChannelsByName(channelName, true).toArray()));

            @Unmodifiable List<TextChannel> Channels = guild.getTextChannelsByName(channelName, true);

            if (Channels.isEmpty()) {
                LOGGER.error("Failed to find channel with name: {}", channelName);
                discordBot.stop();
                return;
            }

            if (Channels.size() > 1) {
                LOGGER.error("Found multiple channels with name: {}", channelName);
                discordBot.stop();
                return;
            }

            channelID = Channels.get(0).getId();
        }

        MessageEmbed startupEmbed = new EmbedBuilder()
            .setTitle("Server Started!")
            .setFooter("MCDiscord - Linking Minecraft and Discord")
            .setColor(0x73ff40)
            .build();

        getDefaultChannel().sendMessageEmbeds(startupEmbed).queue();

        // Stop bot when server stops

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            stopping = true;

            MessageEmbed shutdownEmbed = new EmbedBuilder()
                .setTitle("Server Stopped!")
                .setFooter("MCDiscord - Linking Minecraft and Discord")
                .setColor(0xff4c38)
                .build();

            getDefaultChannel().sendMessageEmbeds(shutdownEmbed).queue();

            discordBot.stop();
        });
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static String getGuildID() {
        return guildID;
    }

    public static String getDefaultChannelID() {
        return channelID;
    }

    public static Guild getGuild() {
        return discordBot.getBot().getGuildById(guildID);
    }

    public static TextChannel getDefaultChannel() {
        return getGuild().getChannelById(TextChannel.class, channelID);
    }

    public static boolean isStopping() {
        return stopping;
    }

    public static boolean debugEnabled() {
        return debug;
    }

    public static ConfigHandler loadConfig(String fileName) {
        File configFile = FabricLoader.getInstance().getConfigDir().resolve("MCDiscord/" + fileName).toFile();
        InputStream defaultConfigFile;
        try {
            URL defaultConfigResource = MCDiscord.class.getResource("/configFiles/" + fileName);

            if (defaultConfigResource == null) {
                LOGGER.error("Failed to find default config file");
                throw new IOException("Failed to find default config file");
            }

            defaultConfigFile = defaultConfigResource.openStream();
        } catch (IOException e) {
            LOGGER.error("Failed to load default config file: ", e);
            return null;
        }

        return new ConfigHandler(configFile, defaultConfigFile);
    }
}
