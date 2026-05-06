package com.vis.model;

import java.time.LocalDate;

public class BookingInfo extends Thing {
    private int vehicleId;
    private String registrationNumber;
    private String ownerName;
    private LocalDate requestDate;
    private String description;
    private String status;

    public BookingInfo() {}

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String getSummary() {
        return registrationNumber + " | " + status + " | " + requestDate;
    }
}
