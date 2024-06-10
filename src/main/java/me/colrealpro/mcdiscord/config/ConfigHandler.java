package me.colrealpro.mcdiscord.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.Settings;
import me.colrealpro.mcdiscord.MCDiscord;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConfigHandler {
    private YamlDocument config;
    private boolean loaded = false;

    public ConfigHandler(File file, InputStream defaults) {
        try {
            this.config = YamlDocument.create(file, defaults);
            this.loaded = true;
        } catch (IOException e) {
            MCDiscord.LOGGER.error("Failed to load config: ", e);
        }
    }

    public YamlDocument getDirectConfig() {
        return config;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void save() {
        try {
            config.save();
        } catch (IOException e) {
            MCDiscord.LOGGER.error("Failed to save config: ", e);
        }
    }
}
