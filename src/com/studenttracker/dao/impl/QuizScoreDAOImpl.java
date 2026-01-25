package com.studenttracker.dao.impl;

import com.studenttracker.dao.QuizScoreDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.QuizScore;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizScoreDAOImpl implements QuizScoreDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public Integer insert(QuizScore score) {
        String sql = "INSERT INTO quiz_scores (quiz_id, student_id, question_id, points_earned, entered_at, entered_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, score.getQuizId());
            pstmt.setInt(2, score.getStudentId());
            pstmt.setInt(3, score.getQuestionId());
            pstmt.setString(4, score.getPointsEarned().toString());
            pstmt.setString(5, score.getEnteredAt().toString());
            pstmt.setInt(6, score.getEnteredBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert quiz score failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert quiz score failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert quiz score", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(QuizScore score) {
        String sql = "UPDATE quiz_scores SET quiz_id = ?, student_id = ?, question_id = ?, " +
                    "points_earned = ?, entered_at = ?, entered_by = ? WHERE score_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, score.getQuizId());
            pstmt.setInt(2, score.getStudentId());
            pstmt.setInt(3, score.getQuestionId());
            pstmt.setString(4, score.getPointsEarned().toString());
            pstmt.setString(5, score.getEnteredAt().toString());
            pstmt.setInt(6, score.getEnteredBy());
            pstmt.setInt(7, score.getScoreId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update quiz score", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int scoreId) {
        String sql = "DELETE FROM quiz_scores WHERE score_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, scoreId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete quiz score", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public QuizScore findById(int scoreId) {
        String sql = "SELECT * FROM quiz_scores WHERE score_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, scoreId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractQuizScoreFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz score by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizScore> findAll() {
        String sql = "SELECT * FROM quiz_scores ORDER BY entered_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<QuizScore> scores = new ArrayList<>();
            while (rs.next()) {
                scores.add(extractQuizScoreFromResultSet(rs));
            }
            return scores;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all quiz scores", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizScore> findByQuizId(int quizId) {
        String sql = "SELECT * FROM quiz_scores WHERE quiz_id = ? ORDER BY student_id, question_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            ResultSet rs = pstmt.executeQuery();
            List<QuizScore> scores = new ArrayList<>();
            while (rs.next()) {
                scores.add(extractQuizScoreFromResultSet(rs));
            }
            return scores;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz scores by quiz ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizScore> findByStudentId(int studentId) {
        String sql = "SELECT * FROM quiz_scores WHERE student_id = ? ORDER BY quiz_id, question_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<QuizScore> scores = new ArrayList<>();
            while (rs.next()) {
                scores.add(extractQuizScoreFromResultSet(rs));
            }
            return scores;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz scores by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizScore> findByQuizAndStudent(int quizId, int studentId) {
        String sql = "SELECT * FROM quiz_scores WHERE quiz_id = ? AND student_id = ? ORDER BY question_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            pstmt.setInt(2, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<QuizScore> scores = new ArrayList<>();
            while (rs.next()) {
                scores.add(extractQuizScoreFromResultSet(rs));
            }
            return scores;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz scores by quiz and student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean bulkInsert(List<QuizScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO quiz_scores (quiz_id, student_id, question_id, points_earned, entered_at, entered_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int count = 0;
            for (QuizScore score : scores) {
                pstmt.setInt(1, score.getQuizId());
                pstmt.setInt(2, score.getStudentId());
                pstmt.setInt(3, score.getQuestionId());
                pstmt.setString(4, score.getPointsEarned().toString());
                pstmt.setString(5, score.getEnteredAt().toString());
                pstmt.setInt(6, score.getEnteredBy());
                
                pstmt.addBatch();
                count++;
                
                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                }
            }
            
            pstmt.executeBatch(); // Execute remaining
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new DAOException("Failed to rollback bulk insert", ex);
                }
            }
            throw new DAOException("Failed to bulk insert quiz scores", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    // Ignore
                }
            }
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Double getTotalScoreForStudent(int quizId, int studentId) {
        String sql = "SELECT SUM(points_earned) as total FROM quiz_scores WHERE quiz_id = ? AND student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            pstmt.setInt(2, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String total = rs.getString("total");
                return total != null ? Double.parseDouble(total) : 0.0;
            }
            return 0.0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get total score for student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract QuizScore object from ResultSet
    private QuizScore extractQuizScoreFromResultSet(ResultSet rs) throws SQLException {
        QuizScore score = new QuizScore();
        score.setScoreId(rs.getInt("score_id"));
        score.setQuizId(rs.getInt("quiz_id"));
        score.setStudentId(rs.getInt("student_id"));
        score.setQuestionId(rs.getInt("question_id"));
        
        String pointsEarned = rs.getString("points_earned");
        score.setPointsEarned(pointsEarned != null ? Double.valueOf(pointsEarned) : 0.0);
        
        String enteredAt = rs.getString("entered_at");
        score.setEnteredAt(enteredAt != null ? LocalDateTime.parse(enteredAt) : null);
        
        score.setEnteredBy(rs.getInt("entered_by"));
        
        return score;
    }
}