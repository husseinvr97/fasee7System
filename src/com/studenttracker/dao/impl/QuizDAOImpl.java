package com.studenttracker.dao.impl;

import com.studenttracker.dao.QuizDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Quiz;
import com.studenttracker.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizDAOImpl implements QuizDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(Quiz quiz) {
        String sql = "INSERT INTO quizzes (lesson_id, quiz_pdf_path, total_marks, created_at, created_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, quiz.getLessonId());
            pstmt.setBytes(2, quiz.getQuizPdfData());
            pstmt.setString(3, quiz.getTotalMarks().toString());
            pstmt.setString(4, quiz.getCreatedAt().toString());
            pstmt.setInt(5, quiz.getCreatedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert quiz failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert quiz failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert quiz", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Quiz quiz) {
        String sql = "UPDATE quizzes SET lesson_id = ?, quiz_pdf_path = ?, total_marks = ? WHERE quiz_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, quiz.getLessonId());
            pstmt.setBytes(2, quiz.getQuizPdfData());
            pstmt.setString(3, quiz.getTotalMarks().toString());
            pstmt.setInt(4, quiz.getQuizId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update quiz", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int quizId) {
        String sql = "DELETE FROM quizzes WHERE quiz_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete quiz", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Quiz findById(int quizId) {
        String sql = "SELECT * FROM quizzes WHERE quiz_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractQuizFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Quiz> findAll() {
        String sql = "SELECT * FROM quizzes ORDER BY created_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Quiz> quizzes = new ArrayList<>();
            while (rs.next()) {
                quizzes.add(extractQuizFromResultSet(rs));
            }
            return quizzes;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all quizzes", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Quiz findByLessonId(int lessonId) {
        String sql = "SELECT * FROM quizzes WHERE lesson_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractQuizFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz by lesson ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public byte[] getQuizPdf(int quizId) {
        String sql = "SELECT quiz_pdf_path FROM quizzes WHERE quiz_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("quiz_pdf_path");
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get quiz PDF", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM quizzes";
        
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
            throw new DAOException("Failed to count all quizzes", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract Quiz object from ResultSet
    private Quiz extractQuizFromResultSet(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setQuizId(rs.getInt("quiz_id"));
        quiz.setLessonId(rs.getInt("lesson_id"));
        quiz.setQuizPdfData(rs.getBytes("quiz_pdf_path"));
        
        String totalMarks = rs.getString("total_marks");
        quiz.setTotalMarks(totalMarks != null ? new BigDecimal(totalMarks) : BigDecimal.ZERO);
        
        String createdAt = rs.getString("created_at");
        quiz.setCreatedAt(createdAt != null ? LocalDateTime.parse(createdAt) : null);
        
        quiz.setCreatedBy(rs.getInt("created_by"));
        
        return quiz;
    }
}