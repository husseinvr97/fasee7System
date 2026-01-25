package com.studenttracker.dao.impl;

import com.studenttracker.dao.QuizCategoryTotalDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.LessonTopic;
import com.studenttracker.model.QuizCategoryTotal;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizCategoryTotalDAOImpl implements QuizCategoryTotalDAO {
    
    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public Integer insert(QuizCategoryTotal total) {
        String sql = "INSERT INTO quiz_category_totals (quiz_id, student_id, category, points_earned, total_points) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, total.getQuizId());
            pstmt.setInt(2, total.getStudentId());
            pstmt.setString(3, total.getCategory().name());
            pstmt.setDouble(4, total.getPointsEarned());
            pstmt.setDouble(5, total.getTotalPoints());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert quiz category total failed, no rows affected");
            }
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new DAOException("Insert quiz category total failed, no ID obtained");
            }
        } catch (SQLException e) {
            throw new DAOException("Failed to insert quiz category total", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(QuizCategoryTotal total) {
        String sql = "UPDATE quiz_category_totals SET points_earned = ?, total_points = ? " +
                     "WHERE total_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, total.getPointsEarned());
            pstmt.setDouble(2, total.getTotalPoints());
            pstmt.setInt(3, total.getTotalId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to update quiz category total ID: " + total.getTotalId(), e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int totalId) {
        String sql = "DELETE FROM quiz_category_totals WHERE total_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, totalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Failed to delete quiz category total ID: " + totalId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public QuizCategoryTotal findById(int totalId) {
        String sql = "SELECT * FROM quiz_category_totals WHERE total_id = ?";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, totalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToQuizCategoryTotal(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DAOException("Failed to find quiz category total by ID: " + totalId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizCategoryTotal> findAll() {
        String sql = "SELECT * FROM quiz_category_totals ORDER BY quiz_id, student_id, category";
        Connection conn = null;
        List<QuizCategoryTotal> totals = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                totals.add(mapResultSetToQuizCategoryTotal(rs));
            }
            return totals;
        } catch (SQLException e) {
            throw new DAOException("Failed to retrieve all quiz category totals", e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizCategoryTotal> findByQuizAndStudent(int quizId, int studentId) {
        String sql = "SELECT * FROM quiz_category_totals WHERE quiz_id = ? AND student_id = ? " +
                     "ORDER BY category";
        Connection conn = null;
        List<QuizCategoryTotal> totals = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            pstmt.setInt(2, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                totals.add(mapResultSetToQuizCategoryTotal(rs));
            }
            return totals;
        } catch (SQLException e) {
            throw new DAOException("Failed to find category totals for quiz " + quizId + 
                                   " and student " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public List<QuizCategoryTotal> findByStudentId(int studentId) {
        String sql = "SELECT * FROM quiz_category_totals WHERE student_id = ? " +
                     "ORDER BY quiz_id, category";
        Connection conn = null;
        List<QuizCategoryTotal> totals = new ArrayList<>();
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                totals.add(mapResultSetToQuizCategoryTotal(rs));
            }
            return totals;
        } catch (SQLException e) {
            throw new DAOException("Failed to find category totals for student: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public boolean bulkInsert(List<QuizCategoryTotal> totals) {
        if (totals == null || totals.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO quiz_category_totals (quiz_id, student_id, category, points_earned, total_points) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int count = 0;
            for (QuizCategoryTotal total : totals) {
                pstmt.setInt(1, total.getQuizId());
                pstmt.setInt(2, total.getStudentId());
                pstmt.setString(3, total.getCategory().name());
                pstmt.setDouble(4, total.getPointsEarned());
                pstmt.setDouble(5, total.getTotalPoints());
                pstmt.addBatch();
                
                count++;
                if (count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                }
            }
            
            pstmt.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new DAOException("Failed to bulk insert quiz category totals", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Failed to reset auto-commit: " + e.getMessage());
                }
            }
            dbConnection.closeConnection(conn);
        }
    }
    
    @Override
    public Map<LessonTopic.TopicCategory, Double> getCategoryTotalsForStudent(int studentId) {
        Map<LessonTopic.TopicCategory, Double> categoryTotals = new HashMap<>();
        
        // Initialize all categories with 0
        for (LessonTopic.TopicCategory category : LessonTopic.TopicCategory.values()) {
            categoryTotals.put(category, 0.0);
        }
        
        String sql = "SELECT category, SUM(points_earned) as total FROM quiz_category_totals " +
                     "WHERE student_id = ? GROUP BY category";
        Connection conn = null;
        try {
            conn = dbConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LessonTopic.TopicCategory category = 
                    LessonTopic.TopicCategory.valueOf(rs.getString("category"));
                Double total = rs.getDouble("total");
                categoryTotals.put(category, total);
            }
            
            return categoryTotals;
        } catch (SQLException e) {
            throw new DAOException("Failed to get category totals for student: " + studentId, e);
        } finally {
            dbConnection.closeConnection(conn);
        }
    }
    
    private QuizCategoryTotal mapResultSetToQuizCategoryTotal(ResultSet rs) throws SQLException {
        QuizCategoryTotal total = new QuizCategoryTotal();
        total.setTotalId(rs.getInt("total_id"));
        total.setQuizId(rs.getInt("quiz_id"));
        total.setStudentId(rs.getInt("student_id"));
        total.setCategory(LessonTopic.TopicCategory.valueOf(rs.getString("category")));
        total.setPointsEarned(rs.getDouble("points_earned"));
        total.setTotalPoints(rs.getDouble("total_points"));
        return total;
    }
}