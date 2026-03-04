package com.chat.client.ui;

import com.chat.client.config.ClientConfig;
import com.chat.client.model.ChatClient;
import com.chat.client.model.ChatClientListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClientController implements ChatClientListener {
    private final ClientConfig config;
    private final ChatClient client;
    private final MessageLineParser parser = new MessageLineParser();

    private final GridPane root = new GridPane();
    private final ObservableList<UiMessage> messageItems = FXCollections.observableArrayList();
    private final ObservableList<String> activeUserItems = FXCollections.observableArrayList();
    private final Set<String> activeUsers = new LinkedHashSet<>();

    private final ListView<UiMessage> messageListView = new ListView<>(messageItems);
    private final ListView<String> activeUsersListView = new ListView<>(activeUserItems);
    private final TextArea messageArea = new TextArea();
    private final Button sendButton = new Button("SEND");
    private final TextField usernameField = new TextField();
    private final Button connectButton = new Button("Connect");
    private final Label serverAddressLabel = new Label();
    private final Label headerStatusLabel = new Label("Offline");
    private final Label sidebarStatusLabel = new Label("Disconnected");
    private final Label readOnlyLabel = new Label("READ-ONLY MODE");
    private final Circle headerStatusDot = new Circle(6, Color.INDIANRED);
    private final Circle sidebarStatusDot = new Circle(5, Color.INDIANRED);

    private boolean connecting;
    private boolean readOnlyMode;
    private String displayName = "Me";

    public ClientController(ClientConfig config) {
        this.config = config;
        this.client = new ChatClient(this);
        serverAddressLabel.setText("Server " + config.getHost() + ":" + config.getPort());
        buildLayout();
        bindHandlers();
        setConnectedState(false);
        appendUiMessage(new UiMessage("System",
                "Welcome. Enter username to chat or leave empty for READ-ONLY MODE.",
                null,
                UiMessageType.SYSTEM,
                "System"));
        if (!config.isArgsProvided()) {
            appendUiMessage(new UiMessage("System",
                    "Using config defaults. Recommended run: java TCPClient <ServerIPAddress> <PortNumber>.",
                    null,
                    UiMessageType.SYSTEM,
                    "System"));
        }
    }

    public Parent getView() {
        return root;
    }

    public void shutdown() {
        client.disconnect();
    }

    @Override
    public void onConnected(boolean readOnly) {
        Platform.runLater(() -> {
            connecting = false;
            readOnlyMode = readOnly;
            setConnectedState(true);
            readOnlyLabel.setVisible(readOnlyMode);
            if (!readOnlyMode) {
                addActiveUser(displayName);
            }
            appendUiMessage(new UiMessage("System",
                    readOnlyMode ? "Connected in READ-ONLY MODE." : "Connected.",
                    null,
                    UiMessageType.SYSTEM,
                    "System"));
            client.sendMessage("allUsers");
        });
    }

    @Override
    public void onMessage(String message) {
        Platform.runLater(() -> {
            UiMessage parsed = parser.parse(message, displayName);
            if (parser.isActiveUsersResponse(message)) {
                updateUsersFromResponse(parsed.getText());
            } else if (parsed.getType() == UiMessageType.SYSTEM) {
                applySystemPresenceUpdate(parsed.getText());
            }
            appendUiMessage(parsed);
        });
    }

    @Override
    public void onDisconnected(String reason) {
        Platform.runLater(() -> {
            connecting = false;
            setConnectedState(false);
            readOnlyMode = false;
            readOnlyLabel.setVisible(false);
            clearUsers();
            appendUiMessage(new UiMessage("System", reason, null, UiMessageType.SYSTEM, reason));
        });
    }

    @Override
    public void onError(String message) {
        Platform.runLater(() -> {
            connecting = false;
            setConnectedState(false);
            readOnlyMode = false;
            readOnlyLabel.setVisible(false);
            clearUsers();
            appendUiMessage(new UiMessage("System", message, null, UiMessageType.SYSTEM, message));
        });
    }

    private void buildLayout() {
        root.setPadding(new Insets(14));
        root.setHgap(14);
        root.setVgap(12);
        root.getStyleClass().add("chat-root");

        ColumnConstraints sidebarCol = new ColumnConstraints();
        sidebarCol.setPrefWidth(280);
        sidebarCol.setMinWidth(260);
        ColumnConstraints mainCol = new ColumnConstraints();
        mainCol.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(sidebarCol, mainCol);

        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(70);
        RowConstraints contentRow = new RowConstraints();
        contentRow.setVgrow(Priority.ALWAYS);
        RowConstraints composerRow = new RowConstraints();
        composerRow.setMinHeight(110);
        root.getRowConstraints().addAll(headerRow, contentRow, composerRow);

        Node sidebar = buildSidebar();
        Node header = buildHeader();
        Node content = buildMessageList();
        Node composer = buildComposer();

        GridPane.setRowSpan(sidebar, 3);
        root.add(sidebar, 0, 0);
        root.add(header, 1, 0);
        root.add(content, 1, 1);
        root.add(composer, 1, 2);
    }

    private Node buildSidebar() {
        Label appTitle = new Label("Group Chat");
        appTitle.getStyleClass().add("sidebar-title");

        Label appSubtitle = new Label("JavaFX TCP Room");
        appSubtitle.getStyleClass().add("sidebar-subtitle");

        HBox connectionBadge = new HBox(8, sidebarStatusDot, sidebarStatusLabel);
        connectionBadge.getStyleClass().add("status-badge");
        connectionBadge.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("sidebar-label");
        usernameField.setPromptText("Enter name (optional)");
        usernameField.getStyleClass().add("sidebar-input");

        connectButton.getStyleClass().add("connect-button");

        Label usersTitle = new Label("Active Users");
        usersTitle.getStyleClass().add("sidebar-label");
        activeUsersListView.getStyleClass().add("active-users-list");
        activeUsersListView.setPlaceholder(new Label("Request with allUsers"));

        VBox sidebar = new VBox(12,
                appTitle,
                appSubtitle,
                connectionBadge,
                usernameLabel,
                usernameField,
                connectButton,
                serverAddressLabel,
                usersTitle,
                activeUsersListView
        );
        sidebar.getStyleClass().add("sidebar-panel");
        VBox.setVgrow(activeUsersListView, Priority.ALWAYS);
        return sidebar;
    }

    private Node buildHeader() {
        Label groupTitle = new Label("General Group");
        groupTitle.getStyleClass().add("header-title");

        Label groupSubtitle = new Label("Messages are broadcast in real time");
        groupSubtitle.getStyleClass().add("header-subtitle");

        VBox left = new VBox(3, groupTitle, groupSubtitle);

        readOnlyLabel.getStyleClass().add("readonly-chip");
        readOnlyLabel.setVisible(false);

        HBox status = new HBox(8, headerStatusDot, headerStatusLabel, readOnlyLabel);
        status.setAlignment(Pos.CENTER_RIGHT);
        status.getStyleClass().add("header-status");

        GridPane header = new GridPane();
        header.getStyleClass().add("header-panel");
        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightCol = new ColumnConstraints();
        header.getColumnConstraints().addAll(leftCol, rightCol);
        header.add(left, 0, 0);
        header.add(status, 1, 0);
        return header;
    }

    private Node buildMessageList() {
        messageListView.getStyleClass().add("message-list");
        messageListView.setCellFactory(list -> new MessageBubbleCell());
        messageListView.setFocusTraversable(false);
        return messageListView;
    }

    private Node buildComposer() {
        messageArea.getStyleClass().add("composer-input");
        messageArea.setPromptText("Type a message or command (allUsers)");
        messageArea.setWrapText(true);
        messageArea.setPrefRowCount(2);
        messageArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleComposerKeyPressed);

        sendButton.getStyleClass().add("send-button");

        GridPane composer = new GridPane();
        composer.getStyleClass().add("composer-panel");
        composer.setHgap(10);
        composer.setPadding(new Insets(8));
        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints buttonCol = new ColumnConstraints();
        buttonCol.setMinWidth(100);
        composer.getColumnConstraints().addAll(inputCol, buttonCol);
        composer.add(messageArea, 0, 0);
        composer.add(sendButton, 1, 0);
        return composer;
    }

    private void bindHandlers() {
        connectButton.setOnAction(event -> connect());
        sendButton.setOnAction(event -> sendMessage());
    }

    private void handleComposerKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            sendMessage();
            event.consume();
        }
    }

    private void connect() {
        if (client.isConnected() || connecting) {
            return;
        }
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        displayName = username.isEmpty() ? "Me" : username;
        readOnlyMode = username.isEmpty();
        connecting = true;
        connectButton.setDisable(true);
        appendUiMessage(new UiMessage("System",
                "Connecting to " + config.getHost() + ":" + config.getPort() + "...",
                null,
                UiMessageType.SYSTEM,
                "System"));
        client.connect(config.getHost(), config.getPort(), username);
    }

    private void sendMessage() {
        if (!client.isConnected()) {
            return;
        }
        String raw = messageArea.getText();
        if (raw == null) {
            return;
        }
        String message = raw.trim();
        if (message.isEmpty()) {
            return;
        }
        messageArea.clear();

        if (isDisconnectCommand(message)) {
            client.disconnect();
            return;
        }

        if (readOnlyMode && !message.equalsIgnoreCase("allUsers")) {
            appendUiMessage(new UiMessage("System",
                    "READ-ONLY MODE - messages are disabled.",
                    null,
                    UiMessageType.SYSTEM,
                    "System"));
            return;
        }

        client.sendMessage(message);
        appendUiMessage(parser.toLocalOutgoing(displayName, message));
    }

    private void setConnectedState(boolean connected) {
        headerStatusLabel.setText(connected ? "Online" : "Offline");
        sidebarStatusLabel.setText(connected ? "Connected" : "Disconnected");
        Color statusColor = connected ? Color.web("#1f9d57") : Color.INDIANRED;
        headerStatusDot.setFill(statusColor);
        sidebarStatusDot.setFill(statusColor);

        usernameField.setDisable(connected);
        connectButton.setDisable(connected || connecting);
        messageArea.setDisable(!connected);
        sendButton.setDisable(!connected);
    }

    private void appendUiMessage(UiMessage message) {
        boolean stickToBottom = isNearBottom();
        messageItems.add(message);
        if (stickToBottom) {
            Platform.runLater(() -> messageListView.scrollTo(messageItems.size() - 1));
        }
    }

    private boolean isNearBottom() {
        ScrollBar vertical = findVerticalScrollBar(messageListView);
        if (vertical == null || !vertical.isVisible()) {
            return true;
        }
        double range = vertical.getMax() - vertical.getMin();
        if (range <= 0) {
            return true;
        }
        double threshold = vertical.getMax() - (range * 0.05);
        return vertical.getValue() >= threshold;
    }

    private ScrollBar findVerticalScrollBar(ListView<?> listView) {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation() == Orientation.VERTICAL) {
                return scrollBar;
            }
        }
        return null;
    }

    private void updateUsersFromResponse(String value) {
        activeUsers.clear();
        if (value == null || value.isBlank() || value.equalsIgnoreCase("(none)")) {
            activeUserItems.clear();
            return;
        }

        String[] users = value.split(",");
        for (String user : users) {
            String trimmed = user.trim();
            if (!trimmed.isEmpty()) {
                activeUsers.add(trimmed);
            }
        }
        activeUserItems.setAll(activeUsers);
    }

    private void applySystemPresenceUpdate(String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        if (text.startsWith("Welcome ")) {
            String assigned = extractAssignedName(text);
            if (!assigned.isEmpty()) {
                if (readOnlyMode) {
                    displayName = assigned;
                }
                addActiveUser(assigned);
            }
            if (text.contains("READ-ONLY MODE")) {
                readOnlyMode = true;
                readOnlyLabel.setVisible(true);
            }
            return;
        }

        String joinedSuffix = " joined the chat.";
        if (text.endsWith(joinedSuffix)) {
            String username = text.substring(0, text.length() - joinedSuffix.length()).trim();
            addActiveUser(username);
            return;
        }

        String leftSuffix = " left the chat.";
        if (text.endsWith(leftSuffix)) {
            String username = text.substring(0, text.length() - leftSuffix.length()).trim();
            removeActiveUser(username);
        }
    }

    private String extractAssignedName(String welcomeText) {
        String content = welcomeText.substring("Welcome ".length()).trim();
        int modeMarker = content.indexOf(" (");
        if (modeMarker > 0) {
            return content.substring(0, modeMarker).trim();
        }
        return content;
    }

    private void addActiveUser(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (activeUsers.add(username.trim())) {
            activeUserItems.setAll(activeUsers);
        }
    }

    private void removeActiveUser(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (activeUsers.remove(username.trim())) {
            activeUserItems.setAll(activeUsers);
        }
    }

    private void clearUsers() {
        activeUsers.clear();
        activeUserItems.clear();
    }

    private static boolean isDisconnectCommand(String message) {
        String lowered = message.trim().toLowerCase();
        return lowered.equals("bye") || lowered.equals("end");
    }
}
