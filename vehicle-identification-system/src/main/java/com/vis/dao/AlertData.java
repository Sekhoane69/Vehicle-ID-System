package com.vis.dao;

import com.vis.model.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertData extends DataRoot<Alert> {

    @Override
    public List<Alert> findAll() throws SQLException {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Alert> findLatestBroadcasts(int limit) throws SQLException {
        List<Alert> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE is_broadcast = TRUE " +
                     "AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP) " +
                     "ORDER BY created_at DESC LIMIT ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean save(Alert n) throws SQLException {
        String sql = "INSERT INTO notifications (sender_id, recipient_id, message, type, is_broadcast, expires_at) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, n.getSenderId());
            if (n.getRecipientId() != null) ps.setInt(2, n.getRecipientId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getType() != null ? n.getType() : "INFO");
            ps.setBoolean(5, n.isBroadcast());
            if (n.getExpiresAt() != null) ps.setTimestamp(6, Timestamp.valueOf(n.getExpiresAt())); else ps.setNull(6, Types.TIMESTAMP);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Alert findById(int id) throws SQLException { return null; }
    @Override
    public boolean update(Alert n) throws SQLException { return false; }
    @Override
    public boolean delete(int id) throws SQLException { return false; }

    private Alert mapRow(ResultSet rs) throws SQLException {
        Alert n = new Alert();
        n.setId(rs.getInt("notification_id"));
        n.setSenderId(rs.getInt("sender_id"));
        n.setRecipientId(rs.getInt("recipient_id") == 0 ? null : rs.getInt("recipient_id"));
        n.setMessage(rs.getString("message"));
        n.setType(rs.getString("type"));
        n.setBroadcast(rs.getBoolean("is_broadcast"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp exp = rs.getTimestamp("expires_at");
        if (exp != null) n.setExpiresAt(exp.toLocalDateTime());
        return n;
    }
}
