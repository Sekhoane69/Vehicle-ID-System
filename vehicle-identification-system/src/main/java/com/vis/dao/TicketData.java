package com.vis.dao;

import com.vis.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TicketData extends DataRoot<Ticket> {

    private static final String SELECT_BASE =
        "SELECT vi.violation_id, vi.vehicle_id, v.registration_number, vi.violation_date, " +
        "vi.violation_type, vi.fine_amount, vi.status, vi.officer_name, vi.location, " +
        "vi.payment_due_date, vi.description, vi.created_at " +
        "FROM violations vi JOIN vehicles v ON vi.vehicle_id = v.vehicle_id ";

    @Override
    public List<Ticket> findAll() throws SQLException {
        List<Ticket> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "ORDER BY vi.violation_date DESC");
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<Ticket> findByCarId(int vehicleId) throws SQLException {
        List<Ticket> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "WHERE vi.vehicle_id = ? ORDER BY vi.violation_date DESC");
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Ticket findById(int id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "WHERE vi.violation_id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    @Override
    public boolean save(Ticket vi) throws SQLException {
        String sql = "INSERT INTO violations (vehicle_id, violation_date, violation_type, fine_amount, status, officer_name, location, payment_due_date, description) VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, vi.getVehicleId());
            ps.setDate(2, Date.valueOf(vi.getTicketDate()));
            ps.setString(3, vi.getTicketType());
            ps.setDouble(4, vi.getFineAmount());
            ps.setString(5, vi.getStatus() != null ? vi.getStatus() : "UNPAID");
            ps.setString(6, vi.getOfficerName());
            ps.setString(7, vi.getLocation());
            if (vi.getPaymentDueDate() != null) ps.setDate(8, Date.valueOf(vi.getPaymentDueDate()));
            else ps.setNull(8, java.sql.Types.DATE);
            ps.setString(9, vi.getDescription());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(Ticket vi) throws SQLException {
        String sql = "UPDATE violations SET violation_date=?, violation_type=?, fine_amount=?, status=?, officer_name=?, location=?, payment_due_date=?, description=? WHERE violation_id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setDate(1, Date.valueOf(vi.getTicketDate()));
            ps.setString(2, vi.getTicketType());
            ps.setDouble(3, vi.getFineAmount());
            ps.setString(4, vi.getStatus());
            ps.setString(5, vi.getOfficerName());
            ps.setString(6, vi.getLocation());
            if (vi.getPaymentDueDate() != null) ps.setDate(7, Date.valueOf(vi.getPaymentDueDate()));
            else ps.setNull(7, java.sql.Types.DATE);
            ps.setString(8, vi.getDescription());
            ps.setInt(9, vi.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM violations WHERE violation_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    public Map<String, Double> finesByType() throws SQLException {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT violation_type, SUM(fine_amount) total FROM violations GROUP BY violation_type ORDER BY total DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("violation_type"), rs.getDouble("total"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    public Map<String, Integer> countByStatus() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) cnt FROM violations GROUP BY status";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("status"), rs.getInt("cnt"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket vi = new Ticket();
        vi.setId(rs.getInt("violation_id"));
        vi.setVehicleId(rs.getInt("vehicle_id"));
        vi.setRegistrationNumber(rs.getString("registration_number"));
        vi.setTicketDate(rs.getDate("violation_date").toLocalDate());
        vi.setTicketType(rs.getString("violation_type"));
        vi.setFineAmount(rs.getDouble("fine_amount"));
        vi.setStatus(rs.getString("status"));
        vi.setOfficerName(rs.getString("officer_name"));
        vi.setLocation(rs.getString("location"));
        Date due = rs.getDate("payment_due_date");
        if (due != null) vi.setPaymentDueDate(due.toLocalDate());
        vi.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) vi.setCreatedAt(ts.toLocalDateTime());
        return vi;
    }

    public boolean updateStatus(int violationId, String status) throws SQLException {
        String sql = "UPDATE violations SET status = ? WHERE violation_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, violationId);
            return ps.executeUpdate() > 0;
        }
    }
}
