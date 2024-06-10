package me.colrealpro.mcdiscord.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class MinecraftColors {
    public static MutableText fromHex(String text, String hex) {
        int rgb = Integer.parseInt(hex, 16);
        return fromRGB(text, rgb);
    }

    public static MutableText fromRGB(String text, int rgb) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
        return Text.literal(text).setStyle(style);
    }

    public static MutableText fromFormatting(String text, Formatting formatting) {
        Style style = Style.EMPTY.withFormatting(formatting);
        return Text.literal(text).setStyle(style);
    }

    public static MutableText fromCode(String text, String code) {
        return fromFormatting(text, Formatting.byCode(code.charAt(0)));
    }
}
