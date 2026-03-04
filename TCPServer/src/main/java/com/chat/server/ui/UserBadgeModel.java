package com.chat.server.ui;

import javafx.scene.paint.Color;

public final class UserBadgeModel {
    private final String username;
    private final Color color;
    private final String statusText;

    public UserBadgeModel(String username, Color color, String statusText) {
        this.username = username == null ? "" : username;
        this.color = color == null ? Color.LIGHTGRAY : color;
        this.statusText = statusText == null ? "" : statusText;
    }

    public String getUsername() {
        return username;
    }

    public Color getColor() {
        return color;
    }

    public String getStatusText() {
        return statusText;
    }
}
