package com.vis.controller;

import com.vis.dao.PersonData;
import com.vis.model.Person;
import com.vis.util.Effects;
import com.vis.util.LoginSession;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class Login implements Initializable {

    @FXML private VBox loginCard;
    @FXML private TextField personnameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Label statusLabel;
    @FXML private VBox loadingOverlay;

    private final PersonData PersonData = new PersonData();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Fade in the card
        Effects.fadeInAndSlide(loginCard, 700);

        // DropShadow on login button
        Effects.applyDropShadow(loginBtn, Color.web("#2563eb"), 18, 0, 4);

        // Continuous fade on register button
        FadeTransition fade = Effects.createFadeLoop(registerBtn);
        fade.play();

        loadingOverlay.setVisible(false);
        statusLabel.setVisible(false);

        // Enter key triggers login
        passwordField.setOnAction(e -> handleLogin());
    }
//method with login object
    @FXML
    private void handleLogin() {
        String personname = personnameField.getText().trim();
        String password = passwordField.getText();

        if (personname.isEmpty() || password.isEmpty()) {
            showStatus("Please fill in all fields.", true);
            return;
        }

        loadingOverlay.setVisible(true);
        statusLabel.setVisible(false);
        loginBtn.setDisable(true);

        new Thread(() -> {
            try {
                Person person = PersonData.authenticate(personname, password);
                javafx.application.Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    loginBtn.setDisable(false);
                    if (person != null) {
                        LoginSession.getInstance().setCurrentPerson(person);
                        navigateToMain();
                    } else {
                        showStatus("Invalid personname or password.", true);
                        Effects.popIn(statusLabel);
                    }
                });
            } catch (SQLException ex) {
                javafx.application.Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    loginBtn.setDisable(false);
                    showStatus("Database error: " + ex.getMessage(), true);
                });
            }
        }).start();
    }
//method with registration object()
    @FXML private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vis/fxml/signup.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/vis/css/style.css").toExternalForm());
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            showStatus("Could not open registration: " + e.getMessage(), true);
        }
    }

    private void navigateToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vis/fxml/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(getClass().getResource("/com/vis/css/style.css").toExternalForm());
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setTitle("Car Identification System — " + LoginSession.getInstance().getCurrentPerson().getpersonname());
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(true);
        } catch (Exception e) {
            showStatus("Navigation error: " + e.getMessage(), true);
        }
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;");
        statusLabel.setVisible(true);
    }
}
