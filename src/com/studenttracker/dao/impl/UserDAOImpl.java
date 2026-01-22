package com.studenttracker.dao.impl;

import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, created_at, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getCreatedAt().toString());
            pstmt.setBoolean(6, user.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert user failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert user failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert user", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, full_name = ?, " +
                    "role = ?, is_active = ? WHERE user_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setInt(6, user.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update user", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete user", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find user by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY username";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            return users;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all users", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find user by username", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY username";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, role.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            return users;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find users by role", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to check if username exists", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, role.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count users by role", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract User object from ResultSet
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        
        String createdAt = rs.getString("created_at");
        user.setCreatedAt(createdAt != null ? LocalDateTime.parse(createdAt) : null);
        
        user.setActive(rs.getBoolean("is_active"));
        
        return user;
    }
}