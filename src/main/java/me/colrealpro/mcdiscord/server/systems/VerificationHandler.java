package me.colrealpro.mcdiscord.server.systems;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.config.ConfigHandler;
import me.colrealpro.mcdiscord.events.EventHandler;
import me.colrealpro.mcdiscord.events.discord.MessageEvent;
import me.colrealpro.mcdiscord.events.game.PlayerAttemptLoginEvent;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import me.colrealpro.mcdiscord.utils.StringUtils;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.UUID;

public class VerificationHandler {
    private static final ConfigHandler playerData = MCDiscord.loadConfig("playerData.yml");
    private static final HashMap<UUID, User> userCache = new HashMap<>();

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerAttemptLogin(PlayerAttemptLoginEvent event) {
        if (isVerificationRequired()) {
            YamlDocument config = getDirectConfig();

            if (config == null || playerData == null) {
                event.setCancelled(true);
                event.setKickReason(Text.literal("An error occurred while trying to verify your account (ID: NO_DATA_FOUND)").formatted(Formatting.RED));
                return;
            }

            String discordID = config.getString("Users." + event.getUUID() + ".DiscordID");
            if (discordID != null) {
                return;
            }

            String verificationCode = getVerificationCode(event.getUUID(), event.getName());

            MutableText kickReason = Text.literal("Your account is not linked with discord!").formatted(Formatting.BLUE)
                .append(Text.literal("\nLink your account by sending ").formatted(Formatting.WHITE))
                .append(Text.literal(MCDiscord.discordBot.getBot().getSelfUser().getAsTag()).formatted(Formatting.YELLOW))
                .append(Text.literal(" the following characters").formatted(Formatting.WHITE))
                .append(Text.literal("\n\n" + verificationCode).formatted(Formatting.YELLOW)
                    .append(Text.literal("\n\nIf it fails to verify you please reconnect for a new verification code").formatted(Formatting.WHITE)));

            event.setCancelled(true);
            event.setKickReason(kickReason);
        }
    }

    @EventHandler
    public void onPrivateMessage(MessageEvent event) {
        if (!event.isPrivateMessage()) {
            return;
        }

        String message = event.getMessage();

        // No need to check for a verification code if the message is not the correct length
        if (message.length() != MCDiscord.config.getDirectConfig().getInt("VerificationCodeLength")) {
            event.getRawEvent().getMessage().reply("Invalid verification code length").queue();
            return;
        }

        YamlDocument config = getDirectConfig();

        if (config == null || playerData == null) {
            event.getRawEvent().getMessage().reply("An error occurred while trying to verify your account (ID: NO_DATA_FOUND)").queue();
            return;
        }

        if (!config.contains("VerificationCodes." + message)) {
            event.getRawEvent().getMessage().reply("Invalid verification code").queue();
            return;
        }

        UUID playerUUID = UUID.fromString(config.getString("VerificationCodes." + message + ".PlayerUUID"));
        String playerName = config.getString("Users." + playerUUID + ".CachedUsername");

        config.set("Users." + playerUUID + ".DiscordID", event.getAuthor().getId());
        config.remove("Users." + playerUUID + ".CachedUsername");
        config.remove("Users." + playerUUID + ".ActiveVerificationCode");
        config.remove("VerificationCodes." + message);

        playerData.save();

        event.getRawEvent().getMessage().reply("You have been successfully linked to the Minecraft Account: **" + playerName + "**").queue();

        ServerPlayerEntity player = MCDiscord.getServer().getPlayerManager().getPlayer(playerUUID);

        if (player != null) {
            String accountString = String.format("%s (%s)", event.getAuthor().getEffectiveName(), event.getAuthor().getName());

            if (event.getAuthor().getEffectiveName().equals(event.getAuthor().getName())) {
                accountString = event.getAuthor().getName();
            }

            MutableText confirmationMessage = NotificationBuilder.getFormatted("You were successfully linked to the Discord Account: ")
                .append(Text.literal(accountString).formatted(Formatting.GRAY));
            player.sendMessage(confirmationMessage, false);
        }
    }

    private static YamlDocument getDirectConfig() {
        if (playerData == null) {
            MCDiscord.LOGGER.error("Failed to load playerData.yml (1) - THIS IS LIKELY YOUR FAULT ILL BE HONEST, BUT REPORT IT ANYWAY ON GITHUB");
            return null;
        }

        YamlDocument config = playerData.getDirectConfig();

        if (config == null) {
            MCDiscord.LOGGER.error("Failed to load playerData.yml (2) - THIS IS LIKELY YOUR FAULT ILL BE HONEST, BUT REPORT IT ANYWAY ON GITHUB");
            return null;
        }

        return config;
    }

    public static String getVerificationCode(UUID playerUUID, String username) {
        YamlDocument config = getDirectConfig();

        if (config == null || playerData == null) {
            return "An error occurred while trying to get your verification code";
        }

        String code = config.getString("Users." + playerUUID.toString() + ".ActiveVerificationCode");
        if (code == null) { // code has not been generated yet
            code = StringUtils.generateSalt(MCDiscord.config.getDirectConfig().getInt("VerificationCodeLength"));
            config.set("Users." + playerUUID + ".CachedUsername", username);
            config.set("Users." + playerUUID + ".ActiveVerificationCode", code);
            config.set("VerificationCodes." + code + ".PlayerUUID", playerUUID.toString());
            config.set("VerificationCodes." + code + ".TimeGenerated", System.currentTimeMillis());
            playerData.save();
        }

        return code;
    }

    public static boolean isPlayerVerified(UUID playerUUID) {
        YamlDocument config = getDirectConfig();

        if (config == null || playerData == null) {
            return false;
        }

        return config.contains("Users." + playerUUID + ".DiscordID");
    }

    public static String getDiscordAccount(UUID playerUUID) {
        YamlDocument config = getDirectConfig();

        if (config == null || playerData == null) {
            return null;
        }

        return config.getString("Users." + playerUUID + ".DiscordID");
    }

    public static void addUserToCache(UUID playerUUID, User user) {
        userCache.put(playerUUID, user);
    }

    public static User getUserFromCache(UUID playerUUID) {
        return userCache.get(playerUUID);
    }

    public boolean isVerificationRequired() {
        return MCDiscord.config.getDirectConfig().getBoolean("VerificationRequired");
    }
}
