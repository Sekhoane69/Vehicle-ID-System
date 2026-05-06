package com.vis.controller;

import com.vis.model.Person;
import com.vis.util.Effects;
import com.vis.util.LoginSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;

import java.util.ResourceBundle;

public class Profile implements Initializable {

    @FXML private VBox profileRoot;
    @FXML private TextField personnameField, emailField;
    @FXML private Label personnameFieldDisplay;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private Label statusLabel, roleLabel, joinDateLabel;


    private Person currentPerson;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(profileRoot, 400);
        currentPerson = LoginSession.getInstance().getCurrentPerson();
        
        if (currentPerson != null) {
            personnameField.setText(currentPerson.getpersonname());
            personnameFieldDisplay.setText(currentPerson.getpersonname());
            emailField.setText(currentPerson.getEmail());
            roleLabel.setText(currentPerson.getRole());
            if (currentPerson.getCreatedAt() != null) {
                joinDateLabel.setText("Member since: " + currentPerson.getCreatedAt().toLocalDate().toString());
            }
        }
    }

    @FXML
    private void handleUpdateProfile() {
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (email.isEmpty()) { showStatus("Email cannot be empty.", true); return; }
        if (!pass.isEmpty() && !pass.equals(confirm)) { showStatus("Passwords do not match.", true); return; }

        new Thread(() -> {
            try {
                String sql = "";
                boolean success = false;
                if (pass.isEmpty()) {
                    sql = "UPDATE Persons SET email = ? WHERE Person_id = ?";
                    try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance().getConnection().prepareStatement(sql)) {
                        ps.setString(1, email);
                        ps.setInt(2, currentPerson.getId());
                        success = ps.executeUpdate() > 0;
                    }
                } else {
                    sql = "UPDATE Persons SET email = ?, password = ? WHERE Person_id = ?";
                    try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance().getConnection().prepareStatement(sql)) {
                        ps.setString(1, email);
                        ps.setString(2, org.mindrot.jbcrypt.BCrypt.hashpw(pass, org.mindrot.jbcrypt.BCrypt.gensalt()));
                        ps.setInt(3, currentPerson.getId());
                        success = ps.executeUpdate() > 0;
                    }
                }

                if (success) {
                    currentPerson.setEmail(email);
                    Platform.runLater(() -> {
                        showStatus("✅ Profile updated successfully!", false);
                        passwordField.clear();
                        confirmPasswordField.clear();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill:#ef4444;" : "-fx-text-fill:#22c55e;");
        statusLabel.setVisible(true);
    }
}
