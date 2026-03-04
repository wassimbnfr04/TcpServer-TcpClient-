package com.chat.client.config;

import java.util.List;
import java.util.Properties;

public final class ClientConfig {
    private final String host;
    private final int port;
    private final boolean argsProvided;

    private ClientConfig(String host, int port, boolean argsProvided) {
        this.host = host;
        this.port = port;
        this.argsProvided = argsProvided;
    }

    public static ClientConfig fromArgs(List<String> args) {
        Properties properties = ConfigLoader.load();
        String defaultHost = properties.getProperty("server.host", "127.0.0.1").trim();
        int defaultPort = parsePort(properties.getProperty("server.port"), 3000);

        if (args != null && args.size() >= 2) {
            String host = args.get(0).trim();
            int port = parsePort(args.get(1), defaultPort);
            return new ClientConfig(host, port, true);
        }

        return new ClientConfig(defaultHost, defaultPort, false);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isArgsProvided() {
        return argsProvided;
    }

    private static int parsePort(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
