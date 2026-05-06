package com.vis.controller;

import com.vis.dao.*;
import com.vis.model.*;
import com.vis.util.Effects;
import com.vis.util.LoginSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientView implements Initializable {

    // ---- Admin/Workshop fields ----
    @FXML private VBox ClientRoot;
    @FXML private TextField nameField, addressField, phoneField, emailField;
    @FXML private Button saveClientBtn;
    @FXML private TextField searchField;
    @FXML private ListView<String> ClientListView;
    @FXML private BarChart<String, Number> CarsByYearChart;
    @FXML private BarChart<String, Number> CarsByMakeChart;
    @FXML private ScrollPane ClientScrollPane;
    @FXML private VBox ClientItemsBox;
    @FXML private Pagination ClientPagination;

    // ---- Shared ----
    @FXML private ProgressBar loadProgress;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    @FXML private VBox adminView;
    @FXML private VBox ClientView;

    // ---- My Cars Tab ----
    @FXML private ListView<String> myCarsListView;
    @FXML private VBox CarDetailPanel;
    @FXML private Label CarDetailTitle;
    @FXML private Label detailReg, detailMakeModel, detailYear, detailColor;
    @FXML private ListView<String> CarTicketsListView;
    @FXML private VBox serviceProblemPanel;
    @FXML private ComboBox<String> problemTypeCombo;
    @FXML private TextArea problemDescArea;

    // ---- Sell Tab ----
    @FXML private ComboBox<String> myCarsCombo;
    @FXML private TextField buyerEmailField;
    @FXML private ListView<Question> incomingOffersListView;

    // ---- Report Stolen Tab ----
    @FXML private ComboBox<String> stolenCarCombo;
    @FXML private TextField stolenLocationField;
    @FXML private TextArea stolenDescArea;
    @FXML private ListView<String> myPoliceReportsListView;

    // ---- Insurance Tab ----
    @FXML private ComboBox<String> insCarCombo;
    @FXML private TextField insProviderField, insPolicyField, insPremiumField;
    @FXML private DatePicker insStartDate, insExpiryDate;
    @FXML private ComboBox<String> insCoverageCombo;
    @FXML private ListView<String> myInsuranceListView;

    // ---- DAOs ----
    private final ClientData     ClientData  = new ClientData();
    private final CarData      CarData   = new CarData();
    private final TicketData    TicketData = new TicketData();
    private final PoliceData policeDAO    = new PoliceData();
    private final InsuranceData    InsuranceData = new InsuranceData();
    private final GarageData serviceDAO  = new GarageData();

    private Client      currentClient;
    private List<Car> myCars;
    private List<Question> incomingOffers;
    private Car       selectedCar;
    private List<Client> allClients;
    private static final int PAGE_SIZE = 5;

    // ===================== INITIALIZE =====================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(ClientRoot, 400);
        loadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        String role = LoginSession.getInstance().getRole().toUpperCase();
        if ("CLIENT".equals(role) || "CUSTOMER".equals(role)) {
            adminView.setVisible(false);
            adminView.setManaged(false);
            ClientView.setVisible(true);
            ClientView.setManaged(true);
            setupClientCombos();
            loadClientData();
        } else if ("ADMIN".equals(role)) {
            adminView.setVisible(true);
            adminView.setManaged(true);
            ClientView.setVisible(false);
            ClientView.setManaged(false);
            Effects.applyDropShadow(saveClientBtn, javafx.scene.paint.Color.web("#0891b2"), 12, 0, 3);
            loadData();
        } else {
            // Workshop/Police/Insurance shouldn't really be here, but if they are, hide both
            adminView.setVisible(false);
            adminView.setManaged(false);
            ClientView.setVisible(false);
            ClientView.setManaged(false);
        }
    }

    private void setupClientCombos() {
        problemTypeCombo.setItems(FXCollections.observableArrayList(
            "Engine Problem", "Brake Issue", "Electrical Fault", "Transmission",
            "Tyre / Wheel", "Body Damage", "Suspension", "AC / Heating", "Other"));
        insCoverageCombo.setItems(FXCollections.observableArrayList(
            "Comprehensive", "Third Party", "Third Party Fire & Theft", "Write-Off Only"));
        insStartDate.setValue(LocalDate.now());
        insExpiryDate.setValue(LocalDate.now().plusYears(1));

        // Car click → show detail
        myCarsListView.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            int i = idx.intValue();
            if (myCars != null && i >= 0 && i < myCars.size()) {
                showCarDetail(myCars.get(i));
            }
        });
    }

    // ===================== ADMIN DATA LOAD =====================

    private void loadData() {
        new Thread(() -> {
            try {
                allClients = ClientData.findAll();
                Map<String, Integer> byYear = CarData.countByYear();
                Map<String, Integer> byMake = CarData.countByMake();
                Platform.runLater(() -> {
                    loadProgress.setProgress(1.0);
                    progressIndicator.setProgress(1.0);
                    ClientListView.setItems(FXCollections.observableArrayList(
                        allClients.stream().map(c -> c.getName() + " | " + c.getPhone() + " | " + c.getEmail()).collect(Collectors.toList())));
                    buildYearChart(byYear);
                    buildMakeChart(byMake);
                    buildScrollPane();
                    setupPagination();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Load error: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML private void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (allClients == null) return;
        ClientListView.setItems(FXCollections.observableArrayList(
            allClients.stream()
                .filter(c -> c.getName().toLowerCase().contains(kw) || c.getEmail().toLowerCase().contains(kw) || (c.getPhone() != null && c.getPhone().contains(kw)))
                .map(c -> c.getName() + " | " + c.getPhone() + " | " + c.getEmail())
                .collect(Collectors.toList())));
    }

    @FXML private void handleSaveClient() {
        try {
            Client c = new Client();
            c.setName(nameField.getText().trim()); c.setAddress(addressField.getText().trim());
            c.setPhone(phoneField.getText().trim()); c.setEmail(emailField.getText().trim());
            if (c.getName().isEmpty() || c.getEmail().isEmpty()) { showStatus("Name and email are required.", true); return; }
            if (ClientData.save(c)) { showStatus("Client saved!", false); clearForm(); loadData(); }
        } catch (SQLException e) { showStatus("Save error: " + e.getMessage(), true); }
    }

    // ===================== Client DATA LOAD =====================

    private void loadClientData() {
        new Thread(() -> {
            try {
                String email = LoginSession.getInstance().getCurrentPerson().getEmail();
                currentClient = ClientData.findByEmail(email);
                if (currentClient != null) {
                    myCars    = CarData.findByOwnerId(currentClient.getId());
                    incomingOffers = CarData.getIncomingOffers(currentClient.getId());
                } else {
                    myCars    = Collections.emptyList();
                    incomingOffers = Collections.emptyList();
                }

                // Load police reports for all my Cars
                List<String> policeReportStrings = new java.util.ArrayList<>();
                List<String> insuranceStrings    = new java.util.ArrayList<>();
                if (myCars != null) {
                    for (Car v : myCars) {
                        for (PoliceInfo pr : policeDAO.findByCarId(v.getId())) {
                            policeReportStrings.add(v.getRegistrationNumber() + " | " + pr.getReportType() + " | " + pr.getReportDate() + " | " + pr.getStatus());
                        }
                        for (InsuranceInfo ir : InsuranceData.findByCarId(v.getId())) {
                            insuranceStrings.add(v.getRegistrationNumber() + " | " + ir.getProvider() + " | " + ir.getPolicyNumber() + " | Exp: " + ir.getExpiryDate() + " | " + ir.getStatus());
                        }
                    }
                }

                final List<String> finalPolice = policeReportStrings;
                final List<String> finalIns    = insuranceStrings;

                Platform.runLater(() -> {
                    loadProgress.setProgress(1.0);
                    progressIndicator.setProgress(1.0);

                    // Cars list
                    if (myCars != null) {
                        myCarsListView.setItems(FXCollections.observableArrayList(
                            myCars.stream().map(v -> "🚗 " + v.getRegistrationNumber() + " — " + v.getYear() + " " + v.getMake() + " " + v.getModel() + " (" + v.getColor() + ")")
                                .collect(Collectors.toList())));
                        List<String> regList = myCars.stream().map(Car::getRegistrationNumber).collect(Collectors.toList());
                        myCarsCombo.setItems(FXCollections.observableArrayList(regList));
                        stolenCarCombo.setItems(FXCollections.observableArrayList(regList));
                        insCarCombo.setItems(FXCollections.observableArrayList(regList));
                    }

                    // Incoming offers
                    if (incomingOffers != null) {
                        incomingOffersListView.setItems(FXCollections.observableArrayList(incomingOffers));
                        incomingOffersListView.setCellFactory(p -> new ListCell<Question>() {
                            @Override protected void updateItem(Question item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) { setText(null); return; }
                                if ("PUBLIC_SALE".equals(item.getQueryText()))
                                    setText("📢 Public Sale: " + item.getRegistrationNumber() + " (" + item.getQueryDate() + ")");
                                else
                                    setText("🤝 Direct Offer: " + item.getRegistrationNumber() + " (" + item.getQueryDate() + ")");
                            }
                        });
                    }

                    // Police reports
                    myPoliceReportsListView.setItems(FXCollections.observableArrayList(finalPolice));

                    // Insurance
                    myInsuranceListView.setItems(FXCollections.observableArrayList(finalIns));
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Load error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== Car DETAIL =====================

    private void showCarDetail(Car v) {
        selectedCar = v;
        CarDetailTitle.setText("Details: " + v.getRegistrationNumber());
        detailReg.setText(v.getRegistrationNumber());
        detailMakeModel.setText(v.getMake() + " " + v.getModel());
        detailYear.setText(String.valueOf(v.getYear()));
        detailColor.setText(v.getColor());
        CarDetailPanel.setVisible(true);
        CarDetailPanel.setManaged(true);
        serviceProblemPanel.setVisible(true);
        serviceProblemPanel.setManaged(true);
        Effects.fadeInAndSlide(CarDetailPanel, 300);

        // Load Tickets for this Car in background
        new Thread(() -> {
            try {
                List<Ticket> Tickets = TicketData.findByCarId(v.getId());
                Platform.runLater(() -> {
                    if (Tickets.isEmpty()) {
                        CarTicketsListView.setItems(FXCollections.observableArrayList("No Tickets on record ✅"));
                    } else {
                        CarTicketsListView.setItems(FXCollections.observableArrayList(
                            Tickets.stream().map(vi -> "⚠ " + vi.getTicketType() + " | " + vi.getTicketDate() + " | R" + vi.getFineAmount() + " | " + vi.getStatus())
                                .collect(Collectors.toList())));
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> CarTicketsListView.setItems(FXCollections.observableArrayList("Error loading Tickets")));
            }
        }).start();
    }

    // ===================== PROBLEM REPORT =====================

    @FXML
    private void handleSubmitProblem() {
        if (selectedCar == null) { showStatus("Select a Car first from My Cars.", true); return; }
        String type = problemTypeCombo.getValue();
        String desc = problemDescArea.getText().trim();
        if (type == null || desc.isEmpty()) { showStatus("Please select a type and describe the problem.", true); return; }

        new Thread(() -> {
            try {
                GarageInfo s = new GarageInfo();
                s.setVehicleId(selectedCar.getId());
                s.setServiceDate(LocalDate.now());
                s.setServiceType("Client Report: " + type);
                s.setDescription(desc);
                s.setCost(0.0);
                s.setTechnician("Client Self-Report");
                boolean ok = serviceDAO.save(s);
                Platform.runLater(() -> {
                    if (ok) {
                        showStatus("Problem report submitted for " + selectedCar.getRegistrationNumber() + "!", false);
                        problemTypeCombo.setValue(null);
                        problemDescArea.clear();
                    } else {
                        showStatus("Failed to submit problem.", true);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== SELL / TRANSFER =====================

    @FXML
    private void handleSellCar() {
        String selectedReg = myCarsCombo.getValue();
        String buyerEmail  = buyerEmailField.getText();
        if (buyerEmail != null) buyerEmail = buyerEmail.trim();
        if (selectedReg == null || selectedReg.isEmpty()) { showStatus("Select a Car to sell.", true); return; }
        if (currentClient != null && buyerEmail != null && buyerEmail.equalsIgnoreCase(currentClient.getEmail())) {
            showStatus("You cannot sell a Car to yourself.", true); return;
        }

        final String finalEmail = buyerEmail;
        new Thread(() -> {
            try {
                Car v = myCars.stream().filter(x -> x.getRegistrationNumber().equals(selectedReg)).findFirst().orElse(null);
                if (v == null) { Platform.runLater(() -> showStatus("Car not found.", true)); return; }

                if (finalEmail == null || finalEmail.isEmpty()) {
                    boolean ok = CarData.createTransferOffer(v.getId(), -1, currentClient.getId());
                    Platform.runLater(() -> {
                        if (ok) { showStatus("Car listed for public sale!", false); buyerEmailField.clear(); myCarsCombo.setValue(null); loadClientData(); }
                        else showStatus("Failed to list.", true);
                    });
                } else {
                    Client buyer = ClientData.findByEmail(finalEmail);
                    if (buyer == null) { Platform.runLater(() -> showStatus("Buyer email not found in system.", true)); return; }
                    boolean ok = CarData.createTransferOffer(v.getId(), buyer.getId(), currentClient.getId());
                    Platform.runLater(() -> {
                        if (ok) { showStatus("Offer sent to " + buyer.getName() + "!", false); buyerEmailField.clear(); myCarsCombo.setValue(null); loadClientData(); }
                        else showStatus("Failed to send offer.", true);
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    @FXML
    private void handleAcceptOffer() {
        Question selected = incomingOffersListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select an offer to accept.", true); return; }
        new Thread(() -> {
            try {
                boolean ok = CarData.acceptTransferOffer(selected.getId(), selected.getVehicleId(), currentClient.getId());
                Platform.runLater(() -> {
                    if (ok) { showStatus("Offer accepted! You are now the owner.", false); loadClientData(); }
                    else showStatus("Failed to accept offer.", true);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== REPORT STOLEN =====================

    @FXML
    private void handleReportStolen() {
        String reg  = stolenCarCombo.getValue();
        String loc  = stolenLocationField.getText().trim();
        String desc = stolenDescArea.getText().trim();
        if (reg == null) { showStatus("Select a Car to report stolen.", true); return; }
        if (desc.isEmpty()) { showStatus("Please describe the incident.", true); return; }

        new Thread(() -> {
            try {
                Car v = myCars.stream().filter(x -> x.getRegistrationNumber().equals(reg)).findFirst().orElse(null);
                if (v == null) { Platform.runLater(() -> showStatus("Car not found.", true)); return; }

                PoliceInfo pr = new PoliceInfo();
                pr.setVehicleId(v.getId());
                pr.setReportDate(LocalDate.now());
                pr.setReportType("Theft");
                pr.setDescription("Location: " + loc + "\n" + desc);
                pr.setOfficerName("Client Report");
                pr.setBadgeNumber("SELF");
                pr.setStatus("OPEN");
                boolean ok = policeDAO.save(pr);
                Platform.runLater(() -> {
                    if (ok) {
                        showStatus("Theft report submitted to Police for " + reg + "!", false);
                        stolenCarCombo.setValue(null);
                        stolenLocationField.clear();
                        stolenDescArea.clear();
                        loadClientData();
                    } else {
                        showStatus("Failed to submit report.", true);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== INSURANCE =====================

    @FXML
    private void handleRegisterInsurance() {
        String reg      = insCarCombo.getValue();
        String provider = insProviderField.getText().trim();
        String policy   = insPolicyField.getText().trim();
        String premStr  = insPremiumField.getText().trim();
        String coverage = insCoverageCombo.getValue();
        LocalDate start  = insStartDate.getValue();
        LocalDate expiry = insExpiryDate.getValue();

        if (reg == null || provider.isEmpty() || policy.isEmpty() || premStr.isEmpty() || coverage == null || start == null || expiry == null) {
            showStatus("All insurance fields are required.", true); return;
        }

        new Thread(() -> {
            try {
                Car v = myCars.stream().filter(x -> x.getRegistrationNumber().equals(reg)).findFirst().orElse(null);
                if (v == null) { Platform.runLater(() -> showStatus("Car not found.", true)); return; }

                InsuranceInfo ir = new InsuranceInfo();
                ir.setVehicleId(v.getId());
                ir.setProvider(provider);
                ir.setPolicyNumber(policy);
                ir.setPremiumAmount(Double.parseDouble(premStr));
                ir.setCoverageType(coverage);
                ir.setStartDate(start);
                ir.setExpiryDate(expiry);
                ir.setStatus("ACTIVE");
                boolean ok = InsuranceData.save(ir);
                Platform.runLater(() -> {
                    if (ok) {
                        showStatus("Insurance policy registered for " + reg + "!", false);
                        insCarCombo.setValue(null);
                        insProviderField.clear(); insPolicyField.clear(); insPremiumField.clear();
                        insCoverageCombo.setValue(null);
                        loadClientData();
                    } else {
                        showStatus("Failed to register insurance.", true);
                    }
                });
            } catch (NumberFormatException e) {
                Platform.runLater(() -> showStatus("Premium must be a valid number.", true));
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== ADMIN HELPERS =====================

    @SuppressWarnings("unchecked")
    private void buildYearChart(Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("By Year");
        data.forEach((y, c) -> series.getData().add(new XYChart.Data<>(y, c)));
        CarsByYearChart.getData().setAll(series);
    }

    @SuppressWarnings("unchecked")
    private void buildMakeChart(Map<String, Integer> data) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("By Make");
        data.forEach((m, c) -> series.getData().add(new XYChart.Data<>(m, c)));
        CarsByMakeChart.getData().setAll(series);
    }

    private void buildScrollPane() {
        ClientItemsBox.getChildren().clear();
        if (allClients == null) return;
        int limit = Math.min(25, allClients.size());
        for (int i = 0; i < limit; i++) {
            Client c = allClients.get(i);
            Label lbl = new Label((i + 1) + ". " + c.getName() + " | " + c.getPhone() + " | " + c.getAddress());
            lbl.getStyleClass().add("scroll-item");
            Effects.fadeInAndSlide(lbl, 150 + i * 25L);
            ClientItemsBox.getChildren().add(lbl);
        }
    }

    private void setupPagination() {
        if (allClients == null) return;
        int pages = (int) Math.ceil(allClients.size() / (double) PAGE_SIZE);
        ClientPagination.setPageCount(Math.max(1, pages));
        ClientPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
            int from = newPage.intValue() * PAGE_SIZE;
            int to   = Math.min(from + PAGE_SIZE, allClients.size());
            ClientItemsBox.getChildren().clear();
            for (int i = from; i < to; i++) {
                Client c = allClients.get(i);
                Label lbl = new Label((i + 1) + ". " + c.getName() + " | " + c.getEmail());
                lbl.getStyleClass().add("scroll-item");
                ClientItemsBox.getChildren().add(lbl);
            }
        });
    }

    private void clearForm() { nameField.clear(); addressField.clear(); phoneField.clear(); emailField.clear(); }
    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill:#ef4444;" : "-fx-text-fill:#22c55e;");
        statusLabel.setVisible(true);
    }
}
