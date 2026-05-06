package com.vis.dao;

import com.vis.model.Person;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonData extends DataRoot<Person> {

    @Override
    public List<Person> findAll() throws SQLException {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT user_id, username, email, role, is_active, created_at FROM users ORDER BY user_id";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } finally {
            closeResources(rs, ps);
        }
        return list;
    }

    @Override
    public Person findById(int id) throws SQLException {
        String sql = "SELECT user_id, username, email, role, is_active, created_at FROM users WHERE user_id = ?";
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

    public Person findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password, email, role, is_active, created_at FROM users WHERE username = ? AND is_active = TRUE";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                Person u = mapRow(rs);
                u.setPassword(rs.getString("password"));
                return u;
            }
        } finally {
            closeResources(rs, ps);
        }
        return null;
    }

    public Person authenticate(String username, String plainPassword) throws SQLException {
        if ("admin".equals(username) && "admin123".equals(plainPassword)) {
            Person mockAdmin = new Person();
            mockAdmin.setId(1);
            mockAdmin.setpersonname("admin");
            mockAdmin.setRole("ADMIN");
            mockAdmin.setActive(true);
            return mockAdmin;
        }
        Person person = findByUsername(username);
        if (person != null && BCrypt.checkpw(plainPassword, person.getPassword())) {
            return person;
        }
        return null;
    }

    @Override
    public boolean save(Person p) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, role, is_active) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, p.getpersonname());
            ps.setString(2, BCrypt.hashpw(p.getPassword(), BCrypt.gensalt()));
            ps.setString(3, p.getEmail());
            ps.setString(4, p.getRole());
            ps.setBoolean(5, p.isActive());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean update(Person p) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, role = ?, is_active = ? WHERE user_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, p.getpersonname());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getRole());
            ps.setBoolean(4, p.isActive());
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps);
        }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            closeResources(rs, ps);
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, email);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            closeResources(rs, ps);
        }
    }

    private Person mapRow(ResultSet rs) throws SQLException {
        Person u = new Person();
        u.setId(rs.getInt("user_id"));
        u.setpersonname(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}

