package com.vis.dao;

import com.vis.model.GarageInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class GarageData extends DataRoot<GarageInfo> {

    @Override
    public List<GarageInfo> findAll() throws SQLException {
        List<GarageInfo> list = new ArrayList<>();
        String sql = "SELECT s.service_id, s.vehicle_id, v.registration_number, s.service_date, " +
                     "s.service_type, s.description, s.cost, s.technician, s.status, s.created_at " +
                     "FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id ORDER BY s.service_date DESC";
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

    public List<GarageInfo> findByCarId(int vehicleId) throws SQLException {
        List<GarageInfo> list = new ArrayList<>();
        String sql = "SELECT s.service_id, s.vehicle_id, v.registration_number, s.service_date, " +
                     "s.service_type, s.description, s.cost, s.technician, s.status, s.created_at " +
                     "FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id " +
                     "WHERE s.vehicle_id = ? ORDER BY s.service_date DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public GarageInfo findById(int id) throws SQLException {
        String sql = "SELECT s.service_id, s.vehicle_id, v.registration_number, s.service_date, " +
                     "s.service_type, s.description, s.cost, s.technician, s.status, s.created_at " +
                     "FROM service_records s JOIN vehicles v ON s.vehicle_id = v.vehicle_id WHERE s.service_id = ?";
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

    @Override
    public boolean save(GarageInfo s) throws SQLException {
        String sql = "INSERT INTO service_records (vehicle_id, service_date, service_type, description, cost, technician, status) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, s.getVehicleId());
            ps.setDate(2, Date.valueOf(s.getServiceDate()));
            ps.setString(3, s.getServiceType());
            ps.setString(4, s.getDescription());
            ps.setDouble(5, s.getCost());
            ps.setString(6, s.getTechnician());
            ps.setString(7, s.getStatus() != null ? s.getStatus() : "PENDING");
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(GarageInfo s) throws SQLException {
        String sql = "UPDATE service_records SET service_date=?, service_type=?, description=?, cost=?, technician=?, status=? WHERE service_id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setDate(1, Date.valueOf(s.getServiceDate()));
            ps.setString(2, s.getServiceType());
            ps.setString(3, s.getDescription());
            ps.setDouble(4, s.getCost());
            ps.setString(5, s.getTechnician());
            ps.setString(6, s.getStatus());
            ps.setInt(7, s.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM service_records WHERE service_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    public Map<String, Long> countByServiceType() throws SQLException {
        Map<String, Long> map = new LinkedHashMap<>();
        String sql = "SELECT service_type, COUNT(*) AS cnt FROM service_records GROUP BY service_type ORDER BY cnt DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("service_type"), rs.getLong("cnt"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    public Map<String, Double> costByMonth() throws SQLException {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT TO_CHAR(service_date, 'Mon YYYY') AS month, SUM(cost) AS total " +
                     "FROM service_records GROUP BY TO_CHAR(service_date, 'Mon YYYY'), DATE_TRUNC('month', service_date) " +
                     "ORDER BY DATE_TRUNC('month', service_date)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("month"), rs.getDouble("total"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    public int countActiveWorkshop() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT vehicle_id) FROM service_records WHERE status IN ('PENDING', 'IN_PROGRESS')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countCompletedService() throws SQLException {
        String sql = "SELECT COUNT(*) FROM service_records WHERE status = 'COMPLETED'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private GarageInfo mapRow(ResultSet rs) throws SQLException {
        GarageInfo s = new GarageInfo();
        s.setId(rs.getInt("service_id"));
        s.setVehicleId(rs.getInt("vehicle_id"));
        s.setRegistrationNumber(rs.getString("registration_number"));
        s.setServiceDate(rs.getDate("service_date").toLocalDate());
        s.setServiceType(rs.getString("service_type"));
        s.setDescription(rs.getString("description"));
        s.setCost(rs.getDouble("cost"));
        s.setTechnician(rs.getString("technician"));
        s.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
        return s;
    }
}
