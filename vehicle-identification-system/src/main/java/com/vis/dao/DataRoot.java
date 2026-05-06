package com.vis.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Abstract generic DAO demonstrating INHERITANCE + POLYMORPHISM.
 * All DAO classes extend this base.
 */
public abstract class DataRoot<T> {

    protected Connection getConnection() {
        return ConnectDB.getInstance().getConnection();
    }

    public abstract List<T> findAll() throws SQLException;
    public abstract T findById(int id) throws SQLException;
    public abstract boolean save(T entity) throws SQLException;
    public abstract boolean update(T entity) throws SQLException;
    public abstract boolean delete(int id) throws SQLException;

    protected void closeResources(ResultSet rs, PreparedStatement ps) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        } catch (SQLException e) {
            System.err.println("[DAO] Close resource error: " + e.getMessage());
        }
    }

    public int countAll(String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}
