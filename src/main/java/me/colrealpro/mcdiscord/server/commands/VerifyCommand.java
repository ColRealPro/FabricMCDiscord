package me.colrealpro.mcdiscord.server.commands;

import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.server.Command;
import me.colrealpro.mcdiscord.server.systems.VerificationHandler;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class VerifyCommand extends Command {
        public VerifyCommand() {
        super("verify");
    }

    @Override
    @SuppressWarnings("deprecation")
    public int onExecute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("You must be a player to use this command"), false);
            return 0;
        }

        if (VerificationHandler.isPlayerVerified(player.getUuid())) {
            context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted("Fetching verification data..."), false);

            String discordID = VerificationHandler.getDiscordAccount(player.getUuid());

            if (discordID == null) return 0; // THIS SHOULD NEVER HAPPEN

            Member cachedUser = VerificationHandler.getUserFromCache(player.getUuid());

            if (cachedUser == null) {
                // attempt to get it from the jda cache, however this doesn't always work -_-
                cachedUser = MCDiscord.getGuild().getMemberById(discordID);
            }

            if (cachedUser == null) {
                MCDiscord.getGuild().retrieveMemberById(discordID).queue(user -> {
                    VerificationHandler.addUserToCache(discordID, user);
                    alreadyVerifiedMessage(context, user.getUser());
                });
            } else {
                VerificationHandler.addUserToCache(discordID, cachedUser); // yes, this will overwrite the user with the same user, but it doesn't matter
                alreadyVerifiedMessage(context, cachedUser.getUser());
            }

            return 0;
        }

        String code = VerificationHandler.getVerificationCode(player.getUuid(), player.getName().getString());
        MutableText message = NotificationBuilder.getFormatted("Please DM the following code to ")
            .append(Text.literal(MCDiscord.discordBot.getBot().getSelfUser().getAsTag()).formatted(Formatting.YELLOW))
            .append(Text.literal(": ").formatted(Formatting.WHITE))
            .append(Text.literal(code).formatted(Formatting.YELLOW));

        context.getSource().sendFeedback(() -> message, false);

        return 1;
    }

    public static void alreadyVerifiedMessage(CommandContext<ServerCommandSource> context, User user) {
        String accountString;

        if (user == null) {
            accountString = "Unknown User";
        } else {
            accountString = String.format("%s (%s)", user.getEffectiveName(), user.getName());

            if (user.getEffectiveName().equals(user.getName())) {
                accountString = user.getName();
            }
        }

        MutableText message = NotificationBuilder.getFormatted("You are already verified to ")
            .append(Text.literal(accountString).formatted(Formatting.YELLOW));

        context.getSource().sendFeedback(() -> message, false);
    }
}
