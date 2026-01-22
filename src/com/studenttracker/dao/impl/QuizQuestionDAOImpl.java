package com.studenttracker.dao.impl;

import com.studenttracker.dao.QuizQuestionDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizQuestion.QuestionType;
import com.studenttracker.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizQuestionDAOImpl implements QuizQuestionDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public Integer insert(QuizQuestion question) {
        String sql = "INSERT INTO quiz_questions (quiz_id, question_number, question_type, category, points, model_answer) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, question.getQuizId());
            pstmt.setInt(2, question.getQuestionNumber());
            pstmt.setString(3, question.getQuestionType().name());
            pstmt.setString(4, question.getCategory().name());
            pstmt.setString(5, question.getPoints().toString());
            pstmt.setString(6, question.getModelAnswer());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert quiz question failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert quiz question failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert quiz question", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(QuizQuestion question) {
        String sql = "UPDATE quiz_questions SET quiz_id = ?, question_number = ?, question_type = ?, " +
                    "category = ?, points = ?, model_answer = ? WHERE question_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, question.getQuizId());
            pstmt.setInt(2, question.getQuestionNumber());
            pstmt.setString(3, question.getQuestionType().name());
            pstmt.setString(4, question.getCategory().name());
            pstmt.setString(5, question.getPoints().toString());
            pstmt.setString(6, question.getModelAnswer());
            pstmt.setInt(7, question.getQuestionId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update quiz question", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int questionId) {
        String sql = "DELETE FROM quiz_questions WHERE question_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, questionId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete quiz question", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public QuizQuestion findById(int questionId) {
        String sql = "SELECT * FROM quiz_questions WHERE question_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, questionId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractQuizQuestionFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz question by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizQuestion> findAll() {
        String sql = "SELECT * FROM quiz_questions ORDER BY quiz_id, question_number";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<QuizQuestion> questions = new ArrayList<>();
            while (rs.next()) {
                questions.add(extractQuizQuestionFromResultSet(rs));
            }
            return questions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all quiz questions", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizQuestion> findByQuizId(int quizId) {
        String sql = "SELECT * FROM quiz_questions WHERE quiz_id = ? ORDER BY question_number";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            ResultSet rs = pstmt.executeQuery();
            List<QuizQuestion> questions = new ArrayList<>();
            while (rs.next()) {
                questions.add(extractQuizQuestionFromResultSet(rs));
            }
            return questions;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz questions by quiz ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean bulkInsert(List<QuizQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO quiz_questions (quiz_id, question_number, question_type, category, points, model_answer) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int count = 0;
            for (QuizQuestion question : questions) {
                pstmt.setInt(1, question.getQuizId());
                pstmt.setInt(2, question.getQuestionNumber());
                pstmt.setString(3, question.getQuestionType().name());
                pstmt.setString(4, question.getCategory().name());
                pstmt.setString(5, question.getPoints().toString());
                pstmt.setString(6, question.getModelAnswer());
                
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
            throw new DAOException("Failed to bulk insert quiz questions", e);
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
    public Map<TopicCategory, BigDecimal> getCategoryTotalsByQuiz(int quizId) {
        String sql = "SELECT category, SUM(points) as total FROM quiz_questions WHERE quiz_id = ? GROUP BY category";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            ResultSet rs = pstmt.executeQuery();
            Map<TopicCategory, BigDecimal> categoryTotals = new HashMap<>();
            
            while (rs.next()) {
                TopicCategory category = TopicCategory.valueOf(rs.getString("category"));
                BigDecimal total = new BigDecimal(rs.getString("total"));
                categoryTotals.put(category, total);
            }
            
            return categoryTotals;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get category totals by quiz", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean deleteByQuizId(int quizId) {
        String sql = "DELETE FROM quiz_questions WHERE quiz_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            pstmt.executeUpdate();
            return true; // Returns true even if 0 rows deleted (no questions existed)
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete quiz questions by quiz ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract QuizQuestion object from ResultSet
    private QuizQuestion extractQuizQuestionFromResultSet(ResultSet rs) throws SQLException {
        QuizQuestion question = new QuizQuestion();
        question.setQuestionId(rs.getInt("question_id"));
        question.setQuizId(rs.getInt("quiz_id"));
        question.setQuestionNumber(rs.getInt("question_number"));
        question.setQuestionType(QuestionType.valueOf(rs.getString("question_type")));
        question.setCategory(TopicCategory.valueOf(rs.getString("category")));
        
        String points = rs.getString("points");
        question.setPoints(points != null ? new BigDecimal(points) : BigDecimal.ZERO);
        
        question.setModelAnswer(rs.getString("model_answer"));
        
        return question;
    }
}