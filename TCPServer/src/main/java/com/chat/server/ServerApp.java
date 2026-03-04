package com.chat.server;

import com.chat.server.ui.ServerController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerApp extends Application {
    private ServerController controller;

    @Override
    public void start(Stage stage) {
        controller = new ServerController();
        Scene scene = new Scene(controller.getView(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
        stage.setTitle("TCP Chat Server");
        stage.setScene(scene);
        stage.show();
        controller.start();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }
}
