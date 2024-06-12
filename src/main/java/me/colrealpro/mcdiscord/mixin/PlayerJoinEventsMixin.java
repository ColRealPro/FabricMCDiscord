package me.colrealpro.mcdiscord.mixin;

import com.mojang.authlib.GameProfile;
import me.colrealpro.mcdiscord.events.EventBus;
import me.colrealpro.mcdiscord.events.game.PlayerAttemptLoginEvent;
import me.colrealpro.mcdiscord.events.game.PlayerJoinEvent;
import me.colrealpro.mcdiscord.events.game.PlayerLeaveEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

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

    @Inject(method = "checkCanJoin", at = @At("HEAD"), cancellable = true)
    private void onPlayerPreJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {

        PlayerAttemptLoginEvent event = new PlayerAttemptLoginEvent(profile);
        EventBus.getInstance().dispatch(event);

        if (event.isCancelled()) {
            cir.setReturnValue(event.getKickReason());
        }
    }
}
