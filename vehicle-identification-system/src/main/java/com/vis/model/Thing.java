package com.vis.model;

import java.time.LocalDateTime;

/**
 * Abstract base entity demonstrating INHERITANCE.
 * All model entities extend this class (Polymorphism base).
 */
public abstract class Thing {

    protected int id;
    protected LocalDateTime createdAt;

    public Thing() {}

    public Thing(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Polymorphic method — each entity describes itself */
    public abstract String getSummary();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + "]";
    }
}
