package com.studenttracker.dao.impl;

import com.studenttracker.dao.LessonDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Lesson;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LessonDAOImpl implements LessonDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(Lesson lesson) {
        String sql = "INSERT INTO lessons (lesson_date, month_group, created_at, created_by) " +
                    "VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, lesson.getLessonDate().toString());
            pstmt.setString(2, lesson.getMonthGroup());
            pstmt.setString(3, lesson.getCreatedAt().toString());
            pstmt.setInt(4, lesson.getCreatedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert lesson failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert lesson failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert lesson", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Lesson lesson) {
        String sql = "UPDATE lessons SET lesson_date = ?, month_group = ? WHERE lesson_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, lesson.getLessonDate().toString());
            pstmt.setString(2, lesson.getMonthGroup());
            pstmt.setInt(3, lesson.getLessonId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update lesson", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int lessonId) {
        String sql = "DELETE FROM lessons WHERE lesson_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete lesson", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Lesson findById(int lessonId) {
        String sql = "SELECT * FROM lessons WHERE lesson_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractLessonFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lesson by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Lesson> findAll() {
        String sql = "SELECT * FROM lessons ORDER BY lesson_date DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Lesson> lessons = new ArrayList<>();
            while (rs.next()) {
                lessons.add(extractLessonFromResultSet(rs));
            }
            return lessons;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all lessons", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Lesson> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM lessons WHERE lesson_date BETWEEN ? AND ? ORDER BY lesson_date";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            
            ResultSet rs = pstmt.executeQuery();
            List<Lesson> lessons = new ArrayList<>();
            while (rs.next()) {
                lessons.add(extractLessonFromResultSet(rs));
            }
            return lessons;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lessons by date range", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Lesson> findByMonthGroup(String monthGroup) {
        String sql = "SELECT * FROM lessons WHERE month_group = ? ORDER BY lesson_date";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, monthGroup);
            
            ResultSet rs = pstmt.executeQuery();
            List<Lesson> lessons = new ArrayList<>();
            while (rs.next()) {
                lessons.add(extractLessonFromResultSet(rs));
            }
            return lessons;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lessons by month group", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Lesson> findByCreatedBy(int userId) {
        String sql = "SELECT * FROM lessons WHERE created_by = ? ORDER BY lesson_date DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Lesson> lessons = new ArrayList<>();
            while (rs.next()) {
                lessons.add(extractLessonFromResultSet(rs));
            }
            return lessons;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lessons by creator", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM lessons";
        
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
            throw new DAOException("Failed to count all lessons", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Lesson findLatest() {
        String sql = "SELECT * FROM lessons ORDER BY lesson_date DESC, created_at DESC LIMIT 1";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                return extractLessonFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find latest lesson", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract Lesson object from ResultSet
    private Lesson extractLessonFromResultSet(ResultSet rs) throws SQLException {
        Lesson lesson = new Lesson();
        lesson.setLessonId(rs.getInt("lesson_id"));
        
        String lessonDate = rs.getString("lesson_date");
        lesson.setLessonDate(lessonDate != null ? LocalDate.parse(lessonDate) : null);
        
        lesson.setMonthGroup(rs.getString("month_group"));
        
        String createdAt = rs.getString("created_at");
        lesson.setCreatedAt(createdAt != null ? LocalDateTime.parse(createdAt) : null);
        
        lesson.setCreatedBy(rs.getInt("created_by"));
        
        return lesson;
    }
}