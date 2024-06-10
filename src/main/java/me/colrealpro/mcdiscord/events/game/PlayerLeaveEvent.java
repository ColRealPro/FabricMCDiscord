package me.colrealpro.mcdiscord.events.game;

import me.colrealpro.mcdiscord.events.CancellableEvent;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerLeaveEvent extends CancellableEvent {
    private final PlayerEntity player;

    public PlayerLeaveEvent(PlayerEntity player) {
        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
