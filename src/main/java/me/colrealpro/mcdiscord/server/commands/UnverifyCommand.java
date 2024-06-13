package me.colrealpro.mcdiscord.server.commands;

import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.server.Command;
import me.colrealpro.mcdiscord.server.systems.VerificationHandler;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class UnverifyCommand extends Command {

    public UnverifyCommand() {
        super("unverify");
    }

    @Override
    public int onExecute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if (player == null) {
            context.getSource().sendFeedback(() -> Text.literal("You must be a player to use this command"), false);
            return 0;
        }

        VerificationHandler.removeVerification(player.getUuid());

        if (VerificationHandler.isVerificationRequired()) {
            player.networkHandler.disconnect(Text.literal("You have been unverified. Please verify your account again."));
        } else {
            context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted("You have been unverified, you will no longer be able to use verified-only features!"), false);
        }

        return 1;
    }
}
