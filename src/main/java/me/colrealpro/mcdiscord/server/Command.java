package me.colrealpro.mcdiscord.server;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.server.suggestions.DiscordChannelSuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

import java.util.ArrayList;
import java.util.List;

public class Command {
    private final String commandName;
    private final Argument[] arguments;
    private int amountOfArguments = 0;
    private int levelRequirement = 0;

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
                amountOfArguments++;
                break;
            }

            if (i == arguments.length - 1) {
                throw new IllegalArgumentException("Too many arguments");
            }
        }
    }

    public void setLevelRequirement(int permissionLevel) {
        this.levelRequirement = permissionLevel;
    }

    public int onExecute(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public void build() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> commandLiteral = CommandManager.literal(commandName);
            List<RequiredArgumentBuilder<ServerCommandSource, ?>> argumentList = new ArrayList<>();

            commandLiteral.requires(source -> source.hasPermissionLevel(levelRequirement));

            int argumentCounter = 0;
            for (Argument argument : arguments) {
                if (argument == null) {
                    break;
                }

                argumentCounter++;

                if (MCDiscord.debugEnabled()) {
                    MCDiscord.LOGGER.info("Registering argument: {}", argument.getName());
                }

                ArgumentType<?> argumentType = switch (argument.getType()) {
                    case INTEGER -> IntegerArgumentType.integer();
                    case STRING, DISCORD_CHANNEL -> StringArgumentType.string();
                    case BOOLEAN -> BoolArgumentType.bool();
                };

                RequiredArgumentBuilder<ServerCommandSource, ?> newArgument = CommandManager.argument(argument.getName(), argumentType);

                if (argument.getType() == CommandType.DISCORD_CHANNEL) {
                    newArgument.suggests(new DiscordChannelSuggestionProvider());
                }

                argumentList.add(newArgument);

                if (argumentCounter == amountOfArguments) {
                    RequiredArgumentBuilder<ServerCommandSource, ?> lastArgument = null;
                    List<RequiredArgumentBuilder<ServerCommandSource, ?>> reversedArgumentList = new ArrayList<>();

                    for (int i = argumentList.size() - 1; i >= 0; i--) {
                        reversedArgumentList.add(argumentList.get(i));
                    }

                    for (RequiredArgumentBuilder<ServerCommandSource, ?> currentArgument : reversedArgumentList) {
                        if (lastArgument == null) {
                            if (MCDiscord.debugEnabled()) MCDiscord.LOGGER.info("Starting stack with executes on: {}", currentArgument.getName());
                            lastArgument = currentArgument.executes(this::onExecute);
                        } else {
                            if (MCDiscord.debugEnabled()) MCDiscord.LOGGER.info("Stacking argument: {}", currentArgument.getName());
                            lastArgument = currentArgument.then(lastArgument);
                        }
                    }

                    if (MCDiscord.debugEnabled()) MCDiscord.LOGGER.info("Registering command: {}, with argument, {}", commandName, lastArgument.getName());
                    commandLiteral.then(lastArgument);
                }
            }

            if (amountOfArguments == 0) {
                commandLiteral.executes(this::onExecute);
            }

            dispatcher.register(commandLiteral);
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
