package me.colrealpro.mcdiscord.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NotificationBuilder {

    public static MutableText getFormatted(String message) {
        return Text.literal("[").formatted(Formatting.WHITE)
            .append(Text.literal("MCDiscord").formatted(Formatting.BLUE).formatted(Formatting.BOLD))
            .append(Text.literal("] ").formatted(Formatting.WHITE))
            .append(Text.literal(message).formatted(Formatting.WHITE));
    }
}
