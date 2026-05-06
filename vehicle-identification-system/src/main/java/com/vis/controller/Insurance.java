package com.vis.controller;

import com.vis.dao.InsuranceData;
import com.vis.dao.CarData;
import com.vis.model.InsuranceInfo;
import com.vis.model.Car;
import com.vis.util.Effects;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Insurance implements Initializable {

    @FXML private VBox insuranceRoot;
    @FXML private ComboBox<String> CarCombo, providerCombo;
    @FXML private TextField policyNumberField, premiumField;
    @FXML private DatePicker startDatePicker, expiryDatePicker;
    @FXML private ComboBox<String> coverageTypeCombo, statusCombo;
    @FXML private Button saveInsuranceBtn;

    @FXML private PieChart providerPieChart;
    @FXML private PieChart statusPieChart;
    @FXML private Label totalInsuredClientsLabel, totalInsuredCarsLabel;
    
    @FXML private VBox formSection, registrySection;
    @FXML private javafx.scene.layout.HBox statsSection, chartsSection;

    @FXML private TableView<InsuranceInfo> insuranceTable;
    @FXML private TableColumn<InsuranceInfo, Integer> idCol;
    @FXML private TableColumn<InsuranceInfo, String> regCol, providerCol, policyCol, coverageCol, statusCol;
    @FXML private TableColumn<InsuranceInfo, LocalDate> startCol, expiryCol;
    @FXML private TableColumn<InsuranceInfo, Double> premiumCol;

    @FXML private ProgressBar loadProgress;
    @FXML private Label statusLabel;

    private final CarData CarData     = new CarData();
    private final InsuranceData InsuranceData = new InsuranceData();
    private List<InsuranceInfo> allRecords;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(insuranceRoot, 400);
        Effects.applyDropShadow(saveInsuranceBtn, javafx.scene.paint.Color.web("#7c3aed"), 12, 0, 3);

        coverageTypeCombo.setItems(FXCollections.observableArrayList("COMPREHENSIVE","THIRD PARTY","COLLISION","FIRE & THEFT"));
        statusCombo.setItems(FXCollections.observableArrayList("ACTIVE","EXPIRED","CANCELLED"));
        providerCombo.setItems(FXCollections.observableArrayList("LNIG", "Metropolitan", "Alliance", "Standard Insurance", "Liberty"));
        startDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(1));
        loadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<Car> Cars = CarData.findAll();
                allRecords = InsuranceData.findAll();
                Map<String, Integer> byProvider = InsuranceData.countByProvider();
                Map<String, Integer> byStatus   = InsuranceData.countByStatus();
                int insuredCars = InsuranceData.countInsuredCars();
                int insuredClients = InsuranceData.countInsuredClients();
                
                Platform.runLater(() -> {
                    loadProgress.setProgress(1.0);
                    totalInsuredCarsLabel.setText(String.valueOf(insuredCars));
                    totalInsuredClientsLabel.setText(String.valueOf(insuredClients));
                    CarCombo.setItems(FXCollections.observableArrayList(
                        Cars.stream().map(v -> v.getId() + ": " + v.getRegistrationNumber()).collect(Collectors.toList())));
                    buildProviderChart(byProvider);
                    buildStatusChart(byStatus);
                    populateTable();
                    applyNavigationMode();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Load error: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void handleSaveInsurance() {
        try {
            InsuranceInfo r = new InsuranceInfo();
            String sel = CarCombo.getValue();
            if (sel == null) { showStatus("Select a Car.", true); return; }
            r.setVehicleId(Integer.parseInt(sel.split(":")[0].trim()));
            r.setProvider(providerCombo.getValue());
            if (r.getProvider() == null) { showStatus("Select an insurance company.", true); return; }
            r.setPolicyNumber(policyNumberField.getText().trim());
            r.setStartDate(startDatePicker.getValue());
            r.setExpiryDate(expiryDatePicker.getValue());
            r.setPremiumAmount(Double.parseDouble(premiumField.getText().trim()));
            r.setCoverageType(coverageTypeCombo.getValue());
            r.setStatus(statusCombo.getValue() != null ? statusCombo.getValue() : "ACTIVE");
            if (InsuranceData.save(r)) { showStatus("Insurance record saved!", false); clearForm(); loadData(); }
        } catch (NumberFormatException e) { showStatus("Premium must be a valid number.", true);
        } catch (SQLException e) { showStatus("Save error: " + e.getMessage(), true); }
    }

    private void buildProviderChart(Map<String, Integer> data) {
        javafx.collections.ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        data.forEach((p, c) -> pie.add(new PieChart.Data(p + " (" + c + ")", c)));
        providerPieChart.setData(pie);
        providerPieChart.setAnimated(true);
    }

    private void buildStatusChart(Map<String, Integer> data) {
        javafx.collections.ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        data.forEach((s, c) -> pie.add(new PieChart.Data(s + " (" + c + ")", c)));
        statusPieChart.setData(pie);
        statusPieChart.setAnimated(true);
    }

    private void populateTable() {
        if (allRecords == null) return;
        
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        regCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("registrationNumber"));
        providerCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("provider"));
        policyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("policyNumber"));
        startCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("startDate"));
        expiryCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("expiryDate"));
        premiumCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("premiumAmount"));
        coverageCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("coverageType"));
        statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        
        insuranceTable.setItems(FXCollections.observableArrayList(allRecords));
    }

    private String currentMode = "ALL";

    public void setNavigationMode(String mode) {
        this.currentMode = mode;
        if (formSection != null) {
            applyNavigationMode();
        }
    }

    private void applyNavigationMode() {
        if (currentMode == null || "ALL".equals(currentMode)) {
            statsSection.setVisible(true);   statsSection.setManaged(true);
            chartsSection.setVisible(true);  chartsSection.setManaged(true);
            formSection.setVisible(false);   formSection.setManaged(false);
            registrySection.setVisible(false); registrySection.setManaged(false);
        } else if ("FORM".equals(currentMode)) {
            statsSection.setVisible(false);  statsSection.setManaged(false);
            chartsSection.setVisible(false); chartsSection.setManaged(false);
            formSection.setVisible(true);    formSection.setManaged(true);
            registrySection.setVisible(false); registrySection.setManaged(false);
        } else if ("REGISTRY".equals(currentMode)) {
            statsSection.setVisible(false);  statsSection.setManaged(false);
            chartsSection.setVisible(false); chartsSection.setManaged(false);
            formSection.setVisible(false);   formSection.setManaged(false);
            registrySection.setVisible(true);  registrySection.setManaged(true);
        }
    }

    private void clearForm() { 
        providerCombo.setValue(null); 
        policyNumberField.clear(); 
        premiumField.clear(); 
        startDatePicker.setValue(LocalDate.now()); 
        expiryDatePicker.setValue(LocalDate.now().plusYears(1)); 
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill:#ef4444;" : "-fx-text-fill:#22c55e;");
        statusLabel.setVisible(true);
    }
}
