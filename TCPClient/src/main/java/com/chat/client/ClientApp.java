package com.chat.client;

import com.chat.client.config.ClientConfig;
import com.chat.client.ui.ClientController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private ClientController controller;

    @Override
    public void start(Stage stage) {
        ClientConfig config = ClientConfig.fromArgs(getParameters().getRaw());
        controller = new ClientController(config);
        Scene scene = new Scene(controller.getView(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setTitle("TCP Chat Client");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }
}
