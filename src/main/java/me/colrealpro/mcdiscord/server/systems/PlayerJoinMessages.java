package me.colrealpro.mcdiscord.server.systems;

import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.events.EventHandler;
import me.colrealpro.mcdiscord.events.game.PlayerJoinEvent;
import me.colrealpro.mcdiscord.events.game.PlayerLeaveEvent;

public class PlayerJoinMessages {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MCDiscord.LOGGER.info("{} joined the server (event)", event.getPlayer().getName().getString());
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent event) {
        MCDiscord.LOGGER.info("{} left the server (event)", event.getPlayer().getName().getString());
    }
}
