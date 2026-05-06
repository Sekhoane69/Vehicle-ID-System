package com.vis.controller;

import com.vis.dao.PoliceData;
import com.vis.dao.CarData;
import com.vis.dao.TicketData;
import com.vis.model.PoliceInfo;
import com.vis.model.Car;
import com.vis.model.Ticket;
import com.vis.util.Effects;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class Police implements Initializable {

    @FXML private VBox policeRoot;
    @FXML private ProgressBar loadProgress;
    @FXML private Label statusLabel;

    @FXML private VBox statsSection, searchSection, reportSection, TicketSection;
    @FXML private Label totalReportsLabel, activeTicketsLabel, totalFinesLabel;

    // ---- Report Form ----
    @FXML private ComboBox<String> CarCombo;
    @FXML private DatePicker reportDatePicker;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private TextArea reportDescArea;
    @FXML private TextField officerNameField, badgeField;
    @FXML private ComboBox<String> reportStatusCombo;
    @FXML private Button saveReportBtn;
    @FXML private CheckBox notifyOwnerCheckbox;

    // ---- Reports Table ----
    @FXML private TableView<PoliceInfo> reportsTable;
    @FXML private TableColumn<PoliceInfo, String> rpIdCol, rpRegCol, rpTypeCol,
                                                     rpDateCol, rpOfficerCol, rpBadgeCol,
                                                     rpStatusCol, rpDescCol;

    // ---- Ticket / Ticket Form ----
    @FXML private ComboBox<String> vCarCombo;
    @FXML private ComboBox<String> TicketTypeCombo;
    @FXML private VBox customTypeBox;
    @FXML private TextField TicketTypeField;
    @FXML private TextArea TicketDescArea;
    @FXML private DatePicker TicketDatePicker;
    @FXML private DatePicker paymentDueDatePicker;
    @FXML private TextField fineField, locationField, vOfficerField;
    @FXML private ComboBox<String> TicketStatusCombo;
    @FXML private Button saveTicketBtn;

    // ---- Tickets Table ----
    @FXML private TableView<Ticket> TicketsTable;
    @FXML private TableColumn<Ticket, String> vIdCol, vRegCol, vTypeCol, vDateCol,
                                                  vDueCol, vFineCol, vStatusCol,
                                                  vLocCol, vOfficerCol;

    // ---- Charts ----
    @FXML private PieChart reportTypePieChart;
    @FXML private BarChart<String, Number> finesBarChart;
    @FXML private PieChart TicketStatusPie;

    // ---- Car Search ----
    @FXML private TextField searchRegField;
    @FXML private VBox searchResultBox;
    @FXML private Label resReg, resMakeModel, resYearColor, resStatus, resOwnerName, resOwnerPhone, resOwnerEmail;
    @FXML private ComboBox<String> updateStatusCombo;
    @FXML private ListView<String> resTicketsList, resReportsList;
    @FXML private VBox notFoundBox;

    private int lastSearchedCarId = -1;

    private final CarData      CarData   = new CarData();
    private final PoliceData reportDAO    = new PoliceData();
    private final TicketData    TicketData = new TicketData();
    private final com.vis.dao.ClientData ClientData = new com.vis.dao.ClientData();

    private ObservableList<PoliceInfo> reportList    = FXCollections.observableArrayList();
    private ObservableList<Ticket>    TicketList = FXCollections.observableArrayList();

    // Common Ticket types for the ticket form
    private static final String[] Ticket_TYPES = {
        "Speeding", "Cracked Windshield", "No Seatbelt", "Running Red Light",
        "Illegal Parking", "No Valid Licence Disc", "Defective Lights",
        "Reckless Driving", "Unroadworthy Car", "No Insurance",
        "Using Cell Phone While Driving", "Drunk Driving", "Other"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(policeRoot, 400);
        loadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        // Populate combos
        reportTypeCombo.setItems(FXCollections.observableArrayList("Accident", "Theft", "Recovered", "Other"));
        reportStatusCombo.setItems(FXCollections.observableArrayList("OPEN", "CLOSED", "PENDING"));
        TicketStatusCombo.setItems(FXCollections.observableArrayList("UNPAID", "PAID"));
        TicketTypeCombo.setItems(FXCollections.observableArrayList(Ticket_TYPES));
        updateStatusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "STOLEN", "RECOVERED", "IMPOUNDED", "SCRAPPED"));

        reportDatePicker.setValue(LocalDate.now());
        TicketDatePicker.setValue(LocalDate.now());
        paymentDueDatePicker.setValue(LocalDate.now().plusDays(30)); // 30-day default

        // Show/hide custom field when "Other" selected
        TicketTypeCombo.valueProperty().addListener((obs, old, val) -> {
            boolean isOther = "Other".equals(val);
            customTypeBox.setManaged(isOther);
            customTypeBox.setVisible(isOther);
        });

        // Wire tables
        wireReportTable();
        wireTicketTable();

        loadData();
        setNavigationMode("All"); // Default
    }

    // ===================== WIRE TABLE COLUMNS =====================

    private void wireReportTable() {
        rpIdCol.setCellValueFactory(c      -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        rpRegCol.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().getRegistrationNumber()));
        rpTypeCol.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getReportType()));
        rpDateCol.setCellValueFactory(c    -> new SimpleStringProperty(String.valueOf(c.getValue().getReportDate())));
        rpOfficerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOfficerName()));
        rpBadgeCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getBadgeNumber()));
        rpStatusCol.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getStatus()));
        rpDescCol.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getDescription()));
        reportsTable.setItems(reportList);
    }

    private void wireTicketTable() {
        vIdCol.setCellValueFactory(c      -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        vRegCol.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().getRegistrationNumber()));
        vTypeCol.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getTicketType()));
        vDateCol.setCellValueFactory(c    -> new SimpleStringProperty(String.valueOf(c.getValue().getTicketDate())));
        vDueCol.setCellValueFactory(c     -> new SimpleStringProperty(
                c.getValue().getPaymentDueDate() != null ? c.getValue().getPaymentDueDate().toString() : "—"));
        vFineCol.setCellValueFactory(c    -> new SimpleStringProperty("R " + String.format("%.2f", c.getValue().getFineAmount())));
        vStatusCol.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getStatus()));
        vLocCol.setCellValueFactory(c     -> new SimpleStringProperty(c.getValue().getLocation()));
        vOfficerCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOfficerName()));
        TicketsTable.setItems(TicketList);

        // Color-code rows by status
        TicketsTable.setRowFactory(tv -> new TableRow<Ticket>() {
            @Override protected void updateItem(Ticket item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                if ("UNPAID".equals(item.getStatus())) {
                    setStyle("-fx-background-color: #fff1f1;");
                } else {
                    setStyle("-fx-background-color: #f0fdf4;");
                }
            }
        });
    }

    // ===================== LOAD DATA =====================

    private void loadData() {
        new Thread(() -> {
            try {
                List<Car>      Cars   = CarData.findAll();
                List<PoliceInfo> reports    = reportDAO.findAll();
                List<Ticket>    Tickets = TicketData.findAll();
                Map<String, Integer> byType    = reportDAO.countByType();
                Map<String, Double>  fines     = TicketData.finesByType();
                Map<String, Integer> vioStatus = TicketData.countByStatus();

                Platform.runLater(() -> {
                    loadProgress.setProgress(1.0);
                    List<String> vItems = Cars.stream()
                        .map(v -> v.getId() + ": " + v.getRegistrationNumber() + " (" + v.getMake() + " " + v.getModel() + ")")
                        .collect(Collectors.toList());
                    CarCombo.setItems(FXCollections.observableArrayList(vItems));
                    vCarCombo.setItems(FXCollections.observableArrayList(vItems));

                    reportList.setAll(reports);
                    TicketList.setAll(Tickets);

                    // Update Stat Cards
                    totalReportsLabel.setText(String.valueOf(reports.size()));
                    long unpaidCount = Tickets.stream().filter(v -> "UNPAID".equals(v.getStatus())).count();
                    activeTicketsLabel.setText(String.valueOf(unpaidCount));
                    double totalFine = Tickets.stream().mapToDouble(Ticket::getFineAmount).sum();
                    totalFinesLabel.setText("R " + String.format("%.2f", totalFine));

                    buildReportTypeChart(byType);
                    buildFinesChart(fines);
                    buildVioStatusChart(vioStatus);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Load error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== FILE REPORT =====================

    @FXML
    private void handleSaveReport() {
        try {
            String sel = CarCombo.getValue();
            if (sel == null) { showStatus("Select a Car.", true); return; }
            if (reportTypeCombo.getValue() == null) { showStatus("Select a report type.", true); return; }
            if (officerNameField.getText().trim().isEmpty()) { showStatus("Officer name is required.", true); return; }

            PoliceInfo r = new PoliceInfo();
            r.setVehicleId(Integer.parseInt(sel.split(":")[0].trim()));
            r.setReportDate(reportDatePicker.getValue());
            r.setReportType(reportTypeCombo.getValue());
            r.setDescription(reportDescArea.getText().trim());
            r.setOfficerName(officerNameField.getText().trim());
            r.setBadgeNumber(badgeField.getText().trim());
            r.setStatus(reportStatusCombo.getValue() != null ? reportStatusCombo.getValue() : "OPEN");

            if (reportDAO.save(r)) {
                if (notifyOwnerCheckbox.isSelected()) {
                    try {
                        Car v = CarData.findById(r.getVehicleId());
                        if (v != null && v.getOwnerId() > 0) {
                            String msg = "POLICE_FEEDBACK: Your Car [" + v.getRegistrationNumber() + "] status updated to " + r.getReportType() + ". Details: " + r.getDescription();
                            ClientData.addAlert(v.getOwnerId(), msg);
                        }
                    } catch (Exception ex) {
                        System.err.println("Alert failed: " + ex.getMessage());
                    }
                }
                showStatus("✅ Report filed successfully!", false);
                clearReportForm();
                loadData();
            }
        } catch (SQLException e) { showStatus("Save error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleCloseReport() {
        PoliceInfo selected = reportsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Please select a report from the table to close.", true); return; }
        
        try {
            selected.setStatus("CLOSED");
            if (reportDAO.update(selected)) {
                // If it was a recovery report, notify owner and update Car status
                if ("RECOVERED".equalsIgnoreCase(selected.getReportType()) || "FOUND".equalsIgnoreCase(selected.getReportType())) {
                    Car v = CarData.findById(selected.getVehicleId());
                    if (v != null && v.getOwnerId() > 0) {
                         ClientData.addAlert(v.getOwnerId(), "POLICE_UPDATE: Your Car [" + v.getRegistrationNumber() + "] report #" + selected.getId() + " has been CLOSED. Status: FOUND/RECOVERED.");
                         CarData.updateStatus(v.getId(), "ACTIVE");
                    }
                }
                showStatus("✅ Report #" + selected.getId() + " marked as CLOSED.", false);
                loadData();
            }
        } catch (SQLException e) {
            showStatus("Update error: " + e.getMessage(), true);
        }
    }

    // ===================== WRITE TICKET =====================

    @FXML
    private void handleSaveTicket() {
        try {
            String sel = vCarCombo.getValue();
            if (sel == null) { showStatus("Select a Car.", true); return; }

            String type = TicketTypeCombo.getValue();
            if (type == null) { showStatus("Select a Ticket type.", true); return; }
            if ("Other".equals(type)) {
                type = TicketTypeField.getText().trim();
                if (type.isEmpty()) { showStatus("Specify the Ticket type.", true); return; }
            }

            String fineStr = fineField.getText().trim();
            if (fineStr.isEmpty()) { showStatus("Enter a fine amount.", true); return; }

            if (vOfficerField.getText().trim().isEmpty()) { showStatus("Officer name is required.", true); return; }

            Ticket vi = new Ticket();
            vi.setVehicleId(Integer.parseInt(sel.split(":")[0].trim()));
            vi.setTicketDate(TicketDatePicker.getValue());
            vi.setTicketType(type);
            vi.setFineAmount(Double.parseDouble(fineStr));
            vi.setStatus(TicketStatusCombo.getValue() != null ? TicketStatusCombo.getValue() : "UNPAID");
            vi.setOfficerName(vOfficerField.getText().trim());
            vi.setLocation(locationField.getText().trim());
            vi.setPaymentDueDate(paymentDueDatePicker.getValue());
            vi.setDescription(TicketDescArea.getText().trim());

            if (TicketData.save(vi)) {
                showStatus("🎫 Ticket issued! Fine: R" + String.format("%.2f", vi.getFineAmount())
                    + " | Due: " + vi.getPaymentDueDate(), false);
                clearTicketForm();
                loadData();
            }
        } catch (NumberFormatException e) { showStatus("Fine must be a valid number.", true);
        } catch (SQLException e)          { showStatus("Save error: " + e.getMessage(), true); }
    }

    // ===================== Car SEARCH =====================

    @FXML
    private void handleCarSearch() {
        String reg = searchRegField.getText().trim();
        if (reg.isEmpty()) { showStatus("Enter a registration number to search.", true); return; }

        new Thread(() -> {
            try {
                Car v = CarData.findByRegistration(reg);
                if (v != null) {
                    List<Ticket> Tickets = TicketData.findByCarId(v.getId());
                    List<PoliceInfo> reports = reportDAO.findByCarId(v.getId());
                    
                    Platform.runLater(() -> {
                        searchResultBox.setVisible(true);
                        searchResultBox.setManaged(true);
                        notFoundBox.setVisible(false);
                        notFoundBox.setManaged(false);

                        resReg.setText(v.getRegistrationNumber());
                        resMakeModel.setText(v.getMake() + " " + v.getModel());
                        resYearColor.setText(v.getYear() + " | " + v.getColor());
                        String status = v.getStatus() != null ? v.getStatus() : "ACTIVE";
                        resStatus.setText(status);
                        updateStatusBadge(status);
                        lastSearchedCarId = v.getId();
                        resOwnerName.setText(v.getOwnerName() != null ? v.getOwnerName() : "No Registered Owner");
                        resOwnerPhone.setText(v.getOwnerPhone() != null ? v.getOwnerPhone() : "—");
                        resOwnerEmail.setText(v.getOwnerEmail() != null ? v.getOwnerEmail() : "—");

                        resTicketsList.setItems(FXCollections.observableArrayList(
                            Tickets.stream().map(vi -> vi.getTicketType() + " (" + vi.getTicketDate() + ") - R" + vi.getFineAmount()).collect(Collectors.toList())
                        ));
                        if (Tickets.isEmpty()) resTicketsList.setItems(FXCollections.observableArrayList("No Tickets found."));

                        resReportsList.setItems(FXCollections.observableArrayList(
                            reports.stream().map(rp -> rp.getReportType() + " (" + rp.getReportDate() + ") - " + rp.getStatus()).collect(Collectors.toList())
                        ));
                        if (reports.isEmpty()) resReportsList.setItems(FXCollections.observableArrayList("No police reports found."));
                        
                        Effects.fadeInAndSlide(searchResultBox, 300);
                    });
                } else {
                    Platform.runLater(() -> {
                        searchResultBox.setVisible(false);
                        searchResultBox.setManaged(false);
                        notFoundBox.setVisible(true);
                        notFoundBox.setManaged(true);
                        Effects.fadeInAndSlide(notFoundBox, 300);
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Search error: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void handleFileUnregisteredReport() {
        String reg = searchRegField.getText().trim();
        showStatus("🚨 Unregistered Car report initiated for " + reg + ". Please switch to 'File Report' tab to complete.", false);
    }

    @FXML
    private void handleUpdateCarStatus() {
        if (lastSearchedCarId == -1) return;
        String newStatus = updateStatusCombo.getValue();
        if (newStatus == null) { showStatus("Select a status to update.", true); return; }

        try {
            if (CarData.updateStatus(lastSearchedCarId, newStatus)) {
                resStatus.setText(newStatus);
                updateStatusBadge(newStatus);
                showStatus("✅ Car status updated to " + newStatus, false);
            }
        } catch (SQLException e) {
            showStatus("Update error: " + e.getMessage(), true);
        }
    }

    // ===================== CHARTS =====================

    private void buildReportTypeChart(Map<String, Integer> data) {
        ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        data.forEach((t, c) -> pie.add(new PieChart.Data(t + " (" + c + ")", c)));
        reportTypePieChart.setData(pie);
    }

    private void buildFinesChart(Map<String, Double> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Fines (R)");
        data.forEach((t, v) -> series.getData().add(new XYChart.Data<>(t, v)));
        finesBarChart.getData().setAll(series);
    }

    private void buildVioStatusChart(Map<String, Integer> data) {
        ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        data.forEach((s, c) -> pie.add(new PieChart.Data(s + " (" + c + ")", c)));
        TicketStatusPie.setData(pie);
    }

    // ===================== CLEAR FORMS =====================

    private void clearReportForm() {
        CarCombo.setValue(null);
        reportTypeCombo.setValue(null);
        reportDescArea.clear();
        officerNameField.clear();
        badgeField.clear();
        reportStatusCombo.setValue(null);
        reportDatePicker.setValue(LocalDate.now());
    }

    private void clearTicketForm() {
        vCarCombo.setValue(null);
        TicketTypeCombo.setValue(null);
        TicketTypeField.clear();
        TicketDescArea.clear();
        fineField.clear();
        locationField.clear();
        vOfficerField.clear();
        TicketStatusCombo.setValue(null);
        TicketDatePicker.setValue(LocalDate.now());
        paymentDueDatePicker.setValue(LocalDate.now().plusDays(30));
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill:#ef4444;" : "-fx-text-fill:#22c55e;");
        statusLabel.setVisible(true);
    }

    private void updateStatusBadge(String status) {
        resStatus.getStyleClass().removeAll("status-stolen", "status-recovered", "status-impounded");
        if (status == null) return;
        switch (status.toUpperCase()) {
            case "STOLEN": resStatus.getStyleClass().add("status-stolen"); break;
            case "RECOVERED": resStatus.getStyleClass().add("status-recovered"); break;
            case "IMPOUNDED": resStatus.getStyleClass().add("status-impounded"); break;
        }
    }

    public void setNavigationMode(String mode) {
        // Reset visibility
        statsSection.setVisible(false); statsSection.setManaged(false);
        searchSection.setVisible(false); searchSection.setManaged(false);
        reportSection.setVisible(false); reportSection.setManaged(false);
        TicketSection.setVisible(false); TicketSection.setManaged(false);

        switch (mode) {
            case "Search":
                searchSection.setVisible(true); searchSection.setManaged(true);
                Effects.fadeIn(searchSection, 400);
                break;
            case "Report":
                reportSection.setVisible(true); reportSection.setManaged(true);
                Effects.fadeIn(reportSection, 400);
                break;
            case "Ticket":
                TicketSection.setVisible(true); TicketSection.setManaged(true);
                Effects.fadeIn(TicketSection, 400);
                break;
            case "All":
            default:
                statsSection.setVisible(true); statsSection.setManaged(true);
                Effects.fadeIn(statsSection, 400);
                break;
        }
    }
}
