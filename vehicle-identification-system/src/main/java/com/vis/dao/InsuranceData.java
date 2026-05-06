package com.vis.dao;

import com.vis.model.InsuranceInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InsuranceData extends DataRoot<InsuranceInfo> {

    private static final String SELECT_BASE =
        "SELECT i.insurance_id, i.vehicle_id, v.registration_number, i.provider, i.policy_number, " +
        "i.start_date, i.expiry_date, i.premium_amount, i.coverage_type, i.status, i.created_at " +
        "FROM insurance_records i JOIN vehicles v ON i.vehicle_id = v.vehicle_id ";

    @Override
    public List<InsuranceInfo> findAll() throws SQLException {
        List<InsuranceInfo> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "ORDER BY i.insurance_id DESC");
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<InsuranceInfo> findByCarId(int vehicleId) throws SQLException {
        List<InsuranceInfo> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "WHERE i.vehicle_id = ? ORDER BY i.expiry_date DESC");
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public InsuranceInfo findById(int id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(SELECT_BASE + "WHERE i.insurance_id = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    @Override
    public boolean save(InsuranceInfo r) throws SQLException {
        String sql = "INSERT INTO insurance_records (vehicle_id, provider, policy_number, start_date, expiry_date, premium_amount, coverage_type, status) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, r.getVehicleId());
            ps.setString(2, r.getProvider());
            ps.setString(3, r.getPolicyNumber());
            ps.setDate(4, Date.valueOf(r.getStartDate()));
            ps.setDate(5, Date.valueOf(r.getExpiryDate()));
            ps.setDouble(6, r.getPremiumAmount());
            ps.setString(7, r.getCoverageType());
            ps.setString(8, r.getStatus() != null ? r.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(InsuranceInfo r) throws SQLException {
        String sql = "UPDATE insurance_logs SET provider=?, policy_number=?, start_date=?, expiry_date=?, premium_amount=?, coverage_type=?, status=? WHERE insurance_id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, r.getProvider());
            ps.setString(2, r.getPolicyNumber());
            ps.setDate(3, Date.valueOf(r.getStartDate()));
            ps.setDate(4, Date.valueOf(r.getExpiryDate()));
            ps.setDouble(5, r.getPremiumAmount());
            ps.setString(6, r.getCoverageType());
            ps.setString(7, r.getStatus());
            ps.setInt(8, r.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM insurance_records WHERE insurance_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    public Map<String, Integer> countByProvider() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT provider, COUNT(*) cnt FROM insurance_records GROUP BY provider ORDER BY cnt DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("provider"), rs.getInt("cnt"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    public Map<String, Integer> countByStatus() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) cnt FROM insurance_records GROUP BY status";
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

    public int countInsuredCars() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT vehicle_id) FROM insurance_records";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int countInsuredClients() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT v.owner_id) FROM insurance_records i JOIN vehicles v ON i.vehicle_id = v.vehicle_id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private InsuranceInfo mapRow(ResultSet rs) throws SQLException {
        InsuranceInfo r = new InsuranceInfo();
        r.setId(rs.getInt("insurance_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setRegistrationNumber(rs.getString("registration_number"));
        r.setProvider(rs.getString("provider"));
        r.setPolicyNumber(rs.getString("policy_number"));
        r.setStartDate(rs.getDate("start_date").toLocalDate());
        r.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        r.setPremiumAmount(rs.getDouble("premium_amount"));
        r.setCoverageType(rs.getString("coverage_type"));
        r.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
