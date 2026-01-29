package com.studenttracker.dao.impl;

import com.studenttracker.dao.RecentActivityDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.RecentActivity;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RecentActivityDAO using SQLite.
 * 
 * <p><b>Implementation Details:</b></p>
 * <ul>
 *   <li>Uses PreparedStatements to prevent SQL injection</li>
 *   <li>Properly handles NULL values for optional fields</li>
 *   <li>Auto-closes connections in finally blocks</li>
 *   <li>Leverages database indexes for performance</li>
 *   <li>Maps ResultSet to RecentActivity objects</li>
 * </ul>
 * 
 * <p><b>Performance Optimizations:</b></p>
 * <ul>
 *   <li>Uses idx_recent_activities_created for ORDER BY created_at DESC</li>
 *   <li>Uses idx_recent_activities_type for WHERE activity_type = ?</li>
 *   <li>Limits result set size with LIMIT clause</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class RecentActivityDAOImpl implements RecentActivityDAO {
    
    /**
     * Database connection instance (singleton).
     */
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    // ==================== CREATE ====================
    
    /**
     * {@inheritDoc}
     * 
     * <p><b>Implementation Notes:</b></p>
     * <ul>
     *   <li>activity_id is auto-generated (PRIMARY KEY AUTOINCREMENT)</li>
     *   <li>created_at is auto-generated (DEFAULT CURRENT_TIMESTAMP)</li>
     *   <li>NULL handling for optional fields: entityType, entityId, performedBy</li>
     * </ul>
     */
    @Override
    public int insert(RecentActivity activity) {
        String sql = "INSERT INTO recent_activities " +
                    "(activity_type, activity_description, entity_type, entity_id, performed_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Set required fields
            pstmt.setString(1, activity.getActivityType());
            pstmt.setString(2, activity.getActivityDescription());
            
            // Set optional field: entityType
            if (activity.getEntityType() != null) {
                pstmt.setString(3, activity.getEntityType());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
            }
            
            // Set optional field: entityId
            if (activity.getEntityId() != null) {
                pstmt.setInt(4, activity.getEntityId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            // Set optional field: performedBy
            if (activity.getPerformedBy() != null) {
                pstmt.setInt(5, activity.getPerformedBy());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            // Execute insert
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert activity failed, no rows affected");
            }
            
            // Get generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert activity failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert recent activity", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // ==================== READ ====================
    
    /**
     * {@inheritDoc}
     * 
     * <p><b>SQL Query:</b></p>
     * <pre>
     * SELECT * FROM recent_activities 
     * ORDER BY created_at DESC 
     * LIMIT ?
     * </pre>
     * 
     * <p><b>Performance:</b> Uses idx_recent_activities_created index</p>
     */
    @Override
    public List<RecentActivity> getRecent(int limit) {
        String sql = "SELECT * FROM recent_activities " +
                    "ORDER BY created_at DESC LIMIT ?";
        
        List<RecentActivity> activities = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
            
            return activities;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to retrieve recent activities", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    /**
     * {@inheritDoc}
     * 
     * <p><b>SQL Query:</b></p>
     * <pre>
     * SELECT * FROM recent_activities 
     * WHERE activity_type = ? 
     * ORDER BY created_at DESC 
     * LIMIT ?
     * </pre>
     * 
     * <p><b>Performance:</b> Uses idx_recent_activities_type index</p>
     */
    @Override
    public List<RecentActivity> getByType(String activityType, int limit) {
        String sql = "SELECT * FROM recent_activities " +
                    "WHERE activity_type = ? " +
                    "ORDER BY created_at DESC LIMIT ?";
        
        List<RecentActivity> activities = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, activityType);
            pstmt.setInt(2, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
            
            return activities;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to retrieve activities by type: " + activityType, e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // ==================== DELETE ====================
    
    /**
     * {@inheritDoc}
     * 
     * <p><b>SQL Query:</b></p>
     * <pre>
     * DELETE FROM recent_activities 
     * WHERE created_at < datetime('now', '-' || ? || ' days')
     * </pre>
     * 
     * <p><b>Example:</b> deleteOlderThan(30) deletes activities from before 30 days ago</p>
     */
    @Override
    public int deleteOlderThan(int days) {
        String sql = "DELETE FROM recent_activities " +
                    "WHERE created_at < datetime('now', '-' || ? || ' days')";
        
        Connection conn = null;
        
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, days);
            
            int deletedCount = pstmt.executeUpdate();
            return deletedCount;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete old activities", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Maps a ResultSet row to a RecentActivity object.
     * Handles NULL values properly for optional fields.
     * 
     * <p><b>Field Mapping:</b></p>
     * <ul>
     *   <li>activity_id → activityId (Integer)</li>
     *   <li>activity_type → activityType (String)</li>
     *   <li>activity_description → activityDescription (String)</li>
     *   <li>entity_type → entityType (String, nullable)</li>
     *   <li>entity_id → entityId (Integer, nullable)</li>
     *   <li>performed_by → performedBy (Integer, nullable)</li>
     *   <li>created_at → createdAt (LocalDateTime)</li>
     * </ul>
     * 
     * @param rs ResultSet positioned at a valid row
     * @return RecentActivity object with all fields populated
     * @throws SQLException if column doesn't exist or type mismatch
     */
    private RecentActivity mapResultSetToActivity(ResultSet rs) throws SQLException {
        RecentActivity activity = new RecentActivity();
        
        // Required fields
        activity.setActivityId(rs.getInt("activity_id"));
        activity.setActivityType(rs.getString("activity_type"));
        activity.setActivityDescription(rs.getString("activity_description"));
        
        // Optional field: entityType
        String entityType = rs.getString("entity_type");
        activity.setEntityType(entityType); // Can be null, that's fine
        
        // Optional field: entityId
        int entityId = rs.getInt("entity_id");
        if (!rs.wasNull()) {
            activity.setEntityId(entityId);
        }
        // else: remains null (default)
        
        // Optional field: performedBy
        int performedBy = rs.getInt("performed_by");
        if (!rs.wasNull()) {
            activity.setPerformedBy(performedBy);
        }
        // else: remains null (default)
        
        // Timestamp field: created_at
        // SQLite stores timestamps as TEXT in ISO 8601 format
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            // Parse SQLite timestamp format
            // Format: "YYYY-MM-DD HH:MM:SS" or "YYYY-MM-DDTHH:MM:SS"
            LocalDateTime createdAt = LocalDateTime.parse(
                createdAtStr.replace(" ", "T") // Convert to ISO format if needed
            );
            activity.setCreatedAt(createdAt);
        }
        
        return activity;
    }
}