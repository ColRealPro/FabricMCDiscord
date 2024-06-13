package me.colrealpro.mcdiscord.events.discord;

import me.colrealpro.mcdiscord.events.CancellableEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageEvent extends CancellableEvent {
    private final MessageReceivedEvent rawEvent;
    private final Member member;
    private final User author;
    private final MessageChannelUnion channel;
    private final String message;

    public MessageEvent(MessageReceivedEvent rawEvent) {
        this.rawEvent = rawEvent;
        this.member = rawEvent.getMember();
        this.author = rawEvent.getAuthor();
        this.channel = rawEvent.getChannel();
        this.message = rawEvent.getMessage().getContentDisplay();
    }

    public MessageReceivedEvent getRawEvent() {
        return rawEvent;
    }

    public Member getMember() {
        return member;
    }

    public User getAuthor() {
        return author;
    }

    public MessageChannelUnion getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPrivateMessage() {
        return !channel.getType().isGuild();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
