package com.vis.controller;

import com.vis.dao.PersonData;
import com.vis.model.Person;
import com.vis.util.Effects;
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

public class Signup implements Initializable {

    @FXML private VBox registerCard;
    @FXML private TextField personnameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Button registerBtn;
    @FXML private Button backBtn;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final PersonData PersonData = new PersonData();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(registerCard, 700);
        Effects.applyDropShadow(registerBtn, Color.web("#2563eb"), 18, 0, 4);

        roleCombo.getItems().addAll("WORKSHOP", "Client", "POLICE", "INSURANCE");
        roleCombo.setValue("Client");

        loadingIndicator.setVisible(false);
        statusLabel.setVisible(false);
    }
    //method with obj
    @FXML
    private void handleRegister() {
        String personname = personnameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        String role     = roleCombo.getValue();

        if (personname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("Please fill in all fields.", true);
            return;
        }
        if (!password.equals(confirm)) {
            showStatus("Passwords do not match.", true);
            return;
        }
        if (password.length() < 6) {
            showStatus("Password must be at least 6 characters.", true);
            return;
        }

        loadingIndicator.setVisible(true);
        registerBtn.setDisable(true);

        new Thread(() -> {
            try {
                if (PersonData.usernameExists(personname)) {
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        registerBtn.setDisable(false);
                        showStatus("personname already exists.", true);
                    });
                    return;
                }
                if (PersonData.emailExists(email)) {
                    javafx.application.Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        registerBtn.setDisable(false);
                        showStatus("Email already registered.", true);
                    });
                    return;
                }
                Person person = new Person();
                person.setpersonname(personname);
                person.setEmail(email);
                person.setPassword(password);
                person.setRole(role);
                person.setActive(true);
                boolean ok = PersonData.save(person);
                if (ok && "Client".equals(role)) {
                    com.vis.dao.ClientData cDao = new com.vis.dao.ClientData();
                    if (cDao.findByEmail(email) == null) {
                        com.vis.model.Client c = new com.vis.model.Client();
                        c.setName(personname);
                        c.setEmail(email);
                        cDao.save(c);
                    }
                }
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    registerBtn.setDisable(false);
                    if (ok) {
                        showStatus("Account created! Redirecting to login...", false);
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                        pause.setOnFinished(ev -> goToLogin());
                        pause.play();
                    } else {
                        showStatus("Registration failed. Please try again.", true);
                    }
                });
            } catch (SQLException ex) {
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    registerBtn.setDisable(false);
                    showStatus("Error: " + ex.getMessage(), true);
                });
            }
        }).start();
    }

    @FXML
    private void handleBack() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vis/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/vis/css/style.css").toExternalForm());
            Stage stage = (Stage) registerBtn.getScene().getWindow();
            stage.setScene(scene);
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
