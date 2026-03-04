package com.chat.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {
    private ConfigLoader() {
    }

    public static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getResourceAsStream("/config.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
        }
        return properties;
    }
}
