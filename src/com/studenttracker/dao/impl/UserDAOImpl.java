package com.studenttracker.dao.impl;

import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.User;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    
    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getCreatedAt().toString());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert user failed, no rows affected");
            }
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DAOException("Insert user failed, no ID obtained");
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to insert user: " + user.getUsername(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, full_name = ?, role = ? " +
                     "WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setInt(5, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to update user ID: " + user.getUserId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to delete user ID: " + userId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find user by ID: " + userId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY full_name";
        Connection conn = null;
        List<User> users = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DAOException("Failed to retrieve all users", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find user by username: " + username, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<User> findByRole(User.UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        Connection conn = null;
        List<User> users = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new DAOException("Failed to find users by role: " + role, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DAOException("Failed to check username existence: " + username, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public int countByRole(User.UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to count users by role: " + role, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return user;
    }
}