package com.vis.dao;

import com.vis.model.PoliceInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PoliceData extends DataRoot<PoliceInfo> {

    private static final String SELECT_BASE =
        "SELECT p.report_id, p.vehicle_id, v.registration_number, p.report_date, " +
        "p.report_type, p.description, p.officer_name, p.badge_number, p.status, p.created_at " +
        "FROM police_reports p JOIN vehicles v ON p.vehicle_id = v.vehicle_id ";

    @Override
    public List<PoliceInfo> findAll() throws SQLException {
        List<PoliceInfo> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "ORDER BY p.report_date DESC");
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<PoliceInfo> findByCarId(int vehicleId) throws SQLException {
        List<PoliceInfo> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "WHERE p.vehicle_id = ? ORDER BY p.report_date DESC");
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public PoliceInfo findById(int id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "WHERE p.report_id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    @Override
    public boolean save(PoliceInfo r) throws SQLException {
        String sql = "INSERT INTO police_reports (vehicle_id, report_date, report_type, description, officer_name, badge_number, status) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, r.getVehicleId());
            ps.setDate(2, Date.valueOf(r.getReportDate()));
            ps.setString(3, r.getReportType());
            ps.setString(4, r.getDescription());
            ps.setString(5, r.getOfficerName());
            ps.setString(6, r.getBadgeNumber());
            ps.setString(7, r.getStatus() != null ? r.getStatus() : "OPEN");
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(PoliceInfo r) throws SQLException {
        String sql = "UPDATE police_reports SET report_date=?, report_type=?, description=?, officer_name=?, badge_number=?, status=? WHERE report_id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setDate(1, Date.valueOf(r.getReportDate()));
            ps.setString(2, r.getReportType());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getOfficerName());
            ps.setString(5, r.getBadgeNumber());
            ps.setString(6, r.getStatus());
            ps.setInt(7, r.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM police_reports WHERE report_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    public Map<String, Integer> countByType() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT report_type, COUNT(*) cnt FROM police_reports GROUP BY report_type";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("report_type"), rs.getInt("cnt"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    private PoliceInfo mapRow(ResultSet rs) throws SQLException {
        PoliceInfo r = new PoliceInfo();
        r.setId(rs.getInt("report_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setRegistrationNumber(rs.getString("registration_number"));
        r.setReportDate(rs.getDate("report_date").toLocalDate());
        r.setReportType(rs.getString("report_type"));
        r.setDescription(rs.getString("description"));
        r.setOfficerName(rs.getString("officer_name"));
        r.setBadgeNumber(rs.getString("badge_number"));
        r.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
