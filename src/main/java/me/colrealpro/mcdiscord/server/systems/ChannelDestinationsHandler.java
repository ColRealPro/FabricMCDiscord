package me.colrealpro.mcdiscord.server.systems;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.events.EventHandler;
import me.colrealpro.mcdiscord.events.game.GameChatMessageEvent;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class ChannelDestinationsHandler {

    @EventHandler
    public void onGameChatMessage(GameChatMessageEvent event) {
        ChatMessageInformation information = parseMessage(event.getPlayer(), event.getMessage());

        if (information == null) {
            event.getPlayer().sendMessage(NotificationBuilder.getFormatted("An error occurred whilst trying to send the message!"));
            event.setCancelled(true);
            return;
        }

        // Default channel, skip permission check
        if (information.channel == MCDiscord.getDefaultChannel()) {
            sendToChannel(event.getPlayer(), information.message, information.channel);
            return;
        }

        // Check if the player has permissions to send messages to the channel
        String discordID = VerificationHandler.getDiscordAccount(event.getPlayer().getUuid());

        if (discordID == null) {
            event.getPlayer().sendMessage(NotificationBuilder.getFormatted("An error occurred whilst trying to verify permissions: ID_NOT_FOUND"));
            event.setCancelled(true);
            return;
        }

        Member cachedMember = VerificationHandler.getUserFromCache(event.getPlayer().getUuid());

        if (cachedMember == null) {
            cachedMember = MCDiscord.getGuild().getMemberById(discordID);
        }

        event.setCancelled(true);
        if (cachedMember == null) {
            MCDiscord.getGuild().retrieveMemberById(discordID).queue(member -> {
                VerificationHandler.addUserToCache(discordID, member);
                handleExternalChannelMessage(event.getPlayer(), member, information);
            });
        } else {
            VerificationHandler.addUserToCache(discordID, cachedMember);
            handleExternalChannelMessage(event.getPlayer(), cachedMember, information);
        }
    }

    private void sendToChannel(ServerPlayerEntity player, String message, TextChannel channel) {
        String discordMessage = String.format("**<%s>** %s", player.getName().getString(), message);
        channel.sendMessage(discordMessage).queue();
    }

    private void handleExternalChannelMessage(ServerPlayerEntity player, Member member, ChatMessageInformation chatMessageInformation) {
        // Validate permissions

        TextChannel channel = chatMessageInformation.channel;
        String message = chatMessageInformation.message;

        boolean hasPermission = member.hasPermission(channel, Permission.MESSAGE_SEND);
        if (!hasPermission) {
            player.sendMessage(NotificationBuilder.getFormatted("You do not have permission to send messages to this channel!"));
            return;
        }

        sendToChannel(player, message, channel);
    }

    private ChatMessageInformation parseMessage(ServerPlayerEntity player, String message) {
        String channelIndicator = "#";
        if (!message.startsWith(channelIndicator)) {
            return new ChatMessageInformation(message, MCDiscord.getDefaultChannel());
        }

        String messageNoIndicator = message.substring(1);
        String[] splitMessage = messageNoIndicator.split(" " ,2);

        if (splitMessage.length < 2) {
            return new ChatMessageInformation(message, MCDiscord.getDefaultChannel());
        }

        String channelName = splitMessage[0];

        @Unmodifiable List<TextChannel> channelsList = MCDiscord.getGuild().getTextChannelsByName(channelName, true);
        TextChannel channel = channelsList.isEmpty() ? null : channelsList.get(0);

        if (channelsList.size() > 1) {
            player.sendMessage(NotificationBuilder.getFormatted("There is more than 1 channel with this name"));
            return null;
        }

        if (channel == null) {
            return new ChatMessageInformation(message, MCDiscord.getDefaultChannel());
        }

        // Check if the player is verified and has access to this feature
        if (!VerificationHandler.isPlayerVerified(player.getUuid())) {
            player.sendMessage(NotificationBuilder.getFormatted("You must be verified to use this feature!"));
            return null;
        }

        return new ChatMessageInformation(splitMessage[1], channel);
    }

    private record ChatMessageInformation(String message, TextChannel channel) {

    }
}
