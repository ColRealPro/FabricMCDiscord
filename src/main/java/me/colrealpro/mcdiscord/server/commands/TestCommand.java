package me.colrealpro.mcdiscord.server.commands;

import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.server.Command;
import me.colrealpro.mcdiscord.server.CommandType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TestCommand extends Command {

    public TestCommand() {
        super("test");
        super.registerArgument("test", CommandType.INTEGER);
    }

    @Override
    public int onExecute(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("test command executed"), false);

        return 1;
    }
}
