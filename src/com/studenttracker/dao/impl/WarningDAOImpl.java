package com.studenttracker.dao.impl;

import com.studenttracker.dao.WarningDAO;
import com.studenttracker.dao.impl.helpers.WarningDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;
import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class WarningDAOImpl implements WarningDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = WarningDAOImplHelpers.getTransformers();
    
    // ========== Standard CRUD Methods ==========
    
    @Override
    public Integer insert(Warning warning) {
        String sql = "INSERT INTO warnings (student_id, warning_type, warning_reason, created_at, is_active, resolved_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, warning.getStudentId());
            pstmt.setString(2, warning.getWarningType().name());
            pstmt.setString(3, warning.getWarningReason());
            pstmt.setString(4, warning.getCreatedAt() != null ? warning.getCreatedAt().toString() : null);
            pstmt.setBoolean(5, warning.isActive());
            pstmt.setString(6, warning.getResolvedAt() != null ? warning.getResolvedAt().toString() : null);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert warning failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert warning failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert warning", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Warning warning) {
        String sql = "UPDATE warnings SET student_id = ?, warning_type = ?, warning_reason = ?, " +
                    "created_at = ?, is_active = ?, resolved_at = ? WHERE warning_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, warning.getStudentId());
            pstmt.setString(2, warning.getWarningType().name());
            pstmt.setString(3, warning.getWarningReason());
            pstmt.setString(4, warning.getCreatedAt() != null ? warning.getCreatedAt().toString() : null);
            pstmt.setBoolean(5, warning.isActive());
            pstmt.setString(6, warning.getResolvedAt() != null ? warning.getResolvedAt().toString() : null);
            pstmt.setInt(7, warning.getWarningId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update warning", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int warningId) {
        String sql = "DELETE FROM warnings WHERE warning_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, warningId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete warning", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Warning findById(int warningId) {
        String sql = "SELECT * FROM warnings WHERE warning_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, warningId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find warning by ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Warning> findAll() {
        String sql = "SELECT * FROM warnings ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Warning> warnings = new ArrayList<>();
            while (rs.next()) {
                warnings.add(ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers));
            }
            return warnings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all warnings", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    // ========== Custom Query Methods ==========
    
    @Override
    public List<Warning> findByStudentId(int studentId) {
        String sql = "SELECT * FROM warnings WHERE student_id = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Warning> warnings = new ArrayList<>();
            while (rs.next()) {
                warnings.add(ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers));
            }
            return warnings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find warnings by student ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Warning> findByActive(boolean isActive) {
        String sql = "SELECT * FROM warnings WHERE is_active = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, isActive);
            
            ResultSet rs = pstmt.executeQuery();
            List<Warning> warnings = new ArrayList<>();
            while (rs.next()) {
                warnings.add(ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers));
            }
            return warnings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find warnings by active status", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Warning> findByStudentAndActive(int studentId, boolean isActive) {
        String sql = "SELECT * FROM warnings WHERE student_id = ? AND is_active = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setBoolean(2, isActive);
            
            ResultSet rs = pstmt.executeQuery();
            List<Warning> warnings = new ArrayList<>();
            while (rs.next()) {
                warnings.add(ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers));
            }
            return warnings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find warnings by student and active status", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<Warning> findByType(WarningType type) {
        String sql = "SELECT * FROM warnings WHERE warning_type = ? ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<Warning> warnings = new ArrayList<>();
            while (rs.next()) {
                warnings.add(ResultSetExtractor.extractWithTransformers(rs, Warning.class, transformers));
            }
            return warnings;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find warnings by type", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public int countActive() {
        String sql = "SELECT COUNT(*) FROM warnings WHERE is_active = 1";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count active warnings", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countByType(WarningType type) {
        String sql = "SELECT COUNT(*) FROM warnings WHERE warning_type = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, type.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count warnings by type", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean resolveWarningsByStudent(int studentId, WarningType type) {
        String sql = "UPDATE warnings SET is_active = 0, resolved_at = CURRENT_TIMESTAMP " +
                    "WHERE student_id = ? AND warning_type = ? AND is_active = 1";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, type.name());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to resolve warnings by student and type", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}