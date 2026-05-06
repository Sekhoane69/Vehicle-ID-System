package com.vis.controller;

import com.vis.util.Effects;
import com.vis.util.LoginSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainApp implements Initializable {

    @FXML private BorderPane mainPane;
    @FXML private Label PersonLabel;
    @FXML private Label roleLabel;
    @FXML private StackPane contentArea;
    @FXML private MenuBar menuBar;
    @FXML private VBox sidebar;
    @FXML private VBox workshopSubNav;
    @FXML private VBox insuranceSubNav;
    @FXML private VBox policeSubNav;
    
    @FXML private VBox AlertBanner;
    @FXML private Label AlertText;
    @FXML private Label AlertIcon;

    private final com.vis.dao.AlertData AlertData = new com.vis.dao.AlertData();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String personname = LoginSession.getInstance().getCurrentPerson().getpersonname();
        String role     = LoginSession.getInstance().getRole();
        PersonLabel.setText(personname);
        roleLabel.setText(role);

        // Configure sidebar visibility based on role
        configureSidebarByRole(role);

        // Load appropriate view by default
        if ("WORKSHOP".equals(role)) {
            showWorkshop();
        } else if ("INSURANCE".equals(role)) {
            showInsurance();
        } else {
            loadView("dashboard");
        }
        checkForBroadcasts();
        Effects.fadeIn(mainPane, 400);
    }

    private void configureSidebarByRole(String role) {
        sidebar.getChildren().removeIf(node -> {
            boolean isAdminItem = (node instanceof javafx.scene.control.Label && "SYSTEM".equals(((javafx.scene.control.Label)node).getText())) ||
                                   (node instanceof javafx.scene.control.Separator) ||
                                   (node instanceof javafx.scene.control.Button && "Admin Panel".equals(((javafx.scene.control.Button)node).getText()));
            
            if (isAdminItem) {
                return !"ADMIN".equals(role); // Only ADMIN sees Admin items
            }
            
            // Hide workshop sub-nav for non-workshop/non-admin roles
            if (node == workshopSubNav) {
                return !"WORKSHOP".equals(role) && !"ADMIN".equals(role);
            }
            
            // Hide insurance sub-nav for non-insurance/non-admin roles
            if (node == insuranceSubNav) {
                return !"INSURANCE".equals(role) && !"ADMIN".equals(role);
            }
            
            // Hide police sub-nav for non-police/non-admin roles
            if (node == policeSubNav) {
                return !"POLICE".equals(role) && !"ADMIN".equals(role);
            }

            if (node instanceof javafx.scene.control.Button) {
                String text = ((javafx.scene.control.Button)node).getText();
                if (text == null) return false;
                
                if (text.equals("My Profile")) {
                    return false; // Everyone sees Profile
                }

                String roleUpper = role.toUpperCase();
                if (roleUpper.equals("ADMIN")) {
                    return false; // ADMIN sees all
                } else if (roleUpper.equals("POLICE")) {
                    return !text.equals("Police") && !text.equals("Car Search") && 
                           !text.equals("File Report") && !text.equals("Issue Ticket");
                } else if (roleUpper.equals("INSURANCE")) {
                    return !text.equals("Insurance") && !text.equals("Insurance Policy") && !text.equals("Policies Registry");
                } else if (roleUpper.equals("WORKSHOP")) {
                    return !text.equals("Workshop") && !text.equals("Service Record") && 
                           !text.equals("Workshop Search") && !text.equals("Service Registry");
                } else if (roleUpper.equals("CLIENT") || roleUpper.equals("CUSTOMER")) {
                    // Clients ONLY see Dashboard, Client, and Profile
                    return text.equals("Police") || text.equals("Insurance") || text.equals("Workshop") || 
                           text.equals("Admin Panel") || text.contains("Tools");
                }
            }
            return false;
        });

        if (menuBar.getMenus().size() > 1) {
            javafx.scene.control.Menu modulesMenu = menuBar.getMenus().get(1);
            modulesMenu.getItems().removeIf(item -> {
                String text = item.getText();
                if (text == null) return false;

                if (text.equals("My Profile")) {
                    return false; // Everyone sees Profile
                }

                if ("Admin".equals(text)) {
                    return !"ADMIN".equals(role); // Only ADMIN sees Admin module in menu
                }
                
                String roleUpper = role.toUpperCase();
                if (roleUpper.equals("ADMIN")) {
                    return false;
                } else if (roleUpper.equals("POLICE")) {
                    return !text.equals("Police") && !text.equals("Police Tools") && 
                           !text.equals("Car Search") && !text.equals("File Report") && !text.equals("Issue Ticket");
                } else if (roleUpper.equals("INSURANCE")) {
                    return !text.equals("Insurance") && !text.equals("Insurance Tools") && 
                           !text.equals("Insurance Policy") && !text.equals("Policies Registry");
                } else if (roleUpper.equals("WORKSHOP")) {
                    return !text.equals("Workshop") && !text.equals("Workshop Tools") && 
                           !text.equals("Service Record") && !text.equals("Workshop Search") && 
                           !text.equals("Service Registry");
                } else if (roleUpper.equals("CLIENT") || roleUpper.equals("CUSTOMER")) {
                    return text.equals("Police") || text.equals("Insurance") || text.equals("Workshop") || 
                           text.equals("Admin") || text.contains("Tools");
                }
                return false;
            });
        }
    }

    @FXML private void showDashboard()  { loadView("dashboard"); }
    @FXML private void showWorkshop()   { loadView("workshop", "ALL"); }
    @FXML private void showGarageInfo()   { loadView("workshop", "RECORD"); }
    @FXML private void showWorkshopSearch()  { loadView("workshop", "SEARCH"); }
    @FXML private void showServiceRegistry() { loadView("workshop", "REGISTRY"); }
    
    @FXML private void showClient()   { loadView("client"); }
    @FXML private void showPolice()     { loadView("police", "All"); }
    @FXML private void showPoliceSearch()    { loadView("police", "Search"); }
    @FXML private void showPoliceInfo()    { loadView("police", "Report"); }
    @FXML private void showPoliceTicket() { loadView("police", "Ticket"); }
    @FXML private void showInsurance()  { loadView("insurance", "ALL"); }
    @FXML private void showInsuranceForm()     { loadView("insurance", "FORM"); }
    @FXML private void showInsuranceRegistry() { loadView("insurance", "REGISTRY"); }
    
    @FXML private void showAdmin()      { loadView("admin"); }
    @FXML private void showProfile()    { loadView("profile"); }

    private void loadView(String name) {
        loadView(name, null);
    }

    private void loadView(String name, String mode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vis/fxml/" + name + ".fxml"));
            Node view = loader.load();
            
            // Pass navigation mode if it's workshop or insurance
            if (mode != null) {
                if ("workshop".equals(name)) {
                    Garage wc = loader.getController();
                    wc.setNavigationMode(mode);
                } else if ("insurance".equals(name)) {
                    Insurance ic = loader.getController();
                    ic.setNavigationMode(mode);
                } else if ("police".equals(name)) {
                    Police pc = loader.getController();
                    pc.setNavigationMode(mode);
                }
            }

            contentArea.getChildren().setAll(view);
            Effects.fadeIn(view, 350);
        } catch (Exception e) {
            // Print full stack trace to console for debugging
            e.printStackTrace();
            // Find root cause
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            String rootMsg = cause.getClass().getSimpleName() + ": " + cause.getMessage();
            Label err = new Label("Could not load view: " + name + "\nCause: " + rootMsg);
            err.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
            err.setWrapText(true);
            contentArea.getChildren().setAll(err);
        }
    }

    @FXML
    private void handleLogout() {
        LoginSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vis/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/vis/css/style.css").toExternalForm());
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
    }

    private void checkForBroadcasts() {
        new Thread(() -> {
            try {
                java.util.List<com.vis.model.Alert> list = AlertData.findLatestBroadcasts(1);
                if (!list.isEmpty()) {
                    com.vis.model.Alert n = list.get(0);
                    Platform.runLater(() -> showAlert(n));
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(com.vis.model.Alert n) {
        AlertText.setText(n.getMessage());
        String icon = "📢";
        String color = "#3b82f6"; // Default Blue
        if ("WARNING".equals(n.getType())) { icon = "⚠️"; color = "#f59e0b"; }
        else if ("ALARM".equals(n.getType())) { icon = "🚨"; color = "#ef4444"; }
        
        AlertIcon.setText(icon);
        AlertBanner.setStyle(AlertBanner.getStyle() + " -fx-border-color: " + color + ";");
        
        AlertBanner.setManaged(true);
        AlertBanner.setVisible(true);
        Effects.fadeInAndSlide(AlertBanner, 500);
    }

    @FXML
    private void hideAlert() {
        Effects.fadeOut(AlertBanner, 300);
        AlertBanner.setManaged(false);
    }
}
