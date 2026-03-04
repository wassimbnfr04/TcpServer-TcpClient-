package com.chat.client.ui;

import java.time.LocalTime;
import java.util.Objects;

public final class UiMessage {
    private final String sender;
    private final String text;
    private final LocalTime timestamp;
    private final UiMessageType type;
    private final String raw;

    public UiMessage(String sender, String text, LocalTime timestamp, UiMessageType type, String raw) {
        this.sender = sender == null ? "" : sender;
        this.text = text == null ? "" : text;
        this.timestamp = timestamp;
        this.type = Objects.requireNonNull(type, "type");
        this.raw = raw == null ? "" : raw;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public LocalTime getTimestamp() {
        return timestamp;
    }

    public UiMessageType getType() {
        return type;
    }

    public String getRaw() {
        return raw;
    }
}
