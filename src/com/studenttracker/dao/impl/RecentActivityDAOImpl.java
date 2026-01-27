package com.studenttracker.dao.impl;

import com.studenttracker.dao.RecentActivityDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.RecentActivity;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecentActivityDAOImpl implements RecentActivityDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(RecentActivity activity) throws DAOException {
        String sql = "INSERT INTO recent_activities " +
                    "(activity_type, activity_description, entity_type, entity_id, performed_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, activity.getActivityType());
            pstmt.setString(2, activity.getActivityDescription());
            pstmt.setString(3, activity.getEntityType());
            
            if (activity.getEntityId() != null) {
                pstmt.setInt(4, activity.getEntityId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            if (activity.getPerformedBy() != null) {
                pstmt.setInt(5, activity.getPerformedBy());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert activity failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert activity failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert activity", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<RecentActivity> getRecent(int limit) throws DAOException {
        String sql = "SELECT * FROM recent_activities " +
                    "ORDER BY created_at DESC LIMIT ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            
            ResultSet rs = pstmt.executeQuery();
            List<RecentActivity> activities = new ArrayList<>();
            while (rs.next()) {
                activities.add(extractActivityFromResultSet(rs));
            }
            return activities;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get recent activities", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<RecentActivity> getByType(String activityType, int limit) throws DAOException {
        String sql = "SELECT * FROM recent_activities " +
                    "WHERE activity_type = ? " +
                    "ORDER BY created_at DESC LIMIT ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, activityType);
            pstmt.setInt(2, limit);
            
            ResultSet rs = pstmt.executeQuery();
            List<RecentActivity> activities = new ArrayList<>();
            while (rs.next()) {
                activities.add(extractActivityFromResultSet(rs));
            }
            return activities;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get activities by type", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int deleteOlderThan(int days) throws DAOException {
        String sql = "DELETE FROM recent_activities " +
                    "WHERE created_at < datetime('now', '-' || ? || ' days')";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete old activities", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract RecentActivity object from ResultSet
    private RecentActivity extractActivityFromResultSet(ResultSet rs) throws SQLException {
        RecentActivity activity = new RecentActivity();
        activity.setActivityId(rs.getInt("activity_id"));
        activity.setActivityType(rs.getString("activity_type"));
        activity.setActivityDescription(rs.getString("activity_description"));
        activity.setEntityType(rs.getString("entity_type"));
        
        Integer entityId = (Integer) rs.getObject("entity_id");
        activity.setEntityId(entityId);
        
        Integer performedBy = (Integer) rs.getObject("performed_by");
        activity.setPerformedBy(performedBy);
        
        String createdAt = rs.getString("created_at");
        activity.setCreatedAt(createdAt != null ? LocalDateTime.parse(createdAt) : null);
        
        return activity;
    }
}