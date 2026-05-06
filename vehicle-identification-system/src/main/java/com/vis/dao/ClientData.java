package com.vis.dao;

import com.vis.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientData extends DataRoot<Client> {

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, address, phone, email, created_at FROM customers ORDER BY customer_id";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Client findById(int id) throws SQLException {
        String sql = "SELECT customer_id, name, address, phone, email, created_at FROM customers WHERE customer_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    public Client findByEmail(String email) throws SQLException {
        String sql = "SELECT customer_id, name, address, phone, email, created_at FROM customers WHERE email = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    @Override
    public boolean save(Client c) throws SQLException {
        String sql = "INSERT INTO customers (name, address, phone, email) VALUES (?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(Client c) throws SQLException {
        String sql = "UPDATE customers SET name=?, address=?, phone=?, email=? WHERE customer_id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setAddress(rs.getString("address"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        return c;
    }

    public boolean addAlert(int customerId, String message) throws SQLException {
        String sql = "INSERT INTO customer_queries (customer_id, query_date, query_text, status) VALUES (?, CURRENT_DATE, ?, 'POLICE_FEEDBACK')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;
        }
    }
}
