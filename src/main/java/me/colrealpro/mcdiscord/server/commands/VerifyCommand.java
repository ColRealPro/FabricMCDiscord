package me.colrealpro.mcdiscord.server.commands;

import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.server.Command;
import me.colrealpro.mcdiscord.server.systems.VerificationHandler;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
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

        String code = VerificationHandler.getVerificationCode(player.getUuid(), player.getName().getString());
        MutableText message = NotificationBuilder.getFormatted("Please DM the following code to ")
            .append(Text.literal(MCDiscord.discordBot.getBot().getSelfUser().getAsTag()).formatted(Formatting.YELLOW))
            .append(Text.literal(": ").formatted(Formatting.WHITE))
            .append(Text.literal(code).formatted(Formatting.YELLOW));

        context.getSource().sendFeedback(() -> message, false);

        return 1;
    }
}
