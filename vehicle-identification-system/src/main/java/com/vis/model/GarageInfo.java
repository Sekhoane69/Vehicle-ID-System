package com.vis.model;

import java.time.LocalDate;

public class GarageInfo extends Thing {

    private int vehicleId;
    private String registrationNumber;
    private LocalDate serviceDate;
    private String serviceType;
    private String description;
    private double cost;
    private String technician;
    private String status;

    public GarageInfo() {}

    public GarageInfo(int id, int vehicleId, LocalDate serviceDate, String serviceType,
                         String description, double cost, String technician, String status) {
        super(id);
        this.vehicleId = vehicleId;
        this.serviceDate = serviceDate;
        this.serviceType = serviceType;
        this.description = description;
        this.cost = cost;
        this.technician = technician;
        this.status = status;
    }

    @Override
    public String getSummary() {
        return serviceDate + " | " + serviceType + " | $" + cost;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public LocalDate getServiceDate() { return serviceDate; }
    public void setServiceDate(LocalDate serviceDate) { this.serviceDate = serviceDate; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public String getTechnician() { return technician; }
    public void setTechnician(String technician) { this.technician = technician; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
