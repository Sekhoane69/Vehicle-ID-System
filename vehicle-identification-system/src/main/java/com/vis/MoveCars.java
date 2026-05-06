package com.vis;

import com.vis.dao.ConnectDB;
import java.sql.Connection;
import java.sql.Statement;

public class MoveCars {
    public static void main(String[] args) {
        try {
            Connection conn = ConnectDB.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            System.out.println("Migrating database...");
            stmt.execute("ALTER TABLE Cars ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';");
            System.out.println("Migration successful: Added status column to Cars table.");
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            if (e.getMessage().contains("already exists")) {
                System.out.println("Column already exists, skipping.");
            }
        }
    }
}
