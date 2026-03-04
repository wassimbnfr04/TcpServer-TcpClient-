package com.chat.client.ui;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageLineParser {
    private static final String SERVER_PREFIX = "Server:";
    private static final String ACTIVE_USERS_PREFIX = "Active users:";
    private static final Pattern CHAT_LINE =
            Pattern.compile("^\\[(\\d{2}:\\d{2}:\\d{2})\\]\\s+([^:]+):\\s*(.*)$");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public UiMessage parse(String rawLine, String localDisplayName) {
        String raw = rawLine == null ? "" : rawLine.trim();
        if (raw.isEmpty()) {
            return new UiMessage("System", "", null, UiMessageType.SYSTEM, raw);
        }

        if (raw.startsWith(ACTIVE_USERS_PREFIX)) {
            String text = raw.substring(ACTIVE_USERS_PREFIX.length()).trim();
            return new UiMessage("Server", text, null, UiMessageType.COMMAND_RESPONSE, raw);
        }

        if (raw.startsWith(SERVER_PREFIX)) {
            String text = raw.substring(SERVER_PREFIX.length()).trim();
            return new UiMessage("Server", text, null, UiMessageType.SYSTEM, raw);
        }

        Matcher matcher = CHAT_LINE.matcher(raw);
        if (matcher.matches()) {
            LocalTime timestamp = parseTime(matcher.group(1));
            String sender = matcher.group(2).trim();
            String text = matcher.group(3);
            boolean outgoing = sender.equalsIgnoreCase(localDisplayName == null ? "" : localDisplayName.trim());
            UiMessageType type = outgoing ? UiMessageType.OUTGOING : UiMessageType.INCOMING;
            return new UiMessage(sender, text, timestamp, type, raw);
        }

        return new UiMessage("System", raw, null, UiMessageType.SYSTEM, raw);
    }

    public UiMessage toLocalOutgoing(String sender, String text) {
        return new UiMessage(sender, text, LocalTime.now(), UiMessageType.OUTGOING, text);
    }

    public boolean isActiveUsersResponse(String rawLine) {
        return rawLine != null && rawLine.startsWith(ACTIVE_USERS_PREFIX);
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value, TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
