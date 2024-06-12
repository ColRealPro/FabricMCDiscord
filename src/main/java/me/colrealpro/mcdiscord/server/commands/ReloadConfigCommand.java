package me.colrealpro.mcdiscord.server.commands;

import com.mojang.brigadier.context.CommandContext;
import me.colrealpro.mcdiscord.MCDiscord;
import me.colrealpro.mcdiscord.server.Command;
import me.colrealpro.mcdiscord.utils.NotificationBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ReloadConfigCommand extends Command {

    public ReloadConfigCommand() {
        super("reloadconfig");
        super.setLevelRequirement(1);
    }

    @Override
    public int onExecute(CommandContext<ServerCommandSource> context) {
        try {
            Set<Object> keys = MCDiscord.config.getDirectConfig().getKeys();
            HashMap<Object, Object> values = new HashMap<>();
            List<HashMap<String, Object>> changedValues = new ArrayList<>();

            for (Object key : keys) {
                values.put(key, MCDiscord.config.getDirectConfig().get(key.toString()));
            }

            MCDiscord.config.getDirectConfig().reload();

            for (Object key : keys) {
                if (!values.get(key).equals(MCDiscord.config.getDirectConfig().get(key.toString()))) {
                    HashMap<String, Object> changedValue = new HashMap<>();
                    changedValue.put("key", key);
                    changedValue.put("oldValue", values.get(key));
                    changedValue.put("newValue", MCDiscord.config.getDirectConfig().get(key.toString()));
                    changedValues.add(changedValue);
                }
            }

            MutableText changedValuesText = Text.literal("\nChanged values: ").formatted(Formatting.GRAY);
            for (HashMap<String, Object> changedValue : changedValues) {
                changedValuesText.append(Text.literal("\n- ").formatted(Formatting.GRAY)
                        .append(Text.literal(changedValue.get("key").toString()).formatted(Formatting.YELLOW))
                        .append(Text.literal(": ").formatted(Formatting.GRAY))
                        .append(Text.literal(changedValue.get("oldValue").toString()).formatted(Formatting.RED))
                        .append(Text.literal(" -> ").formatted(Formatting.GRAY))
                        .append(Text.literal(changedValue.get("newValue").toString()).formatted(Formatting.GREEN)));
            }

            context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted("Config reloaded!").append(changedValuesText), false);
        } catch (IOException e) {
            context.getSource().sendFeedback(() -> NotificationBuilder.getFormatted("Failed to reload config!"), false);
            MCDiscord.LOGGER.error("Failed to reload config: ", e);
        }
        return 1;
    }
}
