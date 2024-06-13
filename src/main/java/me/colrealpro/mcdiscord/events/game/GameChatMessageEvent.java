package me.colrealpro.mcdiscord.events.game;

import me.colrealpro.mcdiscord.events.CancellableEvent;
import net.minecraft.server.network.ServerPlayerEntity;

public class GameChatMessageEvent extends CancellableEvent {
    private final String message;
    private final ServerPlayerEntity player;

    public GameChatMessageEvent(ServerPlayerEntity player, String message) {
        this.player = player;
        this.message = message;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }
}
