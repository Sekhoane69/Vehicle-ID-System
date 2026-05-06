package com.vis.util;

import com.vis.model.Person;

/**
 * Singleton session manager — holds the currently logged-in Person.
 */
public class LoginSession {

    private static LoginSession instance;
    private Person currentPerson;

    private LoginSession() {}

    public static LoginSession getInstance() {
        if (instance == null) {
            instance = new LoginSession();
        }
        return instance;
    }

    public Person getCurrentPerson() { return currentPerson; }
    public void setCurrentPerson(Person Person) { this.currentPerson = Person; }

    public boolean isAdmin() {
        return currentPerson != null && "ADMIN".equals(currentPerson.getRole());
    }

    public String getRole() {
        return currentPerson != null ? currentPerson.getRole() : "";
    }

    public void logout() {
        currentPerson = null;
    }
}
