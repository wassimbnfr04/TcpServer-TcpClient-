package com.chat.server.config;

import java.util.Properties;

public final class ServerConfig {
    private final String host;
    private final String bindHost;
    private final int port;

    private ServerConfig(String host, String bindHost, int port) {
        this.host = host;
        this.bindHost = bindHost;
        this.port = port;
    }

    public static ServerConfig load() {
        Properties properties = ConfigLoader.load();
        String host = properties.getProperty("server.host", "127.0.0.1").trim();
        String bindHost = properties.getProperty("server.bind", host).trim();
        int port = parsePort(properties.getProperty("server.port"), 3000);
        return new ServerConfig(host, bindHost, port);
    }

    public String getHost() {
        return host;
    }

    public String getBindHost() {
        return bindHost;
    }

    public int getPort() {
        return port;
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
