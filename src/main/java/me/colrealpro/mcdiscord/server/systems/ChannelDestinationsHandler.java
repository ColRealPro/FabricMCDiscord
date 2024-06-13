package me.colrealpro.mcdiscord.server.systems;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.events.EventHandler;
import me.colrealpro.mcdiscord.events.game.GameChatMessageEvent;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ChannelDestinationsHandler {
    private final HashMap<UUID, TextChannel> previousChannel = new HashMap<>();

    @EventHandler
    public void onGameChatMessage(GameChatMessageEvent event) {
        ChatMessageInformation information = parseMessage(event.getPlayer(), event.getMessage());

        if (information == null) {
//            event.getPlayer().sendMessage(NotificationBuilder.getFormatted("An error occurred whilst trying to send the message!"));
            event.setCancelled(true);
            return;
        }

        // Cancel event if player intended to send their message to a different channel
        if (information.setByUser) {
            event.setCancelled(true);
        }

        // Default channel, skip permission check
        if (information.channel == MCDiscord.getDefaultChannel()) {
            sendToChannel(event.getPlayer(), information);
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

    private void sendToChannel(ServerPlayerEntity player, ChatMessageInformation information) {
        TextChannel channel = information.channel;
        String message = information.message;
        String discordMessage = String.format("**<%s>** %s", player.getName().getString(), message);
        channel.sendMessage(discordMessage).queue();

        if (channel.equals(MCDiscord.getDefaultChannel())) {
            if (information.setByUser) { // why would you do this to me D:
                // strip the channel name from the message

                MutableText chatMessage = Text.literal("")
                    .append(Text.literal("<" + player.getName().getString() + "> " + message).formatted(Formatting.WHITE));

                MCDiscord.getServer().getPlayerManager().broadcast(chatMessage, false);
            }

            return;
        }

        previousChannel.put(player.getUuid(), channel);

        MutableText chatMessage = Text.literal("")
            .append(Text.literal("[#" + channel.getName() + "] ").formatted(Formatting.GRAY, Formatting.ITALIC))
            .append(Text.literal("<" + player.getName().getString() + "> " + message).formatted(Formatting.RESET).formatted(Formatting.WHITE));

        for (ServerPlayerEntity playerEntity : MCDiscord.getServer().getPlayerManager().getPlayerList()) {
            if (playerEntity.equals(player) || DiscordMessagesHandler.isChannelEnabledForPlayer(playerEntity.getUuid(), channel.getName())) {
                playerEntity.sendMessage(chatMessage, false);
            }
        }
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

        sendToChannel(player, chatMessageInformation);
    }

    private ChatMessageInformation parseMessage(ServerPlayerEntity player, String message) {
        String channelIndicator = "#";
        if (!message.startsWith(channelIndicator)) {
            return new ChatMessageInformation(message, MCDiscord.getDefaultChannel(), false);
        }

        String messageNoIndicator = message.substring(1);
        String[] splitMessage = messageNoIndicator.split(" " ,2);

        if (splitMessage.length < 2) {
            return new ChatMessageInformation(message, MCDiscord.getDefaultChannel(), false);
        }

        String channelName = splitMessage[0];

        @Unmodifiable List<TextChannel> channelsList = new ArrayList<>();

        if (channelName.isEmpty()) {
            // attempt to use the previous channel the player used
            TextChannel previousChannel = this.previousChannel.get(player.getUuid());
            if (previousChannel != null) {
                channelsList.add(previousChannel);
            } else {
                player.sendMessage(NotificationBuilder.getFormatted("You do not have a previous channel to send messages to and must specify a channel name!"));
                return null;
            }
        } else {
            channelsList = MCDiscord.getGuild().getTextChannelsByName(channelName, true);
        }

        TextChannel channel = channelsList.isEmpty() ? null : channelsList.get(0);

        if (channelsList.size() > 1) {
            player.sendMessage(NotificationBuilder.getFormatted("There is more than 1 channel with this name"));
            return null;
        }

        if (channel == null) {
            return new ChatMessageInformation(message, MCDiscord.getDefaultChannel(), false);
        }

        // Check if the player is verified and has access to this feature
        if (!VerificationHandler.isPlayerVerified(player.getUuid())) {
            player.sendMessage(NotificationBuilder.getFormatted("You must be verified to use this feature!"));
            return null;
        }

        return new ChatMessageInformation(splitMessage[1], channel, true);
    }

    private record ChatMessageInformation(String message, TextChannel channel, boolean setByUser) {

    }
}
