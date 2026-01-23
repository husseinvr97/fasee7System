package com.studenttracker.dao.impl;

import com.studenttracker.dao.BehavioralIncidentDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BehavioralIncidentDAOImpl implements BehavioralIncidentDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(BehavioralIncident incident) throws DAOException {
        String sql = "INSERT INTO behavioral_incidents (student_id, lesson_id, incident_type, " +
                    "notes, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, incident.getStudentId());
            pstmt.setInt(2, incident.getLessonId());
            pstmt.setString(3, incident.getIncidentType().name());
            pstmt.setString(4, incident.getNotes());
            pstmt.setString(5, incident.getCreatedAt() != null ? 
                           incident.getCreatedAt().toString() : LocalDateTime.now().toString());
            pstmt.setInt(6, incident.getCreatedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert behavioral incident failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert behavioral incident failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert behavioral incident", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(BehavioralIncident incident) throws DAOException {
        String sql = "UPDATE behavioral_incidents SET student_id = ?, lesson_id = ?, " +
                    "incident_type = ?, notes = ?, created_by = ? WHERE incident_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, incident.getStudentId());
            pstmt.setInt(2, incident.getLessonId());
            pstmt.setString(3, incident.getIncidentType().name());
            pstmt.setString(4, incident.getNotes());
            pstmt.setInt(5, incident.getCreatedBy());
            pstmt.setInt(6, incident.getIncidentId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update behavioral incident", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int incidentId) throws DAOException {
        String sql = "DELETE FROM behavioral_incidents WHERE incident_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, incidentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete behavioral incident", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public BehavioralIncident findById(int incidentId) throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents WHERE incident_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, incidentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractIncidentFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find behavioral incident by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<BehavioralIncident> findAll() throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<BehavioralIncident> incidents = new ArrayList<>();
            while (rs.next()) {
                incidents.add(extractIncidentFromResultSet(rs));
            }
            return incidents;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all behavioral incidents", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<BehavioralIncident> findByStudentId(int studentId) throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents WHERE student_id = ? " +
                    "ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<BehavioralIncident> incidents = new ArrayList<>();
            while (rs.next()) {
                incidents.add(extractIncidentFromResultSet(rs));
            }
            return incidents;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find behavioral incidents by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<BehavioralIncident> findByLessonId(int lessonId) throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents WHERE lesson_id = ? " +
                    "ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            List<BehavioralIncident> incidents = new ArrayList<>();
            while (rs.next()) {
                incidents.add(extractIncidentFromResultSet(rs));
            }
            return incidents;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find behavioral incidents by lesson ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<BehavioralIncident> findByType(IncidentType type) throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents WHERE incident_type = ? " +
                    "ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<BehavioralIncident> incidents = new ArrayList<>();
            while (rs.next()) {
                incidents.add(extractIncidentFromResultSet(rs));
            }
            return incidents;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find behavioral incidents by type", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countByStudentId(int studentId) throws DAOException {
        String sql = "SELECT COUNT(*) FROM behavioral_incidents WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count behavioral incidents by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<BehavioralIncident> findByStudentAndDateRange(int studentId, LocalDate start, 
                                                               LocalDate end) throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents WHERE student_id = ? " +
                    "AND DATE(created_at) BETWEEN ? AND ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            
            ResultSet rs = pstmt.executeQuery();
            List<BehavioralIncident> incidents = new ArrayList<>();
            while (rs.next()) {
                incidents.add(extractIncidentFromResultSet(rs));
            }
            return incidents;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find behavioral incidents by student and date range", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<BehavioralIncident> findRecentByStudent(int studentId, int limit) throws DAOException {
        String sql = "SELECT * FROM behavioral_incidents WHERE student_id = ? " +
                    "ORDER BY created_at DESC LIMIT ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, limit);
            
            ResultSet rs = pstmt.executeQuery();
            List<BehavioralIncident> incidents = new ArrayList<>();
            while (rs.next()) {
                incidents.add(extractIncidentFromResultSet(rs));
            }
            return incidents;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find recent behavioral incidents by student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    /**
     * Helper method to extract BehavioralIncident object from ResultSet
     */
    private BehavioralIncident extractIncidentFromResultSet(ResultSet rs) throws SQLException {
        BehavioralIncident incident = new BehavioralIncident();
        incident.setIncidentId(rs.getInt("incident_id"));
        incident.setStudentId(rs.getInt("student_id"));
        incident.setLessonId(rs.getInt("lesson_id"));
        incident.setIncidentType(IncidentType.valueOf(rs.getString("incident_type")));
        incident.setNotes(rs.getString("notes"));
        
        String createdAt = rs.getString("created_at");
        incident.setCreatedAt(createdAt != null ? LocalDateTime.parse(createdAt) : null);
        
        incident.setCreatedBy(rs.getInt("created_by"));
        
        return incident;
    }
}