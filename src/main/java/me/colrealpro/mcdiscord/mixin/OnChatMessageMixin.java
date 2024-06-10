package me.colrealpro.mcdiscord.mixin;

import me.colrealpro.mcdiscord.MCDiscord;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class OnChatMessageMixin {

    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "handleDecoratedMessage", at = @At("HEAD"))
    private void onChatMessage(SignedMessage message, CallbackInfo ci) {
        this.getPlayer().getName();
        MCDiscord.LOGGER.info(message.getContent().getString());

        String discordMessage = String.format("<%s> %s", this.getPlayer().getName().getString(), message.getContent().getString());
        MCDiscord.LOGGER.info(discordMessage);
        MCDiscord.getServer().getPlayerManager().broadcast(Text.literal(discordMessage), true);

        // send to discord
        MCDiscord.discordBot.getBot().getGuildById(MCDiscord.getGuildID()).getChannelById(TextChannel.class, MCDiscord.getDefaultChannelID())
            .sendMessage(discordMessage).queue();
    }
}
