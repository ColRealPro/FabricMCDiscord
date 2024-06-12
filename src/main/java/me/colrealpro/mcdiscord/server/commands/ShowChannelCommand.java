package me.colrealpro.mcdiscord.server.commands;

import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.server.Command;
import me.colrealpro.mcdiscord.server.CommandType;
import me.colrealpro.mcdiscord.server.systems.DiscordMessagesHandler;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class ShowChannelCommand extends Command {

    public ShowChannelCommand() {
        super("show");
        super.registerArgument("channel", CommandType.DISCORD_CHANNEL);
        super.registerArgument("enabled", CommandType.BOOLEAN);
    }

    @Override
    public int onExecute(CommandContext<ServerCommandSource> context) {
        String channel = context.getArgument("channel", String.class);
        boolean enabled = context.getArgument("enabled", Boolean.class);

        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("You must be a player to run this command!"), false);
            return 0;
        }

        if (!DiscordMessagesHandler.doesChannelExist(channel)) {
            context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted("That channel doesn't exist!"), false);
            return 0;
        }

        if (DiscordMessagesHandler.isDuplicateChannel(channel)) {
            context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted("There is more than 1 channel with this name, and cannot be displayed"), false);
            return 0;
        }

        UUID playerUUID = player.getUuid();
        DiscordMessagesHandler.setChannelEnabledForPlayer(playerUUID, channel, enabled);

        String confirmationMessage;

        if (enabled) confirmationMessage = String.format("#%s will now be displayed in chat", channel);
        else confirmationMessage = String.format("#%s will no longer be displayed in chat", channel);

        context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted(confirmationMessage), false);

        return 1;
    }
}
