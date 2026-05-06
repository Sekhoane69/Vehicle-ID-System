package com.vis.model;

import java.time.LocalDate;

public class Question extends Thing {

    private int customerId;
    private String ClientName;
    private int vehicleId;
    private String registrationNumber;
    private LocalDate queryDate;
    private String queryText;
    private String responseText;
    private String status;

    public Question() {}

    public Question(int id, int customerId, int vehicleId, LocalDate queryDate,
                         String queryText, String responseText, String status) {
        super(id);
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.queryDate = queryDate;
        this.queryText = queryText;
        this.responseText = responseText;
        this.status = status;
    }

    @Override
    public String getSummary() {
        return queryDate + " | " + queryText + " [" + status + "]";
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getClientName() { return ClientName; }
    public void setClientName(String ClientName) { this.ClientName = ClientName; }
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
    public LocalDate getQueryDate() { return queryDate; }
    public void setQueryDate(LocalDate queryDate) { this.queryDate = queryDate; }
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
