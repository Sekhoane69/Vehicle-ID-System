package com.vis.model;

public class Client extends Thing {

    private String name;
    private String address;
    private String phone;
    private String email;

    public Client() {}
//constructor
    public Client(int id, String name, String address, String phone, String email) {
        super(id);
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String getSummary() {
        return "Client: " + name + " | " + phone + " | " + email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
