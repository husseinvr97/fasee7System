package com.studenttracker.dao.impl;

import com.studenttracker.dao.TargetDAO;
import com.studenttracker.dao.impl.helpers.TargetDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Target;
import static com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TargetDAOImpl implements TargetDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = TargetDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(Target target) {
        String sql = "INSERT INTO targets (student_id, category, target_pi_value, created_at, is_achieved, achieved_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, target.getStudentId());
            pstmt.setString(2, target.getCategory().name());
            pstmt.setInt(3, target.getTargetPiValue());
            pstmt.setString(4, target.getCreatedAt().toString());
            pstmt.setInt(5, target.isAchieved() ? 1 : 0);
            pstmt.setString(6, target.getAchievedAt() != null ? target.getAchievedAt().toString() : null);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert target failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert target failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert target", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Target target) {
        String sql = "UPDATE targets SET student_id = ?, category = ?, target_pi_value = ?, " +
                    "created_at = ?, is_achieved = ?, achieved_at = ? WHERE target_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, target.getStudentId());
            pstmt.setString(2, target.getCategory().name());
            pstmt.setInt(3, target.getTargetPiValue());
            pstmt.setString(4, target.getCreatedAt().toString());
            pstmt.setInt(5, target.isAchieved() ? 1 : 0);
            pstmt.setString(6, target.getAchievedAt() != null ? target.getAchievedAt().toString() : null);
            pstmt.setInt(7, target.getTargetId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update target", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int targetId) {
        String sql = "DELETE FROM targets WHERE target_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, targetId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete target", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Target findById(int targetId) {
        String sql = "SELECT * FROM targets WHERE target_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, targetId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return TargetDAOImplHelpers.extractTargetFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find target by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Target> findAll() {
        String sql = "SELECT * FROM targets ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Target> targets = new ArrayList<>();
            while (rs.next()) {
                targets.add(TargetDAOImplHelpers.extractTargetFromResultSet(rs, transformers));
            }
            return targets;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all targets", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Target> findByStudentId(int studentId) {
        String sql = "SELECT * FROM targets WHERE student_id = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Target> targets = new ArrayList<>();
            while (rs.next()) {
                targets.add(TargetDAOImplHelpers.extractTargetFromResultSet(rs, transformers));
            }
            return targets;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find targets by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Target> findActiveByStudent(int studentId) {
        String sql = "SELECT * FROM targets WHERE student_id = ? AND is_achieved = 0 ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Target> targets = new ArrayList<>();
            while (rs.next()) {
                targets.add(TargetDAOImplHelpers.extractTargetFromResultSet(rs, transformers));
            }
            return targets;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find active targets by student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Target> findAchievedByStudent(int studentId) {
        String sql = "SELECT * FROM targets WHERE student_id = ? AND is_achieved = 1 ORDER BY achieved_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Target> targets = new ArrayList<>();
            while (rs.next()) {
                targets.add(TargetDAOImplHelpers.extractTargetFromResultSet(rs, transformers));
            }
            return targets;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find achieved targets by student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Target> findByStudentAndCategory(int studentId, TopicCategory category) {
        String sql = "SELECT * FROM targets WHERE student_id = ? AND category = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, category.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<Target> targets = new ArrayList<>();
            while (rs.next()) {
                targets.add(TargetDAOImplHelpers.extractTargetFromResultSet(rs, transformers));
            }
            return targets;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find targets by student and category", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countActiveByStudent(int studentId) {
        String sql = "SELECT COUNT(*) FROM targets WHERE student_id = ? AND is_achieved = 0";
        
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
            throw new DAOException("Failed to count active targets by student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countAchievedByStudent(int studentId) {
        String sql = "SELECT COUNT(*) FROM targets WHERE student_id = ? AND is_achieved = 1";
        
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
            throw new DAOException("Failed to count achieved targets by student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean hasActiveTarget(int studentId, TopicCategory category, int targetValue) {
        String sql = "SELECT COUNT(*) FROM targets WHERE student_id = ? AND category = ? " +
                    "AND target_pi_value = ? AND is_achieved = 0";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, category.name());
            pstmt.setInt(3, targetValue);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to check if active target exists", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}