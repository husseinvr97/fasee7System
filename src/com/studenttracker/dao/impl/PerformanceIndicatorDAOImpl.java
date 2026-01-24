package com.studenttracker.dao.impl;

import com.studenttracker.dao.PerformanceIndicatorDAO;
import com.studenttracker.dao.impl.helpers.PerformanceIndicatorDAOImplHelpers;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.PerformanceIndicator;
import com.studenttracker.model.PerformanceIndicator.TopicCategory;
import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.ResultSetExtractor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PerformanceIndicatorDAOImpl implements PerformanceIndicatorDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final Map<String, Function<Object, Object>> transformers = 
        PerformanceIndicatorDAOImplHelpers.getTransformers();
    
    @Override
    public Integer insert(PerformanceIndicator pi) {
        // Validate required fields
        if (pi.getStudentId() == null || pi.getQuizId() == null || pi.getCategory() == null) {
            throw new DAOException("StudentId, QuizId, and Category are required");
        }
        
        // Calculate PI value
        int piValue = pi.calculatePiValue();
        pi.setPiValue(piValue);
        
        // Get previous cumulative PI for this category
        PerformanceIndicator previousPI = findLatestByStudentAndCategory(
            pi.getStudentId(), 
            pi.getCategory()
        );
        
        // Calculate cumulative PI
        int cumulativePi = (previousPI != null) ? 
            previousPI.getCumulativePi() + piValue : piValue;
        pi.setCumulativePi(cumulativePi);
        
        String sql = "INSERT INTO performance_indicators " +
                    "(student_id, category, quiz_id, correct_answers, wrong_answers, " +
                    "pi_value, cumulative_pi, calculated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, pi.getStudentId());
            pstmt.setString(2, pi.getCategory().name());
            pstmt.setInt(3, pi.getQuizId());
            pstmt.setInt(4, pi.getCorrectAnswers());
            pstmt.setInt(5, pi.getWrongAnswers());
            pstmt.setInt(6, pi.getPiValue());
            pstmt.setInt(7, pi.getCumulativePi());
            pstmt.setString(8, pi.getCalculatedAt().toString());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert performance indicator failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert performance indicator failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert performance indicator", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(PerformanceIndicator pi) {
        String sql = "UPDATE performance_indicators SET " +
                    "student_id = ?, category = ?, quiz_id = ?, " +
                    "correct_answers = ?, wrong_answers = ?, " +
                    "pi_value = ?, cumulative_pi = ?, calculated_at = ? " +
                    "WHERE pi_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, pi.getStudentId());
            pstmt.setString(2, pi.getCategory().name());
            pstmt.setInt(3, pi.getQuizId());
            pstmt.setInt(4, pi.getCorrectAnswers());
            pstmt.setInt(5, pi.getWrongAnswers());
            pstmt.setInt(6, pi.getPiValue());
            pstmt.setInt(7, pi.getCumulativePi());
            pstmt.setString(8, pi.getCalculatedAt().toString());
            pstmt.setInt(9, pi.getPiId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update performance indicator", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int piId) {
        String sql = "DELETE FROM performance_indicators WHERE pi_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, piId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete performance indicator", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public PerformanceIndicator findById(int piId) {
        String sql = "SELECT * FROM performance_indicators WHERE pi_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, piId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ResultSetExtractor.extractWithTransformers(
                    rs, PerformanceIndicator.class, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find performance indicator by ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<PerformanceIndicator> findAll() {
        String sql = "SELECT * FROM performance_indicators ORDER BY calculated_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<PerformanceIndicator> indicators = new ArrayList<>();
            while (rs.next()) {
                indicators.add(ResultSetExtractor.extractWithTransformers(
                    rs, PerformanceIndicator.class, transformers));
            }
            return indicators;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all performance indicators", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<PerformanceIndicator> findByStudentId(int studentId) {
        String sql = "SELECT * FROM performance_indicators " +
                    "WHERE student_id = ? " +
                    "ORDER BY calculated_at ASC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<PerformanceIndicator> indicators = new ArrayList<>();
            while (rs.next()) {
                indicators.add(ResultSetExtractor.extractWithTransformers(
                    rs, PerformanceIndicator.class, transformers));
            }
            return indicators;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find performance indicators by student ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public List<PerformanceIndicator> findByStudentAndCategory(int studentId, TopicCategory category) {
        String sql = "SELECT * FROM performance_indicators " +
                    "WHERE student_id = ? AND category = ? " +
                    "ORDER BY calculated_at ASC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, category.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<PerformanceIndicator> indicators = new ArrayList<>();
            while (rs.next()) {
                indicators.add(ResultSetExtractor.extractWithTransformers(
                    rs, PerformanceIndicator.class, transformers));
            }
            return indicators;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find performance indicators by student and category", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public PerformanceIndicator findLatestByStudentAndCategory(int studentId, TopicCategory category) {
        String sql = "SELECT * FROM performance_indicators " +
                    "WHERE student_id = ? AND category = ? " +
                    "ORDER BY calculated_at DESC LIMIT 1";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, category.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return ResultSetExtractor.extractWithTransformers(
                    rs, PerformanceIndicator.class, transformers);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find latest performance indicator", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
    
    @Override
    public Map<TopicCategory, Integer> getCurrentPIsByStudent(int studentId) {
        String sql = "SELECT category, cumulative_pi " +
                    "FROM performance_indicators pi1 " +
                    "WHERE student_id = ? " +
                    "AND calculated_at = (" +
                    "  SELECT MAX(calculated_at) " +
                    "  FROM performance_indicators pi2 " +
                    "  WHERE pi2.student_id = pi1.student_id " +
                    "  AND pi2.category = pi1.category" +
                    ") " +
                    "ORDER BY category";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            Map<TopicCategory, Integer> currentPIs = new HashMap<>();
            
            while (rs.next()) {
                TopicCategory category = TopicCategory.valueOf(rs.getString("category"));
                int cumulativePi = rs.getInt("cumulative_pi");
                currentPIs.put(category, cumulativePi);
            }
            return currentPIs;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get current PIs by student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<PerformanceIndicator> findByQuizId(int quizId) {
        String sql = "SELECT * FROM performance_indicators " +
                    "WHERE quiz_id = ? " +
                    "ORDER BY student_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quizId);
            
            ResultSet rs = pstmt.executeQuery();
            List<PerformanceIndicator> indicators = new ArrayList<>();
            while (rs.next()) {
                indicators.add(ResultSetExtractor.extractWithTransformers(
                    rs, PerformanceIndicator.class, transformers));
            }
            return indicators;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find performance indicators by quiz ID", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbConn.closeConnection(conn);
        }
        
        return null;
    }
}