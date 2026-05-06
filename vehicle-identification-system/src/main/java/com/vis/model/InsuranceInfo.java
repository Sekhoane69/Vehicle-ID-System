package com.vis.model;

import java.time.LocalDate;

public class InsuranceInfo extends Thing {

    private int vehicleId;
    private String registrationNumber;
    private String provider;
    private String policyNumber;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private double premiumAmount;
    private String coverageType;
    private String status;

    public InsuranceInfo() {}

    public InsuranceInfo(int id, int vehicleId, String provider, String policyNumber,
                           LocalDate startDate, LocalDate expiryDate, double premiumAmount,
                           String coverageType, String status) {
        super(id);
        this.vehicleId = vehicleId;
        this.provider = provider;
        this.policyNumber = policyNumber;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.premiumAmount = premiumAmount;
        this.coverageType = coverageType;
        this.status = status;
    }

    @Override
    public String getSummary() {
        return provider + " | " + policyNumber + " | " + status;
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public double getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(double premiumAmount) { this.premiumAmount = premiumAmount; }
    public String getCoverageType() { return coverageType; }
    public void setCoverageType(String coverageType) { this.coverageType = coverageType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
