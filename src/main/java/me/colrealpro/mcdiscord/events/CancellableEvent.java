package me.colrealpro.mcdiscord.events;

public class CancellableEvent {
    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isCancellable() {
        return true;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
