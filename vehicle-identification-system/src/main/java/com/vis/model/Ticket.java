package com.vis.model;

import java.time.LocalDate;

public class Ticket extends Thing {

    private int vehicleId;
    private String registrationNumber;
    private LocalDate TicketDate;
    private String TicketType;
    private double fineAmount;
    private String status;
    private String officerName;
    private String location;
    private LocalDate paymentDueDate;
    private String description;

    public Ticket() {}

    public Ticket(int id, int vehicleId, LocalDate TicketDate, String TicketType,
                     double fineAmount, String status, String officerName, String location) {
        super(id);
        this.vehicleId = vehicleId;
        this.TicketDate = TicketDate;
        this.TicketType = TicketType;
        this.fineAmount = fineAmount;
        this.status = status;
        this.officerName = officerName;
        this.location = location;
    }

    @Override
    public String getSummary() {
        return TicketDate + " | " + TicketType + " | $" + fineAmount + " [" + status + "]";
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
    public LocalDate getTicketDate() { return TicketDate; }
    public void setTicketDate(LocalDate TicketDate) { this.TicketDate = TicketDate; }
    public String getTicketType() { return TicketType; }
    public void setTicketType(String TicketType) { this.TicketType = TicketType; }
    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDate getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(LocalDate paymentDueDate) { this.paymentDueDate = paymentDueDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
