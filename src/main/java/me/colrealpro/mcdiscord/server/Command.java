package me.colrealpro.mcdiscord.server;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public class Command {
    final String commandName;
    final Argument[] arguments;

    public Command(String commandName) {
        this.commandName = commandName.toLowerCase();
        this.arguments = new Argument[10];
    }

    public void registerArgument(String name, CommandType type) {
        Argument argument = new Argument(name, type);
        assert arguments.length < 10 : "Too many arguments";

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                arguments[i] = argument;
                break;
            }

            if (i == arguments.length - 1) {
                throw new IllegalArgumentException("Too many arguments");
            }
        }
    }

    public int onExecute(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public void build() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> commandLiteral = CommandManager.literal(commandName);

            for (Argument argument : arguments) {
                if (argument == null) {
                    break;
                }

                switch (argument.getType()) {
                    case INTEGER -> commandLiteral.then(CommandManager.argument(argument.getName(), IntegerArgumentType.integer()));
                    case STRING -> commandLiteral.then(CommandManager.argument(argument.getName(), StringArgumentType.string()));
                    case BOOLEAN -> commandLiteral.then(CommandManager.argument(argument.getName(), BoolArgumentType.bool()));
                }
            }

            dispatcher.register(commandLiteral.executes(this::onExecute));
        });
    }

    record Argument(String name, CommandType type) {
        public String getName() {
            return name;
        }

        public CommandType getType() {
            return type;
        }
    }
}
