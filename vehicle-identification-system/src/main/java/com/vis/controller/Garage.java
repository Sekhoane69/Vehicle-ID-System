package com.vis.controller;

import com.vis.dao.GarageData;
import com.vis.dao.BookingData;
import com.vis.dao.CarData;

import com.vis.model.GarageInfo;
import com.vis.model.BookingInfo;
import com.vis.model.Car;
import com.vis.util.Effects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Garage implements Initializable {

    @FXML private VBox workshopRoot;
    @FXML private TextField searchField;
    @FXML private ListView<String> CarListView;
    @FXML private Label CarDetailLabel;
    
    @FXML private VBox addRecordSection, searchSection, registrySection;
    @FXML private javafx.scene.layout.HBox statsSection, chartsSection;



    // Service form
    @FXML private ComboBox<String> CarCombo;
    @FXML private DatePicker serviceDatePicker;
    @FXML private ComboBox<String> serviceTypeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField costField, technicianField;
    @FXML private Button saveServiceBtn;

    // Charts
    @FXML private PieChart serviceTypePieChart;
    @FXML private BarChart<String, Number> costBarChart;

    // Table
    @FXML private TableView<GarageInfo> serviceTable;
    @FXML private TableColumn<GarageInfo, Integer> idCol;
    @FXML private TableColumn<GarageInfo, String> regCol, typeCol, techCol, statusCol;
    @FXML private TableColumn<GarageInfo, LocalDate> dateCol;
    @FXML private TableColumn<GarageInfo, Double> costCol;

    // Stats
    @FXML private Label inWorkshopLabel, servicedTotalLabel, pendingServicesLabel;

    @FXML private Label statusLabel;
    @FXML private ProgressBar loadProgress;

    private final CarData CarData         = new CarData();

    private final GarageData serviceDAO   = new GarageData();
    private final BookingData requestDAO = new BookingData();

    private List<Car> allCars;
    private List<GarageInfo> allServices;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(workshopRoot, 400);
        Effects.applyDropShadow(saveServiceBtn, javafx.scene.paint.Color.web("#16a34a"), 12, 0, 3);

        serviceTypeCombo.setItems(FXCollections.observableArrayList(
            "Oil Change", "Brake Service", "Tire Rotation", "Engine Tune-Up",
            "AC Repair", "Transmission", "Battery Replace", "Suspension",
            "Alignment", "Windshield", "Other"));
        
        statusCombo.setItems(FXCollections.observableArrayList("PENDING", "IN_PROGRESS", "COMPLETED"));
        statusCombo.setValue("PENDING");

        serviceDatePicker.setValue(LocalDate.now());
        loadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                allCars = CarData.findAll();
                allServices = serviceDAO.findAll();
                int inWorkshop = serviceDAO.countActiveWorkshop();
                int totalServiced = serviceDAO.countCompletedService();
                long pending = allServices.stream().filter(s -> "PENDING".equals(s.getStatus())).count();

                Platform.runLater(() -> {
                    loadProgress.setProgress(1.0);
                    inWorkshopLabel.setText(String.valueOf(inWorkshop));
                    servicedTotalLabel.setText(String.valueOf(totalServiced));
                    pendingServicesLabel.setText(String.valueOf(pending));

                    // Populate Car list
                    CarListView.setItems(FXCollections.observableArrayList(
                        allCars.stream().map(v -> v.getRegistrationNumber() + " — " + v.getMake() + " " + v.getModel()).collect(Collectors.toList())));

                    // Populate combos - only show Cars with pending service requests
                    try {
                        List<BookingInfo> pendingRequests = requestDAO.findPending();
                        CarCombo.setItems(FXCollections.observableArrayList(
                            pendingRequests.stream().map(r -> r.getVehicleId() + ": " + r.getRegistrationNumber() + " (Reported by " + r.getOwnerName() + ")").collect(Collectors.toList())));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    // Build charts & table
                    buildServiceTypeChart();
                    buildCostChart();
                    populateTable();
                    
                    // Apply initial mode if set via MainApp
                    applyNavigationMode();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Load error: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        new Thread(() -> {
            try {
                List<Car> results = CarData.search(kw);
                
                // If in workshop-only search mode, filter results
                if ("SEARCH".equals(currentMode)) {
                    List<GarageInfo> active = serviceDAO.findAll().stream()
                        .filter(s -> "PENDING".equals(s.getStatus()) || "IN_PROGRESS".equals(s.getStatus()))
                        .collect(Collectors.toList());
                    
                    results = results.stream()
                        .filter(v -> active.stream().anyMatch(s -> s.getVehicleId() == v.getId()))
                        .collect(Collectors.toList());
                }

                List<Car> finalResults = results;
                Platform.runLater(() -> {
                    CarListView.setItems(FXCollections.observableArrayList(
                        finalResults.stream().map(v -> v.getRegistrationNumber() + " — " + v.getMake() + " " + v.getModel() + " | Owner: " + v.getOwnerName()).collect(Collectors.toList())));
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Search error: " + e.getMessage(), true));
            }
        }).start();
    }

    private String currentMode = "ALL";

    public void setNavigationMode(String mode) {
        this.currentMode = mode;
        if (addRecordSection != null) {
            applyNavigationMode();
        }
    }

    private void applyNavigationMode() {
        if (currentMode == null || "ALL".equals(currentMode)) {
            // StartApp Workshop landing: Show only stats/charts, hide the specific tool sections
            statsSection.setVisible(true);      statsSection.setManaged(true);
            chartsSection.setVisible(true);     chartsSection.setManaged(true);
            addRecordSection.setVisible(false); addRecordSection.setManaged(false);
            searchSection.setVisible(false);    searchSection.setManaged(false);
            registrySection.setVisible(false);  registrySection.setManaged(false);
        } else {
            // Specific tool mode: Hide stats/charts, show only the selected tool
            statsSection.setVisible(false);     statsSection.setManaged(false);
            chartsSection.setVisible(false);    chartsSection.setManaged(false);
            
            if ("RECORD".equals(currentMode)) {
                addRecordSection.setVisible(true); addRecordSection.setManaged(true);
                searchSection.setVisible(false);   searchSection.setManaged(false);
                registrySection.setVisible(false); registrySection.setManaged(false);
            } else if ("SEARCH".equals(currentMode)) {
                addRecordSection.setVisible(false); addRecordSection.setManaged(false);
                searchSection.setVisible(true);    searchSection.setManaged(true);
                registrySection.setVisible(false); registrySection.setManaged(false);
            } else if ("REGISTRY".equals(currentMode)) {
                addRecordSection.setVisible(false); addRecordSection.setManaged(false);
                searchSection.setVisible(false);   searchSection.setManaged(false);
                registrySection.setVisible(true);  registrySection.setManaged(true);
            }
        }
    }



    @FXML
    private void handleSaveService() {
        try {
            GarageInfo s = new GarageInfo();
            String CarSel = CarCombo.getValue();
            if (CarSel == null) { showStatus("Select a Car.", true); return; }
            s.setVehicleId(Integer.parseInt(CarSel.split(":")[0].trim()));
            s.setServiceDate(serviceDatePicker.getValue());
            s.setServiceType(serviceTypeCombo.getValue());
            s.setDescription(descriptionArea.getText().trim());
            s.setCost(Double.parseDouble(costField.getText().trim()));
            s.setTechnician(technicianField.getText().trim());
            s.setStatus(statusCombo.getValue());
            if (serviceDAO.save(s)) {
                // Automatically complete the service request
                requestDAO.completeRequest(s.getVehicleId());
                
                showStatus("Service record saved & Report submitted to owner!", false);
                clearServiceForm();
                loadData();
            }
        } catch (NumberFormatException e) {
            showStatus("Cost must be a valid number.", true);
        } catch (SQLException e) {
            showStatus("Save error: " + e.getMessage(), true);
        }
    }

    private void buildServiceTypeChart() {
        try {
            Map<String, Long> data = serviceDAO.countByServiceType();
            javafx.collections.ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            data.forEach((type, cnt) -> pieData.add(new PieChart.Data(type + " (" + cnt + ")", cnt)));
            serviceTypePieChart.setData(pieData);
            serviceTypePieChart.setAnimated(true);
        } catch (SQLException e) { showStatus("Chart error: " + e.getMessage(), true); }
    }

    @SuppressWarnings("unchecked")
    private void buildCostChart() {
        try {
            Map<String, Double> data = serviceDAO.costByMonth();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Cost (R)");
            data.forEach((month, cost) -> series.getData().add(new XYChart.Data<>(month, cost)));
            costBarChart.getData().setAll(series);
            costBarChart.setAnimated(true);
        } catch (SQLException e) { showStatus("Chart error: " + e.getMessage(), true); }
    }

    private void populateTable() {
        if (allServices == null) return;
        
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        regCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("registrationNumber"));
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("serviceDate"));
        typeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("serviceType"));
        techCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("technician"));
        costCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("cost"));
        statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        
        serviceTable.setItems(FXCollections.observableArrayList(allServices));
    }


    private void clearServiceForm()  { CarCombo.setValue(null); serviceTypeCombo.setValue(null); statusCombo.setValue("PENDING"); descriptionArea.clear(); costField.clear(); technicianField.clear(); }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill:#ef4444;" : "-fx-text-fill:#22c55e;");
        statusLabel.setVisible(true);
    }
}
