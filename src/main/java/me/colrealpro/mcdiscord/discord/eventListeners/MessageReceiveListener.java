package me.colrealpro.mcdiscord.discord.eventListeners;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.events.EventBus;
import me.colrealpro.mcdiscord.events.discord.MessageEvent;
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

        EventBus.getInstance().dispatch(new MessageEvent(event));
    }
}
