package com.studenttracker.dao.impl;

import com.studenttracker.dao.Fasee7PointsDAO;
import com.studenttracker.dao.impl.helpers.Fasee7PointsDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Fasee7Points;
import com.studenttracker.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Fasee7PointsDAOImpl implements Fasee7PointsDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = 
        Fasee7PointsDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(Fasee7Points points) {
        String sql = "INSERT INTO fasee7_points (student_id, quiz_points, attendance_points, " +
                    "homework_points, target_points, total_points, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, points.getStudentId());
            pstmt.setBigDecimal(2, points.getQuizPoints());
            pstmt.setInt(3, points.getAttendancePoints());
            pstmt.setInt(4, points.getHomeworkPoints());
            pstmt.setInt(5, points.getTargetPoints());
            pstmt.setBigDecimal(6, points.getTotalPoints());
            pstmt.setString(7, points.getLastUpdated() != null ? points.getLastUpdated().toString() : null);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert fasee7 points failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert fasee7 points failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert fasee7 points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Fasee7Points points) {
        String sql = "UPDATE fasee7_points SET student_id = ?, quiz_points = ?, " +
                    "attendance_points = ?, homework_points = ?, target_points = ?, " +
                    "total_points = ?, last_updated = ? WHERE points_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, points.getStudentId());
            pstmt.setBigDecimal(2, points.getQuizPoints());
            pstmt.setInt(3, points.getAttendancePoints());
            pstmt.setInt(4, points.getHomeworkPoints());
            pstmt.setInt(5, points.getTargetPoints());
            pstmt.setBigDecimal(6, points.getTotalPoints());
            pstmt.setString(7, points.getLastUpdated() != null ? points.getLastUpdated().toString() : null);
            pstmt.setInt(8, points.getPointsId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update fasee7 points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int pointsId) {
        String sql = "DELETE FROM fasee7_points WHERE points_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pointsId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete fasee7 points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Fasee7Points findById(int pointsId) {
        String sql = "SELECT * FROM fasee7_points WHERE points_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pointsId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Fasee7PointsDAOImplHelpers.extractPointsFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find fasee7 points by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Points> findAll() {
        String sql = "SELECT * FROM fasee7_points";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Fasee7Points> pointsList = new ArrayList<>();
            while (rs.next()) {
                pointsList.add(Fasee7PointsDAOImplHelpers.extractPointsFromResultSet(rs, transformers));
            }
            return pointsList;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all fasee7 points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Fasee7Points findByStudentId(int studentId) {
        String sql = "SELECT * FROM fasee7_points WHERE student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Fasee7PointsDAOImplHelpers.extractPointsFromResultSet(rs, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find fasee7 points by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean upsert(Fasee7Points points) {
        String sql = "INSERT INTO fasee7_points (student_id, quiz_points, attendance_points, " +
                    "homework_points, target_points, total_points, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(student_id) DO UPDATE SET " +
                    "quiz_points = excluded.quiz_points, " +
                    "attendance_points = excluded.attendance_points, " +
                    "homework_points = excluded.homework_points, " +
                    "target_points = excluded.target_points, " +
                    "total_points = excluded.total_points, " +
                    "last_updated = excluded.last_updated";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, points.getStudentId());
            pstmt.setBigDecimal(2, points.getQuizPoints());
            pstmt.setInt(3, points.getAttendancePoints());
            pstmt.setInt(4, points.getHomeworkPoints());
            pstmt.setInt(5, points.getTargetPoints());
            pstmt.setBigDecimal(6, points.getTotalPoints());
            pstmt.setString(7, points.getLastUpdated() != null ? points.getLastUpdated().toString() : null);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to upsert fasee7 points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Points> findAllOrderedByTotal() {
        String sql = "SELECT * FROM fasee7_points ORDER BY total_points DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Fasee7Points> pointsList = new ArrayList<>();
            while (rs.next()) {
                pointsList.add(Fasee7PointsDAOImplHelpers.extractPointsFromResultSet(rs, transformers));
            }
            return pointsList;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all fasee7 points ordered by total", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int getRankByStudentId(int studentId) {
        // SQLite doesn't have ROW_NUMBER(), so we use a subquery approach
        String sql = "SELECT COUNT(*) + 1 AS rank " +
                    "FROM fasee7_points " +
                    "WHERE total_points > (SELECT total_points FROM fasee7_points WHERE student_id = ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank");
            }
            
            // If no record found for student, check if student exists
            String checkSql = "SELECT COUNT(*) FROM fasee7_points WHERE student_id = ?";
            PreparedStatement checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, studentId);
            ResultSet checkRs = checkPstmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) == 0) {
                return 0; // Student has no points record
            }
            
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get rank for student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Points> getTopN(int limit) {
        String sql = "SELECT * FROM fasee7_points ORDER BY total_points DESC LIMIT ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            
            ResultSet rs = pstmt.executeQuery();
            List<Fasee7Points> pointsList = new ArrayList<>();
            while (rs.next()) {
                pointsList.add(Fasee7PointsDAOImplHelpers.extractPointsFromResultSet(rs, transformers));
            }
            return pointsList;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get top N fasee7 points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Fasee7Points> findByMinPoints(BigDecimal minPoints) {
        String sql = "SELECT * FROM fasee7_points WHERE total_points >= ? ORDER BY total_points DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBigDecimal(1, minPoints);
            
            ResultSet rs = pstmt.executeQuery();
            List<Fasee7Points> pointsList = new ArrayList<>();
            while (rs.next()) {
                pointsList.add(Fasee7PointsDAOImplHelpers.extractPointsFromResultSet(rs, transformers));
            }
            return pointsList;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find fasee7 points by minimum points", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
}