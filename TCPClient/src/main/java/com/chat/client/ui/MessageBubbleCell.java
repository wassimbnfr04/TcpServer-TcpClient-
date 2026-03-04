package com.chat.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public final class MessageBubbleCell extends ListCell<UiMessage> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void updateItem(UiMessage item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        if (item.getType() == UiMessageType.SYSTEM || item.getType() == UiMessageType.COMMAND_RESPONSE) {
            setGraphic(buildSystemNode(item));
            setText(null);
            return;
        }

        setGraphic(buildBubbleNode(item));
        setText(null);
    }

    private HBox buildSystemNode(UiMessage message) {
        String text = message.getType() == UiMessageType.COMMAND_RESPONSE && !message.getRaw().isBlank()
                ? message.getRaw()
                : message.getText();
        Label systemLabel = new Label(text);
        systemLabel.getStyleClass().add(
                message.getType() == UiMessageType.COMMAND_RESPONSE ? "command-chip" : "system-chip"
        );
        systemLabel.setWrapText(true);
        systemLabel.setMaxWidth(420);

        HBox row = new HBox(systemLabel);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(6, 0, 6, 0));
        return row;
    }

    private HBox buildBubbleNode(UiMessage message) {
        Label bodyLabel = new Label(message.getText());
        bodyLabel.getStyleClass().add("bubble-body");
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxWidth(430);

        String metaText = message.getSender();
        if (message.getTimestamp() != null) {
            metaText += "  " + message.getTimestamp().format(TIME_FORMATTER);
        }

        Label metaLabel = new Label(metaText);
        metaLabel.getStyleClass().add("bubble-meta");

        VBox bubble = new VBox(4, bodyLabel, metaLabel);
        bubble.getStyleClass().add("message-bubble");
        bubble.getStyleClass().add(message.getType() == UiMessageType.OUTGOING ? "bubble-outgoing" : "bubble-incoming");
        bubble.setMaxWidth(470);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row;
        if (message.getType() == UiMessageType.OUTGOING) {
            row = new HBox(spacer, bubble);
            row.setAlignment(Pos.CENTER_RIGHT);
        } else {
            row = new HBox(bubble, spacer);
            row.setAlignment(Pos.CENTER_LEFT);
        }
        row.setPadding(new Insets(4, 8, 4, 8));
        return row;
    }
}
