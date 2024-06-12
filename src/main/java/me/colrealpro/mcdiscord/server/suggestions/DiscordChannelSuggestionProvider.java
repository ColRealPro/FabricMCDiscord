package me.colrealpro.mcdiscord.server.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.colrealpro.mcdiscord.MCDiscord;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class DiscordChannelSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder)  {
        MCDiscord.getGuild().getTextChannels().forEach(textChannel -> {
            if (CommandSource.shouldSuggest(builder.getRemaining(), textChannel.getName())) {
                builder.suggest(textChannel.getName());
            }
        });

        return builder.buildFuture();
    }
}
