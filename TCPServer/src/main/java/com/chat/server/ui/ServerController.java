package com.chat.server.ui;

import com.chat.server.config.ServerConfig;
import com.chat.server.model.ChatServer;
import com.chat.server.model.ChatServerListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ServerController implements ChatServerListener {
    private final GridPane root = new GridPane();
    private final TextArea logArea = new TextArea();
    private final ListView<UserBadgeModel> userList = new ListView<>();
    private final ObservableList<UserBadgeModel> userItems = FXCollections.observableArrayList();
    private final Map<String, Color> userColors = new HashMap<>();
    private final Circle statusDot = new Circle(6, Color.INDIANRED);
    private final Label statusLabel = new Label("Offline");
    private final Label serverAddressLabel = new Label("Bind: -");
    private final Label onlineCountLabel = new Label("0 online");

    private ChatServer server;

    public ServerController() {
        buildLayout();
    }

    public Parent getView() {
        return root;
    }

    public void start() {
        ServerConfig config = ServerConfig.load();
        serverAddressLabel.setText("Bind: " + config.getBindHost() + ":" + config.getPort());
        server = new ChatServer(config, this);
        server.start();
    }

    public void shutdown() {
        if (server != null) {
            server.stop();
        }
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> logArea.appendText(message + System.lineSeparator()));
    }

    @Override
    public void onUsersChanged(List<String> users) {
        Platform.runLater(() -> updateUsers(users));
    }

    @Override
    public void onStatusChanged(boolean online, String detail) {
        Platform.runLater(() -> {
            statusLabel.setText(online ? "Online" : "Offline");
            statusDot.setFill(online ? Color.web("#2f9b5b") : Color.INDIANRED);
        });
    }

    private void buildLayout() {
        root.setPadding(new Insets(14));
        root.setHgap(14);
        root.setVgap(12);
        root.getStyleClass().add("server-root");

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setMinWidth(250);
        leftCol.setPrefWidth(280);
        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setHgrow(Priority.ALWAYS);
        root.getColumnConstraints().addAll(leftCol, rightCol);

        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(78);
        RowConstraints contentRow = new RowConstraints();
        contentRow.setVgrow(Priority.ALWAYS);
        root.getRowConstraints().addAll(headerRow, contentRow);

        Parent header = buildHeader();
        Parent usersCard = buildUsersCard();
        Parent logsCard = buildLogsCard();

        GridPane.setColumnSpan(header, 2);
        root.add(header, 0, 0);
        root.add(usersCard, 0, 1);
        root.add(logsCard, 1, 1);
    }

    private Parent buildHeader() {
        Label title = new Label("Server Monitor");
        title.getStyleClass().add("header-title");

        Label subtitle = new Label("TCP group chat control panel");
        subtitle.getStyleClass().add("header-subtitle");
        VBox left = new VBox(3, title, subtitle);

        HBox state = new HBox(8, statusDot, statusLabel, serverAddressLabel);
        state.getStyleClass().add("state-chip");
        state.setAlignment(Pos.CENTER_RIGHT);

        GridPane header = new GridPane();
        header.getStyleClass().add("header-card");
        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightCol = new ColumnConstraints();
        header.getColumnConstraints().addAll(leftCol, rightCol);
        header.add(left, 0, 0);
        header.add(state, 1, 0);
        return header;
    }

    private Parent buildUsersCard() {
        Label title = new Label("Connected Users");
        title.getStyleClass().add("card-title");
        onlineCountLabel.getStyleClass().add("online-count");

        userList.setItems(userItems);
        userList.getStyleClass().add("users-list");
        userList.setPlaceholder(new Label("No active clients"));
        userList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(UserBadgeModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setBackground(null);
                    return;
                }

                Label name = new Label(item.getUsername());
                name.getStyleClass().add("user-name");
                Label status = new Label(item.getStatusText());
                status.getStyleClass().add("user-status");

                HBox row = new HBox(8, name, status);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 8, 6, 8));
                row.setMaxWidth(Double.MAX_VALUE);

                setGraphic(row);
                setText(null);
                setBackground(new Background(new BackgroundFill(item.getColor(), new CornerRadii(10), Insets.EMPTY)));
            }
        });

        VBox card = new VBox(10, title, onlineCountLabel, userList);
        card.getStyleClass().add("card-panel");
        VBox.setVgrow(userList, Priority.ALWAYS);
        return card;
    }

    private Parent buildLogsCard() {
        Label title = new Label("Server Log");
        title.getStyleClass().add("card-title");

        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.getStyleClass().add("log-area");

        VBox card = new VBox(10, title, logArea);
        card.getStyleClass().add("card-panel");
        VBox.setVgrow(logArea, Priority.ALWAYS);
        return card;
    }

    private void updateUsers(List<String> users) {
        Set<String> active = new HashSet<>(users);
        userColors.keySet().retainAll(active);

        List<UserBadgeModel> updated = users.stream()
                .map(user -> {
                    Color color = userColors.computeIfAbsent(user, name -> randomPastel());
                    return new UserBadgeModel(user, color, "online");
                })
                .collect(Collectors.toList());

        userItems.setAll(updated);
        onlineCountLabel.setText(updated.size() + " online");
    }

    private static Color randomPastel() {
        double hue = ThreadLocalRandom.current().nextDouble(0, 360);
        return Color.hsb(hue, 0.30, 0.95);
    }
}
