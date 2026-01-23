package com.studenttracker.dao.impl;

import com.studenttracker.dao.ConsecutivityTrackingDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.ConsecutivityTracking;
import com.studenttracker.model.ConsecutivityTracking.TrackingType;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsecutivityTrackingDAOImpl implements ConsecutivityTrackingDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(ConsecutivityTracking tracking) throws DAOException {
        String sql = "INSERT INTO consecutivity_tracking (student_id, tracking_type, " +
                    "consecutive_count, last_lesson_id, last_updated) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, tracking.getStudentId());
            pstmt.setString(2, tracking.getTrackingType().name());
            pstmt.setInt(3, tracking.getConsecutiveCount());
            pstmt.setObject(4, tracking.getLastLessonId());
            pstmt.setString(5, tracking.getLastUpdated() != null ? 
                           tracking.getLastUpdated().toString() : LocalDateTime.now().toString());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert consecutivity tracking failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert consecutivity tracking failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert consecutivity tracking", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(ConsecutivityTracking tracking) throws DAOException {
        String sql = "UPDATE consecutivity_tracking SET student_id = ?, tracking_type = ?, " +
                    "consecutive_count = ?, last_lesson_id = ?, last_updated = ? " +
                    "WHERE tracking_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, tracking.getStudentId());
            pstmt.setString(2, tracking.getTrackingType().name());
            pstmt.setInt(3, tracking.getConsecutiveCount());
            pstmt.setObject(4, tracking.getLastLessonId());
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.setInt(6, tracking.getTrackingId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update consecutivity tracking", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int trackingId) throws DAOException {
        String sql = "DELETE FROM consecutivity_tracking WHERE tracking_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trackingId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete consecutivity tracking", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public ConsecutivityTracking findById(int trackingId) throws DAOException {
        String sql = "SELECT * FROM consecutivity_tracking WHERE tracking_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trackingId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractTrackingFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find consecutivity tracking by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<ConsecutivityTracking> findAll() throws DAOException {
        String sql = "SELECT * FROM consecutivity_tracking ORDER BY last_updated DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<ConsecutivityTracking> trackings = new ArrayList<>();
            while (rs.next()) {
                trackings.add(extractTrackingFromResultSet(rs));
            }
            return trackings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all consecutivity trackings", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public ConsecutivityTracking findByStudentAndType(int studentId, TrackingType type) throws DAOException {
        String sql = "SELECT * FROM consecutivity_tracking WHERE student_id = ? AND tracking_type = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, type.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractTrackingFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find consecutivity tracking by student and type", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean upsert(ConsecutivityTracking tracking) throws DAOException {
        // First try to find existing record
        ConsecutivityTracking existing = findByStudentAndType(
            tracking.getStudentId(), 
            tracking.getTrackingType()
        );
        
        if (existing != null) {
            // Update existing record
            tracking.setTrackingId(existing.getTrackingId());
            return update(tracking);
        } else {
            // Insert new record
            Integer id = insert(tracking);
            tracking.setTrackingId(id);
            return id != null;
        }
    }
    
    @Override
    public List<ConsecutivityTracking> findByThreshold(TrackingType type, int minCount) throws DAOException {
        String sql = "SELECT * FROM consecutivity_tracking WHERE tracking_type = ? " +
                    "AND consecutive_count >= ? ORDER BY consecutive_count DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type.name());
            pstmt.setInt(2, minCount);
            
            ResultSet rs = pstmt.executeQuery();
            List<ConsecutivityTracking> trackings = new ArrayList<>();
            while (rs.next()) {
                trackings.add(extractTrackingFromResultSet(rs));
            }
            return trackings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find consecutivity trackings by threshold", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public void resetByStudentId(int studentId) throws DAOException {
        String sql = "UPDATE consecutivity_tracking SET consecutive_count = 0, " +
                    "last_updated = ? WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, studentId);
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new DAOException("Failed to reset consecutivity tracking by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    /**
     * Helper method to extract ConsecutivityTracking object from ResultSet
     */
    private ConsecutivityTracking extractTrackingFromResultSet(ResultSet rs) throws SQLException {
        ConsecutivityTracking tracking = new ConsecutivityTracking();
        tracking.setTrackingId(rs.getInt("tracking_id"));
        tracking.setStudentId(rs.getInt("student_id"));
        tracking.setTrackingType(TrackingType.valueOf(rs.getString("tracking_type")));
        tracking.setConsecutiveCount(rs.getInt("consecutive_count"));
        
        Integer lastLessonId = (Integer) rs.getObject("last_lesson_id");
        tracking.setLastLessonId(lastLessonId);
        
        String lastUpdated = rs.getString("last_updated");
        tracking.setLastUpdated(lastUpdated != null ? LocalDateTime.parse(lastUpdated) : null);
        
        return tracking;
    }
}