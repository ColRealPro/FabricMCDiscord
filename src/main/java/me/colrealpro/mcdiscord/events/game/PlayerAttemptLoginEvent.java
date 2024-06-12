package me.colrealpro.mcdiscord.events.game;

import com.mojang.authlib.GameProfile;
import me.colrealpro.mcdiscord.events.CancellableEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.UUID;

public class PlayerAttemptLoginEvent extends CancellableEvent {
    private final GameProfile profile;
    private MutableText kickReason = null;

    public PlayerAttemptLoginEvent(GameProfile profile) {
        this.profile = profile;
    }

    public GameProfile getRawProfile() {
        return profile;
    }

    public String getName() {
        return this.getRawProfile().getName();
    }

    public UUID getUUID() {
        return this.getRawProfile().getId();
    }

    public MutableText getKickReason() {
        return kickReason;
    }

    public void setKickReason(MutableText kickReason) {
        this.kickReason = kickReason;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);

        if (cancelled && kickReason == null) {
            this.setKickReason(Text.literal("Login was cancelled by server"));
        }
    }
}
