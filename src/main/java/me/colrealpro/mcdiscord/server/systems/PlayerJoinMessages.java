package me.colrealpro.mcdiscord.server.systems;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.events.EventHandler;
import me.colrealpro.mcdiscord.events.game.PlayerJoinEvent;
import me.colrealpro.mcdiscord.events.game.PlayerLeaveEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class PlayerJoinMessages {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MCDiscord.LOGGER.info("{} joined the server (event)", event.getPlayer().getName().getString());

        sendJoinEmbed(event.getPlayer().getName().getString(), true);
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        MCDiscord.LOGGER.info("{} left the server (event)", event.getPlayer().getName().getString());

        sendJoinEmbed(event.getPlayer().getName().getString(), false);
    }

    private void sendJoinEmbed(String playerName, boolean wasJoin) {
        String title = wasJoin ? "Player Joined!" : "Player Left!";
        String playerJoinMessage = wasJoin ? "%s has joined the server" : "%s has left the server";
        String onlinePlayers = " (%s players online)";

        int playerCount = MCDiscord.getServer().getCurrentPlayerCount();
        int color = wasJoin ? 0x73ff40 : 0xff4c38;
        String description = String.format(playerJoinMessage + onlinePlayers, playerName, playerCount);

        MessageEmbed embed = new EmbedBuilder()
            .setTitle(title)
            .setColor(color)
            .setDescription(description)
            .build();

        if (MCDiscord.isStopping()) return; // prevent jda from absolutely fucking killing itself

        MCDiscord.discordBot.getBot().getGuildById(MCDiscord.getGuildID()).getChannelById(TextChannel.class, MCDiscord.getDefaultChannelID())
            .sendMessageEmbeds(embed).queue();
    }
}
