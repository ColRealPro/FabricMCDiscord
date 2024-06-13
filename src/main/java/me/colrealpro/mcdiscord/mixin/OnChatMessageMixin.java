package me.colrealpro.mcdiscord.mixin;

import me.colrealpro.mcdiscord.events.EventBus;
import me.colrealpro.mcdiscord.events.game.GameChatMessageEvent;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class OnChatMessageMixin {

    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "handleDecoratedMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(SignedMessage message, CallbackInfo ci) {
        this.getPlayer().getName();

        GameChatMessageEvent event = new GameChatMessageEvent(this.getPlayer(), message.getContent().getString())
        EventBus.getInstance().dispatch(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
