package com.vis.dao;

import com.vis.model.Car;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarData extends DataRoot<Car> {

    @Override
    public List<Car> findAll() throws SQLException {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.color, v.status, " +
                     "v.owner_id, c.name AS owner_name, c.phone AS owner_phone, c.email AS owner_email, v.created_at " +
                     "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id ORDER BY v.vehicle_id";
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
    public Car findById(int id) throws SQLException {
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.color, v.status, " +
                     "v.owner_id, c.name AS owner_name, c.phone AS owner_phone, c.email AS owner_email, v.created_at " +
                     "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id WHERE v.vehicle_id = ?";
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

    public Car findByRegistration(String regNumber) throws SQLException {
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.color, v.status, " +
                     "v.owner_id, c.name AS owner_name, c.phone AS owner_phone, c.email AS owner_email, v.created_at " +
                     "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                     "WHERE v.registration_number = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, regNumber);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    public List<Car> findByOwnerId(int ownerId) throws SQLException {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.color, v.status, " +
                     "v.owner_id, c.name AS owner_name, c.phone AS owner_phone, c.email AS owner_email, v.created_at " +
                     "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                     "WHERE v.owner_id = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, ownerId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    public List<Car> search(String keyword) throws SQLException {
        List<Car> list = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.color, v.status, " +
                     "v.owner_id, c.name AS owner_name, c.phone AS owner_phone, c.email AS owner_email, v.created_at " +
                     "FROM vehicles v LEFT JOIN customers c ON v.owner_id = c.customer_id " +
                     "WHERE LOWER(v.registration_number) LIKE ? OR LOWER(v.make) LIKE ? OR LOWER(v.model) LIKE ? OR LOWER(c.name) LIKE ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String kw = "%" + keyword.toLowerCase() + "%";
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public boolean save(Car v) throws SQLException {
        String sql = "INSERT INTO vehicles (registration_number, make, model, year, color, owner_id, status) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, v.getRegistrationNumber());
            ps.setString(2, v.getMake());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getYear());
            ps.setString(5, v.getColor());
            if (v.getOwnerId() > 0) ps.setInt(6, v.getOwnerId());
            else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, v.getStatus() != null ? v.getStatus() : "ACTIVE");
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(Car v) throws SQLException {
        String sql = "UPDATE vehicles SET registration_number=?, make=?, model=?, year=?, color=?, owner_id=?, status=? WHERE vehicle_id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, v.getRegistrationNumber());
            ps.setString(2, v.getMake());
            ps.setString(3, v.getModel());
            ps.setInt(4, v.getYear());
            ps.setString(5, v.getColor());
            if (v.getOwnerId() > 0) ps.setInt(6, v.getOwnerId());
            else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, v.getStatus());
            ps.setInt(8, v.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    public java.util.Map<String, Integer> countByMake() throws SQLException {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT make, COUNT(*) AS cnt FROM vehicles GROUP BY make ORDER BY cnt DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("make"), rs.getInt("cnt"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    public java.util.Map<String, Integer> countByYear() throws SQLException {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT year::TEXT, COUNT(*) AS cnt FROM vehicles GROUP BY year ORDER BY year";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("year"), rs.getInt("cnt"));
        } finally {
            closeResources(rs, ps);
        }
        return map;
    }

    private Car mapRow(ResultSet rs) throws SQLException {
        Car v = new Car();
        v.setId(rs.getInt("vehicle_id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setMake(rs.getString("make"));
        v.setModel(rs.getString("model"));
        v.setYear(rs.getInt("year"));
        v.setColor(rs.getString("color"));
        v.setStatus(rs.getString("status"));
        v.setOwnerId(rs.getInt("owner_id"));
        v.setOwnerName(rs.getString("owner_name"));
        v.setOwnerPhone(rs.getString("owner_phone"));
        v.setOwnerEmail(rs.getString("owner_email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) v.setCreatedAt(ts.toLocalDateTime());
        return v;
    }

    public boolean createTransferOffer(int vehicleId, int buyerId, int sellerId) throws SQLException {
        String sql = "INSERT INTO customer_queries (customer_id, vehicle_id, query_date, query_text, status) VALUES (?, ?, CURRENT_DATE, ?, 'PENDING')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (buyerId > 0) {
                ps.setInt(1, buyerId);
                ps.setInt(2, vehicleId);
                ps.setString(3, "TRANSFER_OFFER_FROM_" + sellerId);
            } else {
                ps.setInt(1, sellerId); // store seller as the customer for public sale
                ps.setInt(2, vehicleId);
                ps.setString(3, "PUBLIC_SALE");
            }
            return ps.executeUpdate() > 0;
        }
    }

    public List<com.vis.model.Question> getIncomingOffers(int buyerId) throws SQLException {
        List<com.vis.model.Question> list = new ArrayList<>();
        String sql = "SELECT q.query_id, q.customer_id, q.vehicle_id, q.query_date, q.query_text, q.status, " +
                     "v.registration_number, v.make, v.model " +
                     "FROM customer_queries q " +
                     "LEFT JOIN vehicles v ON q.vehicle_id = v.vehicle_id " +
                     "WHERE (q.customer_id = ? AND q.query_text LIKE 'TRANSFER_OFFER_FROM_%' AND q.status = 'PENDING') " +
                     "   OR (q.customer_id != ? AND q.query_text = 'PUBLIC_SALE' AND q.status = 'PENDING')";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.vis.model.Question cq = new com.vis.model.Question();
                    cq.setId(rs.getInt("query_id"));
                    cq.setCustomerId(rs.getInt("customer_id"));
                    cq.setVehicleId(rs.getInt("vehicle_id"));
                    java.sql.Date d = rs.getDate("query_date");
                    if (d != null) cq.setQueryDate(d.toLocalDate());
                    cq.setQueryText(rs.getString("query_text"));
                    cq.setStatus(rs.getString("status"));
                    String reg = rs.getString("registration_number");
                    String make = rs.getString("make");
                    String model = rs.getString("model");
                    cq.setRegistrationNumber(reg + " (" + make + " " + model + ")");
                    list.add(cq);
                }
            }
        }
        return list;
    }

    public boolean acceptTransferOffer(int queryId, int vehicleId, int buyerId) throws SQLException {
        boolean success = false;
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            // 1. Update vehicle owner
            String sql1 = "UPDATE vehicles SET owner_id = ? WHERE vehicle_id = ?";
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setInt(1, buyerId);
                ps1.setInt(2, vehicleId);
                ps1.executeUpdate();
            }
            // 2. Update query status
            String sql2 = "UPDATE customer_queries SET status = 'CLOSED', response_text = 'ACCEPTED' WHERE query_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setInt(1, queryId);
                ps2.executeUpdate();
            }
            conn.commit();
            success = true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        return success;
    }

    public boolean updateStatus(int vehicleId, String status) throws SQLException {
        String sql = "UPDATE vehicles SET status = ? WHERE vehicle_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, vehicleId);
            return ps.executeUpdate() > 0;
        }
    }
}
