package me.colrealpro.mcdiscord.events.discord;

import me.colrealpro.mcdiscord.events.CancellableEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageEvent extends CancellableEvent {
    private final MessageReceivedEvent rawEvent;
    private final Member author;
    private final MessageChannelUnion channel;
    private final String message;

    public MessageEvent(MessageReceivedEvent rawEvent) {
        this.rawEvent = rawEvent;
        this.author = rawEvent.getMember();
        this.channel = rawEvent.getChannel();
        this.message = rawEvent.getMessage().getContentDisplay();
    }

    public MessageReceivedEvent getRawEvent() {
        return rawEvent;
    }

    public Member getAuthor() {
        return author;
    }

    public MessageChannelUnion getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
