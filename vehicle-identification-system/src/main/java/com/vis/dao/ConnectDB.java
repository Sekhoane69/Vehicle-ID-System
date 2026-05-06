package com.vis.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton database connection manager.
 * Update DB_URL, DB_Person, DB_PASS to match your PostgreSQL configuration.
 */
public class ConnectDB {

    private static final String DB_URL  = "jdbc:postgresql://localhost:5432/vehicle_identificationdb";
    private static final String DB_Person = "postgres";
    private static final String DB_PASS = "0000";

    private static ConnectDB instance;
    private Connection connection;

    private ConnectDB() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_Person, DB_PASS);
            System.out.println("[DB] Connected to PostgreSQL successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] PostgreSQL JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
        }
    }

    public static synchronized ConnectDB getInstance() {
        if (instance == null) {
            instance = new ConnectDB();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_Person, DB_PASS);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Reconnect failed: " + e.getMessage());
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Close failed: " + e.getMessage());
        }
    }
}
