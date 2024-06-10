package me.colrealpro.mcdiscord.discord.eventListeners;

import me.colrealpro.mcdiscord.MCDiscord;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MessageReceiveListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) { // prevent mod from sending its own messages in chat
            return;
        }

        String log = String.format("[%s] %#s: %s", event.getChannel(), event.getAuthor(), event.getMessage().getContentDisplay());
        MCDiscord.LOGGER.info(log);

        MutableText message = Text.literal("[").formatted(Formatting.WHITE)
                .append(Text.literal("#" + event.getChannel().getName()).formatted(Formatting.BLUE))
                .append(Text.literal("] ").formatted(Formatting.WHITE))
                .append(Text.literal(event.getMember().getEffectiveName()).formatted(Formatting.WHITE).formatted(Formatting.BOLD))
                .append(Text.literal(" >> ").formatted(Formatting.RESET).formatted(Formatting.DARK_GRAY))
                .append(Text.literal(event.getMessage().getContentDisplay()).formatted(Formatting.RESET).formatted(Formatting.WHITE));
        MCDiscord.getServer().getPlayerManager().broadcast(message, false);
    }
}
