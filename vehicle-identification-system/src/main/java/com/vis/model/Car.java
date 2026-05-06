package com.vis.model;

public class Car extends Thing {

    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private String color;
    private int ownerId;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String status;

    public Car() {}

    public Car(int id, String registrationNumber, String make, String model, int year, String color, int ownerId) {
        super(id);
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.color = color;
        this.ownerId = ownerId;
    }

    @Override
    public String getSummary() {
        return year + " " + make + " " + model + " [" + registrationNumber + "]";
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCarName() { return make + " " + model; }
}
