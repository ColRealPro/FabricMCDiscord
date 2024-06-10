package me.colrealpro.mcdiscord.mixin;

import me.colrealpro.mcdiscord.events.EventBus;
import me.colrealpro.mcdiscord.events.game.PlayerJoinEvent;
import me.colrealpro.mcdiscord.events.game.PlayerLeaveEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerJoinEventsMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        // MCDiscord.LOGGER.info("{} joined the server", player.getName().getString());

        EventBus.getInstance().dispatch(new PlayerJoinEvent(player));
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        // MCDiscord.LOGGER.info("{} left the server", player.getName().getString());

        EventBus.getInstance().dispatch(new PlayerLeaveEvent(player));
    }
}
