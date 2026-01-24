package com.studenttracker.dao.impl;

import com.studenttracker.dao.NotificationDAO;
import com.studenttracker.dao.impl.helpers.NotificationDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Notification;
import com.studenttracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class NotificationDAOImpl implements NotificationDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = NotificationDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, notification_type, message, is_read) " +
                    "VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getNotificationType());
            pstmt.setString(3, notification.getMessage());
            pstmt.setInt(4, notification.isRead() ? 1 : 0);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert notification failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert notification failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert notification", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Notification notification) {
        String sql = "UPDATE notifications SET user_id = ?, notification_type = ?, " +
                    "message = ?, is_read = ? WHERE notification_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, notification.getUserId());
            pstmt.setString(2, notification.getNotificationType());
            pstmt.setString(3, notification.getMessage());
            pstmt.setInt(4, notification.isRead() ? 1 : 0);
            pstmt.setInt(5, notification.getNotificationId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update notification", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, notificationId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete notification", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Notification findById(int notificationId) {
        String sql = "SELECT * FROM notifications WHERE notification_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, notificationId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return NotificationDAOImplHelpers.extractNotificationFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find notification by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Notification> findAll() {
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Notification> notifications = new ArrayList<>();
            while (rs.next()) {
                notifications.add(NotificationDAOImplHelpers.extractNotificationFromResultSet(rs, transformers));
            }
            return notifications;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all notifications", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Notification> findByUserId(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Notification> notifications = new ArrayList<>();
            while (rs.next()) {
                notifications.add(NotificationDAOImplHelpers.extractNotificationFromResultSet(rs, transformers));
            }
            return notifications;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find notifications by user ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Notification> findUnreadByUser(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 " +
                    "ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Notification> notifications = new ArrayList<>();
            while (rs.next()) {
                notifications.add(NotificationDAOImplHelpers.extractNotificationFromResultSet(rs, transformers));
            }
            return notifications;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find unread notifications by user", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countUnreadByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count unread notifications", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, notificationId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to mark notification as read", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean markAllAsReadForUser(int userId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to mark all notifications as read", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Notification> findByUserAndType(int userId, String notificationType) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND notification_type = ? " +
                    "ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, notificationType);
            
            ResultSet rs = pstmt.executeQuery();
            List<Notification> notifications = new ArrayList<>();
            while (rs.next()) {
                notifications.add(NotificationDAOImplHelpers.extractNotificationFromResultSet(rs, transformers));
            }
            return notifications;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find notifications by user and type", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean deleteOldNotifications(LocalDateTime before) {
        String sql = "DELETE FROM notifications WHERE created_at < ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, before.toString());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete old notifications", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}