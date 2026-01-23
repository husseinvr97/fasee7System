package com.studenttracker.dao.impl;

import com.studenttracker.dao.MissionDAO;
import com.studenttracker.dao.impl.helpers.MissionDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionStatus;
import com.studenttracker.model.Mission.MissionType;
import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MissionDAOImpl implements MissionDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = MissionDAOImplHelpers.getTransformers();
    
    // ========== Standard CRUD Methods ==========
    
    @Override
    public Integer insert(Mission mission) {
        String sql = "INSERT INTO missions (lesson_id, mission_type, assigned_to, assigned_by, " +
                    "assigned_at, status, completed_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, mission.getLessonId());
            pstmt.setString(2, mission.getMissionType().name());
            pstmt.setInt(3, mission.getAssignedTo());
            pstmt.setInt(4, mission.getAssignedBy());
            pstmt.setString(5, mission.getAssignedAt() != null ? mission.getAssignedAt().toString() : null);
            pstmt.setString(6, mission.getStatus().name());
            pstmt.setString(7, mission.getCompletedAt() != null ? mission.getCompletedAt().toString() : null);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert mission failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert mission failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert mission", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Mission mission) {
        String sql = "UPDATE missions SET lesson_id = ?, mission_type = ?, assigned_to = ?, " +
                    "assigned_by = ?, assigned_at = ?, status = ?, completed_at = ? WHERE mission_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, mission.getLessonId());
            pstmt.setString(2, mission.getMissionType().name());
            pstmt.setInt(3, mission.getAssignedTo());
            pstmt.setInt(4, mission.getAssignedBy());
            pstmt.setString(5, mission.getAssignedAt() != null ? mission.getAssignedAt().toString() : null);
            pstmt.setString(6, mission.getStatus().name());
            pstmt.setString(7, mission.getCompletedAt() != null ? mission.getCompletedAt().toString() : null);
            pstmt.setInt(8, mission.getMissionId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update mission", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int missionId) {
        String sql = "DELETE FROM missions WHERE mission_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, missionId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete mission", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Mission findById(int missionId) {
        String sql = "SELECT * FROM missions WHERE mission_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, missionId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find mission by ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Mission> findAll() {
        String sql = "SELECT * FROM missions ORDER BY assigned_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Mission> missions = new ArrayList<>();
            while (rs.next()) {
                missions.add(ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers));
            }
            return missions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all missions", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    // ========== Custom Query Methods ==========
    
    @Override
    public List<Mission> findByLessonId(int lessonId) {
        String sql = "SELECT * FROM missions WHERE lesson_id = ? ORDER BY assigned_at ASC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Mission> missions = new ArrayList<>();
            while (rs.next()) {
                missions.add(ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers));
            }
            return missions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find missions by lesson ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Mission> findByAssignedTo(int userId) {
        String sql = "SELECT * FROM missions WHERE assigned_to = ? ORDER BY assigned_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Mission> missions = new ArrayList<>();
            while (rs.next()) {
                missions.add(ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers));
            }
            return missions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find missions by assigned user", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Mission> findByStatus(MissionStatus status) {
        String sql = "SELECT * FROM missions WHERE status = ? ORDER BY assigned_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<Mission> missions = new ArrayList<>();
            while (rs.next()) {
                missions.add(ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers));
            }
            return missions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find missions by status", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public int countByStatus(MissionStatus status) {
        String sql = "SELECT COUNT(*) FROM missions WHERE status = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count missions by status", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Mission findByLessonAndType(int lessonId, MissionType type) {
        String sql = "SELECT * FROM missions WHERE lesson_id = ? AND mission_type = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            pstmt.setString(2, type.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find mission by lesson and type", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Mission> findPendingByUser(int userId) {
        String sql = "SELECT * FROM missions WHERE assigned_to = ? AND status = ? ORDER BY assigned_at ASC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, MissionStatus.IN_PROGRESS.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<Mission> missions = new ArrayList<>();
            while (rs.next()) {
                missions.add(ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers));
            }
            return missions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find pending missions by user", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Mission> findCompletedByDateRange(LocalDate start, LocalDate end) {
        String sql = "SELECT * FROM missions WHERE status = ? AND DATE(completed_at) BETWEEN ? AND ? " +
                    "ORDER BY completed_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, MissionStatus.COMPLETED.name());
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            
            ResultSet rs = pstmt.executeQuery();
            List<Mission> missions = new ArrayList<>();
            while (rs.next()) {
                missions.add(ResultSetExtractor.extractWithTransformers(rs, Mission.class, transformers));
            }
            return missions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find completed missions by date range", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
}