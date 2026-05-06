package com.vis.dao;

import com.vis.model.BookingInfo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingData extends DataRoot<BookingInfo> {

    @Override
    public List<BookingInfo> findAll() throws SQLException {
        List<BookingInfo> list = new ArrayList<>();
        String sql = "SELECT r.*, v.registration_number, c.name as owner_name " +
                     "FROM service_requests r " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "JOIN customers c ON v.owner_id = c.customer_id " +
                     "ORDER BY r.created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<BookingInfo> findPending() throws SQLException {
        List<BookingInfo> list = new ArrayList<>();
        String sql = "SELECT r.*, v.registration_number, c.name as owner_name " +
                     "FROM service_requests r " +
                     "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
                     "JOIN customers c ON v.owner_id = c.customer_id " +
                     "WHERE r.status != 'COMPLETED' " +
                     "ORDER BY r.created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean save(BookingInfo r) throws SQLException {
        String sql = "INSERT INTO service_requests (vehicle_id, description, status) VALUES (?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, r.getVehicleId());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatus() != null ? r.getStatus() : "PENDING");
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(BookingInfo r) throws SQLException {
        String sql = "UPDATE service_requests SET status = ?, description = ? WHERE request_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, r.getStatus());
            ps.setString(2, r.getDescription());
            ps.setInt(3, r.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean completeRequest(int vehicleId) throws SQLException {
        String sql = "UPDATE service_requests SET status = 'COMPLETED' WHERE vehicle_id = ? AND status != 'COMPLETED'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public BookingInfo findById(int id) throws SQLException { return null; }
    @Override
    public boolean delete(int id) throws SQLException { return false; }

    private BookingInfo mapRow(ResultSet rs) throws SQLException {
        BookingInfo r = new BookingInfo();
        r.setId(rs.getInt("request_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setRegistrationNumber(rs.getString("registration_number"));
        r.setOwnerName(rs.getString("owner_name"));
        r.setRequestDate(rs.getDate("request_date").toLocalDate());
        r.setDescription(rs.getString("description"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
