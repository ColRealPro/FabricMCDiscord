package me.colrealpro.mcdiscord.server.systems;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.events.EventHandler;
import me.colrealpro.mcdiscord.events.discord.MessageEvent;
import me.colrealpro.mcdiscord.utils.StringUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.UUID;

public class DiscordMessagesHandler {
    private final HashMap<UUID, HashMap<String, Boolean>> enabledChannelsForPlayers = new HashMap<>();

    @EventHandler
    public void onMessage(MessageEvent event) {
        ServerPlayerEntity[] players = MCDiscord.getServer().getPlayerManager().getPlayerList().toArray(new ServerPlayerEntity[0]);

        MCDiscord.LOGGER.info("players: {}", players.length);

        for (ServerPlayerEntity player : players) {
            MCDiscord.LOGGER.info("checking player: {}", player.getName().getString());

            if (!enabledChannelsForPlayers.containsKey(player.getUuid())) {
                HashMap<String, Boolean> newChannelsHashMap = new HashMap<>();
                enabledChannelsForPlayers.put(player.getUuid(), newChannelsHashMap);

                // enable default channel for all players
                String defaultChannelName = MCDiscord.getDefaultChannel().getName();
                newChannelsHashMap.put(defaultChannelName, true);
            }

            HashMap<String, Boolean> channels = enabledChannelsForPlayers.get(player.getUuid());
            String channelName = event.getChannel().getName();

            if (!channels.getOrDefault(channelName, false)) {
                if (MCDiscord.debugEnabled()) {
                    MCDiscord.LOGGER.info("channel {} is disabled for player {}", channelName, player.getName().getString());
                }

                return;
            }

            if (MCDiscord.debugEnabled()) {
                MCDiscord.LOGGER.info("channel {} is enabled for player {}", channelName, player.getName().getString());
            }

            // my god this is ugly
            MutableText message = Text.literal("[").formatted(Formatting.WHITE)
                .append(Text.literal("#" + StringUtils.capitalize(event.getChannel().getName())).formatted(Formatting.BLUE))
                .append(Text.literal("] ").formatted(Formatting.WHITE))
                .append(Text.literal(event.getAuthor().getEffectiveName()).formatted(Formatting.WHITE).formatted(Formatting.BOLD))
                .append(Text.literal(" >> ").formatted(Formatting.RESET).formatted(Formatting.DARK_GRAY))
                .append(Text.literal(event.getMessage()).formatted(Formatting.RESET).formatted(Formatting.WHITE));

            player.sendMessage(message, false);
        }
    }
}
