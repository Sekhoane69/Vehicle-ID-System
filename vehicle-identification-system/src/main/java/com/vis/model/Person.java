package com.vis.model;

public class Person extends Thing {

    private String personname;
    private String password;
    private String email;
    private String role;
    private boolean active;

    public Person() {}

    public Person(int id, String personname, String email, String role, boolean active) {
        super(id);
        this.personname = personname;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    @Override
    public String getSummary() {
        return "Person: " + personname + " [" + role + "]";
    }

    public String getpersonname() { return personname; }
    public void setpersonname(String personname) { this.personname = personname; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
