package com.vis.model;

import java.time.LocalDate;

public class PoliceInfo extends Thing {

    private int vehicleId;
    private String registrationNumber;
    private LocalDate reportDate;
    private String reportType;
    private String description;
    private String officerName;
    private String badgeNumber;
    private String status;

    public PoliceInfo() {}

    public PoliceInfo(int id, int vehicleId, LocalDate reportDate, String reportType,
                        String description, String officerName, String badgeNumber, String status) {
        super(id);
        this.vehicleId = vehicleId;
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.description = description;
        this.officerName = officerName;
        this.badgeNumber = badgeNumber;
        this.status = status;
    }

    @Override
    public String getSummary() {
        return reportDate + " | " + reportType + " | " + officerName + " [" + status + "]";
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }
    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
