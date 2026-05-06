package com.vis;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class StartApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vis/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/com/vis/css/style.css").toExternalForm());

        primaryStage.setTitle("Car Identification System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
