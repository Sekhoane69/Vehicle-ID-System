package com.vis.controller;

import com.vis.dao.ClientData;
import com.vis.dao.PersonData;
import com.vis.dao.CarData;
import com.vis.model.Client;
import com.vis.model.Person;
import com.vis.model.Car;
import com.vis.util.Effects;
import com.vis.util.LoginSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Admin implements Initializable {

    @FXML private VBox adminRoot;
    @FXML private Label adminWelcomeLabel;
    @FXML private ProgressBar loadProgress;
    @FXML private Label statusLabel;

    // ---- Persons Tab ----
    @FXML private TextField    newpersonnameField, newEmailField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<String> newRoleCombo;
    @FXML private Button createPersonBtn, updatePersonBtn, deletePersonBtn, togglePersonBtn;
    @FXML private TableView<Person> PersonsTable;
    @FXML private TableColumn<Person, String> PersonIdCol, personnameCol, PersonEmailCol,
                                             PersonRoleCol, PersonActiveCol, PersonCreatedCol;

    // ---- Clients Tab ----
    @FXML private TextField custNameField, custEmailField, custPhoneField, custAddressField;
    @FXML private TableView<Client> ClientsTable;
    @FXML private TableColumn<Client, String> custIdCol, custNameCol, custEmailCol,
                                                 custPhoneCol, custAddrCol, custCreatedCol;

    // ---- Cars Tab ----
    @FXML private TextField regField, makeField, modelField, yearField, colorField;
    @FXML private ComboBox<String> ownerCombo;
    @FXML private Button saveCarBtn, updateCarBtn, deleteCarBtn;
    @FXML private TableView<Car> CarsTable;
    @FXML private TableColumn<Car, String> vehIdCol, vehRegCol, vehMakeCol,
                                                vehModelCol, vehYearCol, vehColorCol, vehOwnerCol;

    // ---- Overview ----
    @FXML private BarChart<String, Number> PersonsByRoleChart;

    // ---- Broadcast Tab ----
    @FXML private TextArea broadcastMessageArea;
    @FXML private ComboBox<String> broadcastTypeCombo;
    @FXML private DatePicker broadcastExpiryPicker;
    @FXML private ListView<String> recentBroadcastsList;

    private final PersonData     PersonData     = new PersonData();
    private final ClientData ClientData = new ClientData();
    private final CarData  CarData  = new CarData();
    private final com.vis.dao.AlertData AlertData = new com.vis.dao.AlertData();

    private ObservableList<Person>     PersonList     = FXCollections.observableArrayList();
    private ObservableList<Client> ClientList = FXCollections.observableArrayList();
    private ObservableList<Car>  CarList  = FXCollections.observableArrayList();
    private List<Client> allClients; // for owner combo

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Effects.fadeInAndSlide(adminRoot, 400);

        String role = LoginSession.getInstance().getRole();
        adminWelcomeLabel.setText("Admin Panel — " + LoginSession.getInstance().getCurrentPerson().getpersonname());

        if (!"ADMIN".equals(role)) {
            adminRoot.setDisable(true);
            showStatus("Access denied: Admin role required.", true);
            return;
        }

        newRoleCombo.setItems(FXCollections.observableArrayList("ADMIN", "WORKSHOP", "CUSTOMER", "POLICE", "INSURANCE"));
        broadcastTypeCombo.setItems(FXCollections.observableArrayList("INFO", "WARNING", "SYSTEM", "ALARM"));
        broadcastTypeCombo.setValue("SYSTEM");
        broadcastExpiryPicker.setValue(LocalDate.now().plusDays(7));

        wirePersonTable();
        wireClientTable();
        wireCarTable();

        // Populate form when row selected
        PersonsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> populatePersonForm(sel));
        ClientsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> populateClientForm(sel));
        CarsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> populateCarForm(sel));

        loadProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        loadAll();
    }

    // ===================== WIRE TABLE COLUMNS =====================

    private void wirePersonTable() {
        PersonIdCol.setCellValueFactory(c     -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        personnameCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getpersonname()));
        PersonEmailCol.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getEmail()));
        PersonRoleCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getRole()));
        PersonActiveCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "✅ Yes" : "❌ No"));
        PersonCreatedCol.setCellValueFactory(c -> {
            if (c.getValue().getCreatedAt() != null)
                return new SimpleStringProperty(c.getValue().getCreatedAt().toLocalDate().toString());
            return new SimpleStringProperty("");
        });
        PersonsTable.setItems(PersonList);
    }

    private void wireClientTable() {
        custIdCol.setCellValueFactory(c      -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        custNameCol.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getName()));
        custEmailCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getEmail()));
        custPhoneCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getPhone()));
        custAddrCol.setCellValueFactory(c    -> new SimpleStringProperty(c.getValue().getAddress()));
        custCreatedCol.setCellValueFactory(c -> {
            if (c.getValue().getCreatedAt() != null)
                return new SimpleStringProperty(c.getValue().getCreatedAt().toLocalDate().toString());
            return new SimpleStringProperty("");
        });
        ClientsTable.setItems(ClientList);
    }

    private void wireCarTable() {
        vehIdCol.setCellValueFactory(c    -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        vehRegCol.setCellValueFactory(c   -> new SimpleStringProperty(c.getValue().getRegistrationNumber()));
        vehMakeCol.setCellValueFactory(c  -> new SimpleStringProperty(c.getValue().getMake()));
        vehModelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        vehYearCol.setCellValueFactory(c  -> new SimpleStringProperty(String.valueOf(c.getValue().getYear())));
        vehColorCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getColor()));
        vehOwnerCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOwnerName() != null ? c.getValue().getOwnerName() : "Unassigned"));
        CarsTable.setItems(CarList);
    }

    // ===================== LOAD DATA =====================

    private void loadAll() {
        new Thread(() -> {
            try {
                List<Person>     Persons     = PersonData.findAll();
                List<Client> Clients = ClientData.findAll();
                List<Car>  Cars  = CarData.findAll();
                allClients = Clients;

                Platform.runLater(() -> {
                    loadProgress.setProgress(1.0);
                    PersonList.setAll(Persons);
                    ClientList.setAll(Clients);
                    CarList.setAll(Cars);

                    ownerCombo.setItems(FXCollections.observableArrayList(
                        Clients.stream().map(c -> c.getId() + ": " + c.getName()).collect(Collectors.toList())));

                    buildRoleChart(Persons);
                    loadRecentBroadcasts();
                });
            } catch (SQLException e) {
                Platform.runLater(() -> showStatus("Load error: " + e.getMessage(), true));
            }
        }).start();
    }

    // ===================== Person CRUD =====================

    @FXML
    private void handleCreatePerson() {
        String personname = newpersonnameField.getText().trim();
        String email    = newEmailField.getText().trim();
        String password = newPasswordField.getText();
        String role     = newRoleCombo.getValue();

        if (personname.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            showStatus("All fields are required to create a Person.", true); return;
        }
        try {
            if (PersonData.usernameExists(personname)) { showStatus("personname already exists.", true); return; }
            Person u = new Person();
            u.setpersonname(personname); u.setEmail(email); u.setPassword(password);
            u.setRole(role); u.setActive(true);
            if (PersonData.save(u)) {
                if ("CUSTOMER".equals(role) && ClientData.findByEmail(email) == null) {
                    Client c = new Client();
                    c.setName(personname); c.setEmail(email);
                    ClientData.save(c);
                }
                showStatus("Person '" + personname + "' created!", false);
                clearPersonForm();
                loadAll();
            }
        } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleUpdatePerson() {
        Person selected = PersonsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Person to update.", true); return; }

        String personname = newpersonnameField.getText().trim();
        String email = newEmailField.getText().trim();
        String role  = newRoleCombo.getValue();
        
        if (!personname.isEmpty()) selected.setpersonname(personname);
        if (!email.isEmpty())    selected.setEmail(email);
        if (role != null)        selected.setRole(role);

        // If password entered, update it
        String pw = newPasswordField.getText();
        try {
            if (!pw.isEmpty()) {
                String sql = "UPDATE users SET password = ? WHERE user_id = ?";
                try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance()
                        .getConnection().prepareStatement(sql)) {
                    ps.setString(1, org.mindrot.jbcrypt.BCrypt.hashpw(pw, org.mindrot.jbcrypt.BCrypt.gensalt()));
                    ps.setInt(2, selected.getId());
                    ps.executeUpdate();
                }
            }
            if (PersonData.update(selected)) {
                showStatus("Person updated.", false);
                clearPersonForm();
                loadAll();
            }
        } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleDeletePerson() {
        Person selected = PersonsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Person to delete.", true); return; }

        Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete Person '" + selected.getpersonname() + "'?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (confirm.isPresent() && confirm.get() == ButtonType.YES) {
            try {
                // Hard delete
                String sql = "DELETE FROM users WHERE user_id = ?";
                try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance()
                        .getConnection().prepareStatement(sql)) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }
                showStatus("Person deleted.", false);
                loadAll();
            } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
        }
    }

    @FXML
    private void handleTogglePerson() {
        Person selected = PersonsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Person to toggle.", true); return; }
        try {
            selected.setActive(!selected.isActive());
            if (PersonData.update(selected)) {
                showStatus("Person " + (selected.isActive() ? "activated" : "deactivated") + ".", false);
                loadAll();
            }
        } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
    }

    // ===================== Client CRUD =====================

    @FXML
    private void handleCreateClient() {
        String name  = custNameField.getText().trim();
        String email = custEmailField.getText().trim();
        if (name.isEmpty() || email.isEmpty()) { showStatus("Name and email are required.", true); return; }
        try {
            Client c = new Client();
            c.setName(name); c.setEmail(email);
            c.setPhone(custPhoneField.getText().trim());
            c.setAddress(custAddressField.getText().trim());
            if (ClientData.save(c)) { showStatus("Client added!", false); clearClientForm(); loadAll(); }
        } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleUpdateClient() {
        Client selected = ClientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Client to update.", true); return; }
        String name  = custNameField.getText().trim();
        String email = custEmailField.getText().trim();
        if (!name.isEmpty())  selected.setName(name);
        if (!email.isEmpty()) selected.setEmail(email);
        String phone = custPhoneField.getText().trim();
        String addr  = custAddressField.getText().trim();
        if (!phone.isEmpty()) selected.setPhone(phone);
        if (!addr.isEmpty())  selected.setAddress(addr);
        try {
            if (ClientData.update(selected)) { showStatus("Client updated.", false); clearClientForm(); loadAll(); }
        } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleDeleteClient() {
        Client selected = ClientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Client to delete.", true); return; }
        Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete Client '" + selected.getName() + "'?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (confirm.isPresent() && confirm.get() == ButtonType.YES) {
            try {
                if (ClientData.delete(selected.getId())) { showStatus("Client deleted.", false); loadAll(); }
            } catch (SQLException e) { showStatus("Error (may have linked records): " + e.getMessage(), true); }
        }
    }

    // ===================== Car CRUD =====================

    @FXML
    private void handleSaveCar() {
        try {
            String reg = regField.getText().trim();
            String make = makeField.getText().trim();
            String model = modelField.getText().trim();
            String yearStr = yearField.getText().trim();
            if (reg.isEmpty() || make.isEmpty() || model.isEmpty() || yearStr.isEmpty()) {
                showStatus("Registration, Make, Model and Year are required.", true); return;
            }
            Car v = new Car();
            v.setRegistrationNumber(reg); v.setMake(make); v.setModel(model);
            v.setYear(Integer.parseInt(yearStr));
            v.setColor(colorField.getText().trim());
            String ownerSel = ownerCombo.getValue();
            if (ownerSel != null && !ownerSel.isEmpty())
                v.setOwnerId(Integer.parseInt(ownerSel.split(":")[0].trim()));
            if (CarData.save(v)) { showStatus("Car registered!", false); clearCarForm(); loadAll(); }
        } catch (NumberFormatException e) { showStatus("Year must be a number.", true);
        } catch (SQLException e)          { showStatus("Error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleUpdateCar() {
        Car selected = CarsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Car to update.", true); return; }
        String reg = regField.getText().trim();
        String make = makeField.getText().trim();
        String model = modelField.getText().trim();
        String yearStr = yearField.getText().trim();
        if (!reg.isEmpty())   selected.setRegistrationNumber(reg);
        if (!make.isEmpty())  selected.setMake(make);
        if (!model.isEmpty()) selected.setModel(model);
        if (!yearStr.isEmpty()) { try { selected.setYear(Integer.parseInt(yearStr)); } catch (NumberFormatException e) { showStatus("Year must be a number.", true); return; } }
        String color = colorField.getText().trim();
        if (!color.isEmpty()) selected.setColor(color);
        String ownerSel = ownerCombo.getValue();
        if (ownerSel != null && !ownerSel.isEmpty())
            selected.setOwnerId(Integer.parseInt(ownerSel.split(":")[0].trim()));
        try {
            String sql = "UPDATE vehicles SET registration_number=?, make=?, model=?, year=?, color=?, owner_id=? WHERE vehicle_id=?";
            try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance()
                    .getConnection().prepareStatement(sql)) {
                ps.setString(1, selected.getRegistrationNumber());
                ps.setString(2, selected.getMake());
                ps.setString(3, selected.getModel());
                ps.setInt(4, selected.getYear());
                ps.setString(5, selected.getColor());
                if (selected.getOwnerId() > 0) ps.setInt(6, selected.getOwnerId());
                else ps.setNull(6, java.sql.Types.INTEGER);
                ps.setInt(7, selected.getId());
                if (ps.executeUpdate() > 0) { showStatus("Car updated.", false); clearCarForm(); loadAll(); }
            }
        } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
    }

    @FXML
    private void handleDeleteCar() {
        Car selected = CarsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showStatus("Select a Car to delete.", true); return; }
        Optional<ButtonType> confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete Car '" + selected.getRegistrationNumber() + "'?", ButtonType.YES, ButtonType.NO).showAndWait();
        if (confirm.isPresent() && confirm.get() == ButtonType.YES) {
            try {
                String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
                try (java.sql.PreparedStatement ps = com.vis.dao.ConnectDB.getInstance()
                        .getConnection().prepareStatement(sql)) {
                    ps.setInt(1, selected.getId());
                    ps.executeUpdate();
                }
                showStatus("Car deleted.", false);
                loadAll();
            } catch (SQLException e) { showStatus("Error: " + e.getMessage(), true); }
        }
    }

    // ===================== FORM POPULATION =====================

    private void populatePersonForm(Person u) {
        if (u == null) return;
        newpersonnameField.setText(u.getpersonname());
        newEmailField.setText(u.getEmail());
        newPasswordField.clear();
        newRoleCombo.setValue(u.getRole());
    }

    private void populateClientForm(Client c) {
        if (c == null) return;
        custNameField.setText(c.getName());
        custEmailField.setText(c.getEmail());
        custPhoneField.setText(c.getPhone() != null ? c.getPhone() : "");
        custAddressField.setText(c.getAddress() != null ? c.getAddress() : "");
    }

    private void populateCarForm(Car v) {
        if (v == null) return;
        regField.setText(v.getRegistrationNumber());
        makeField.setText(v.getMake());
        modelField.setText(v.getModel());
        yearField.setText(String.valueOf(v.getYear()));
        colorField.setText(v.getColor() != null ? v.getColor() : "");
        if (v.getOwnerId() > 0 && allClients != null) {
            allClients.stream()
                .filter(c -> c.getId() == v.getOwnerId())
                .findFirst()
                .ifPresent(c -> ownerCombo.setValue(c.getId() + ": " + c.getName()));
        } else {
            ownerCombo.setValue(null);
        }
    }

    // ===================== CHART =====================

    @SuppressWarnings("unchecked")
    private void buildRoleChart(List<Person> Persons) {
        Map<String, Long> byRole = Persons.stream()
            .collect(Collectors.groupingBy(Person::getRole, Collectors.counting()));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Persons");
        byRole.forEach((role, cnt) -> series.getData().add(new XYChart.Data<>(role, cnt)));
        PersonsByRoleChart.getData().setAll(series);
    }

    // ===================== BROADCAST =====================

    @FXML
    private void handleSendBroadcast() {
        String msg = broadcastMessageArea.getText().trim();
        if (msg.isEmpty()) { showStatus("Please enter a message.", true); return; }

        try {
            com.vis.model.Alert n = new com.vis.model.Alert();
            n.setSenderId(LoginSession.getInstance().getCurrentPerson().getId());
            n.setMessage(msg);
            n.setType(broadcastTypeCombo.getValue());
            n.setBroadcast(true);
            if (broadcastExpiryPicker.getValue() != null) {
                n.setExpiresAt(broadcastExpiryPicker.getValue().atTime(23, 59));
            }
            if (AlertData.save(n)) {
                showStatus("Broadcast sent successfully!", false);
                broadcastMessageArea.clear();
                loadRecentBroadcasts();
            }
        } catch (SQLException e) {
            showStatus("Broadcast error: " + e.getMessage(), true);
        }
    }

    private void loadRecentBroadcasts() {
        new Thread(() -> {
            try {
                List<com.vis.model.Alert> list = AlertData.findLatestBroadcasts(10);
                Platform.runLater(() -> {
                    recentBroadcastsList.setItems(FXCollections.observableArrayList(
                        list.stream().map(n -> n.getCreatedAt().toLocalDate() + " | " + n.getType() + ": " + n.getMessage()).collect(Collectors.toList())
                    ));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ===================== HELPERS =====================

    private void clearPersonForm()     { newpersonnameField.clear(); newEmailField.clear(); newPasswordField.clear(); newRoleCombo.setValue(null); }
    private void clearClientForm() { custNameField.clear(); custEmailField.clear(); custPhoneField.clear(); custAddressField.clear(); }
    private void clearCarForm()  { regField.clear(); makeField.clear(); modelField.clear(); yearField.clear(); colorField.clear(); ownerCombo.setValue(null); }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setStyle(isError ? "-fx-text-fill:#ef4444;" : "-fx-text-fill:#22c55e;");
        statusLabel.setVisible(true);
    }
}
