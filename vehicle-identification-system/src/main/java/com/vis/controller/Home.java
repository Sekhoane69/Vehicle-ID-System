package com.vis.controller;

import com.vis.dao.*;
import com.vis.util.Effects;
import com.vis.util.LoginSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class Home implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label totalCarsLabel;
    @FXML private Label totalClientsLabel;
    @FXML private Label totalServicesLabel;
    @FXML private Label totalTicketsLabel;

    @FXML private BarChart<String, Number>   CarsByMakeChart;
    @FXML private PieChart                   reportTypePieChart;
    @FXML private LineChart<String, Number>  serviceCostChart;
    @FXML private BarChart<String, Number>   TicketsFineChart;

    @FXML private ProgressBar  CarProgress;
    @FXML private ProgressBar  serviceProgress;
    @FXML private ProgressIndicator overallIndicator;

    @FXML private ScrollPane   scrollPane;
    @FXML private VBox         dashboardRoot;
    @FXML private VBox         ClientsCard;
    @FXML private VBox         servicesCard;
    @FXML private VBox         TicketsCard;
    @FXML private VBox         reportsCard;
    @FXML private VBox         serviceProgressBox;
    @FXML private VBox         serviceCostChartBox;
    @FXML private VBox         TicketsChartBox;
    @FXML private VBox         adminChartsContainer;
    @FXML private VBox         ClientListsContainer;
    @FXML private ListView<String> ClientServicesListView;
    @FXML private ListView<String> ClientTicketsListView;
    @FXML private ListView<String> policeAlertsListView;

    private final CarData     CarData     = new CarData();
    private final ClientData    ClientData    = new ClientData();
    private final GarageData serviceDAO   = new GarageData();
    private final TicketData   TicketData   = new TicketData();
    private final PoliceData reportDAO     = new PoliceData();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String name = LoginSession.getInstance().getCurrentPerson().getpersonname();
        String role = LoginSession.getInstance().getRole();
        
        welcomeLabel.setText("System Dashboard");
        subtitleLabel.setText("Logged in as " + name + " (" + role + ") — " + java.time.LocalDate.now());
        
        Effects.fadeInAndSlide(dashboardRoot, 500);

        overallIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        new Thread(() -> {
            try {
                boolean isClient = "CLIENT".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role);
                int Cars = 0, Clients = 0, services = 0, Tickets = 0;

                if (isClient) {
                    com.vis.model.Client currentClient = ClientData.findByEmail(LoginSession.getInstance().getCurrentPerson().getEmail());
                    if (currentClient != null) {
                        int cid = currentClient.getId();
                        Cars = getCount("SELECT COUNT(*) FROM vehicles WHERE owner_id = " + cid);
                        services = getCount("SELECT COUNT(s.*) FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id WHERE v.owner_id = " + cid);
                        Tickets = getCount("SELECT COUNT(vi.*) FROM violations vi JOIN vehicles v ON vi.vehicle_id = v.vehicle_id WHERE v.owner_id = " + cid);
                        
                        java.util.List<String> serviceStrs = new java.util.ArrayList<>();
                        try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance().getConnection().prepareStatement(
                                "SELECT v.registration_number, s.service_date, s.service_type, s.cost FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id WHERE v.owner_id = ? ORDER BY s.service_date DESC")) {
                            ps.setInt(1, cid);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    serviceStrs.add(rs.getString(1) + " | " + rs.getDate(2) + " | " + rs.getString(3) + " | R" + String.format("%.2f", rs.getDouble(4)));
                                }
                            }
                        }
                        
                        java.util.List<String> TicketStrs = new java.util.ArrayList<>();
                        try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance().getConnection().prepareStatement(
                                "SELECT vi.violation_id, v.registration_number, vi.violation_date, vi.violation_type, vi.fine_amount, vi.status " +
                                "FROM violations vi JOIN vehicles v ON vi.vehicle_id = v.vehicle_id WHERE v.owner_id = ? ORDER BY vi.violation_date DESC")) {
                            ps.setInt(1, cid);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    TicketStrs.add("#" + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getDate(3) + " | " + rs.getString(4) + " | R" + String.format("%.2f", rs.getDouble(5)) + " [" + rs.getString(6) + "]");
                                }
                            }
                        }

                        java.util.List<String> AlertStrs = new java.util.ArrayList<>();
                        try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance().getConnection().prepareStatement(
                                "SELECT query_date, query_text FROM customer_queries WHERE customer_id = ? AND status IN ('POLICE_FEEDBACK', 'POLICE_UPDATE') ORDER BY query_date DESC")) {
                            ps.setInt(1, cid);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    AlertStrs.add("[" + rs.getDate(1) + "] " + rs.getString(2));
                                }
                            }
                        }

                        Platform.runLater(() -> {
                            ClientsCard.setVisible(false);
                            ClientsCard.setManaged(false);
                            adminChartsContainer.setVisible(false);
                            adminChartsContainer.setManaged(false);
                            reportsCard.setVisible(false);
                            reportsCard.setManaged(false);
                            ClientListsContainer.setVisible(true);
                            ClientListsContainer.setManaged(true);
                            
                            ClientServicesListView.setItems(javafx.collections.FXCollections.observableArrayList(serviceStrs));
                            ClientTicketsListView.setItems(javafx.collections.FXCollections.observableArrayList(TicketStrs));
                            policeAlertsListView.setItems(javafx.collections.FXCollections.observableArrayList(AlertStrs));
                        });
                    }
                } else {
                    boolean isPolice = "POLICE".equals(role);
                    
                    Cars   = CarData.countAll("vehicles");
                    Clients  = isPolice ? 0 : ClientData.countAll("customers");
                    services   = isPolice ? 0 : serviceDAO.countAll("service_records");
                    Tickets = TicketData.countAll("violations");

                    Map<String, Integer> byMake      = CarData.countByMake();
                    Map<String, Integer> byReportType = reportDAO.countByType();
                    Map<String, Double>  costByMonth  = isPolice ? null : serviceDAO.costByMonth();
                    Map<String, Double>  finesByType  = TicketData.finesByType();

                    Platform.runLater(() -> {
                        if (isPolice || "CLIENT".equals(role)) {
                            reportsCard.setVisible(isPolice);
                            reportsCard.setManaged(isPolice);
                            serviceProgressBox.setVisible(false);
                            serviceProgressBox.setManaged(false);
                            serviceCostChartBox.setVisible(false);
                            serviceCostChartBox.setManaged(false);
                            
                            if ("INSURANCE".equals(role)) {
                                TicketsCard.setVisible(false);
                                TicketsCard.setManaged(false);
                                reportsCard.setVisible(false);
                                reportsCard.setManaged(false);
                                TicketsChartBox.setVisible(false);
                                TicketsChartBox.setManaged(false);
                            }
                        }
                        
                        buildCarsByMakeChart(byMake);
                        buildReportPieChart(byReportType);
                        if (costByMonth != null) buildServiceCostChart(costByMonth);
                        buildTicketsFineChart(finesByType);
                    });
                }

                final int fCars = Cars;
                final int fClients = Clients;
                final int fServices = services;
                final int fTickets = Tickets;

                Platform.runLater(() -> {
                    totalCarsLabel.setText(String.valueOf(fCars));
                    totalClientsLabel.setText(String.valueOf(fClients));
                    totalServicesLabel.setText(String.valueOf(fServices));
                    totalTicketsLabel.setText(String.valueOf(fTickets));

                    overallIndicator.setProgress(1.0);
                    if (!isClient) {
                        Effects.animateProgress(CarProgress, fCars / 20.0);
                        Effects.animateProgress(serviceProgress, fServices / 30.0);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    overallIndicator.setProgress(0);
                    welcomeLabel.setText("Dashboard load error: " + e.getMessage());
                });
            }
        }).start();
    }

    private int getCount(String sql) throws SQLException {
        try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance().getConnection().prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private void buildCarsByMakeChart(Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cars");
        data.forEach((make, cnt) -> series.getData().add(new XYChart.Data<>(make, cnt)));
        CarsByMakeChart.getData().setAll(series);
        CarsByMakeChart.setAnimated(true);
    }

    private void buildReportPieChart(Map<String, Integer> data) {
        javafx.collections.ObservableList<PieChart.Data> pieData =
            javafx.collections.FXCollections.observableArrayList();
        data.forEach((type, cnt) -> pieData.add(new PieChart.Data(type + " (" + cnt + ")", cnt)));
        reportTypePieChart.setData(pieData);
        reportTypePieChart.setAnimated(true);
    }

    @SuppressWarnings("unchecked")
    private void buildServiceCostChart(Map<String, Double> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Cost ($)");
        data.forEach((month, cost) -> series.getData().add(new XYChart.Data<>(month, cost)));
        serviceCostChart.getData().setAll(series);
        serviceCostChart.setAnimated(true);
    }

    @SuppressWarnings("unchecked")
    private void buildTicketsFineChart(Map<String, Double> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Fines (R)");
        data.forEach((type, amt) -> series.getData().add(new XYChart.Data<>(type, amt)));
        TicketsFineChart.getData().setAll(series);
        TicketsFineChart.setAnimated(true);
    }

    @FXML
    private void handlePayTicket() {
        String selected = ClientTicketsListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (selected.contains("[PAID]")) {
            welcomeLabel.setText("Ticket already paid.");
            return;
        }

        try {
            int id = Integer.parseInt(selected.substring(1, selected.indexOf(" ")));
            if (TicketData.updateStatus(id, "PAID")) {
                welcomeLabel.setText("✅ Ticket #" + id + " paid successfully!");
                initialize(null, null); // refresh dashboard
            }
        } catch (Exception e) {
            welcomeLabel.setText("Payment error: " + e.getMessage());
        }
    }
}
