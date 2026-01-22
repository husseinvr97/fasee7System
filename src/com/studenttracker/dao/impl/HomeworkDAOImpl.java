package com.studenttracker.dao.impl;

import com.studenttracker.dao.HomeworkDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Homework;
import com.studenttracker.model.Homework.HomeworkStatus;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeworkDAOImpl implements HomeworkDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    private static final int BATCH_SIZE = 100;
    
    @Override
    public Integer insert(Homework homework) {
        String sql = "INSERT INTO homework (lesson_id, student_id, status, marked_at, marked_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, homework.getLessonId());
            pstmt.setInt(2, homework.getStudentId());
            pstmt.setString(3, homework.getStatus().name());
            pstmt.setString(4, homework.getMarkedAt().toString());
            pstmt.setInt(5, homework.getMarkedBy());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert homework failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert homework failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert homework", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(Homework homework) {
        String sql = "UPDATE homework SET lesson_id = ?, student_id = ?, status = ?, " +
                    "marked_at = ?, marked_by = ? WHERE homework_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, homework.getLessonId());
            pstmt.setInt(2, homework.getStudentId());
            pstmt.setString(3, homework.getStatus().name());
            pstmt.setString(4, homework.getMarkedAt().toString());
            pstmt.setInt(5, homework.getMarkedBy());
            pstmt.setInt(6, homework.getHomeworkId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update homework", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int homeworkId) {
        String sql = "DELETE FROM homework WHERE homework_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, homeworkId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete homework", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Homework findById(int homeworkId) {
        String sql = "SELECT * FROM homework WHERE homework_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, homeworkId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractHomeworkFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find homework by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Homework> findAll() {
        String sql = "SELECT * FROM homework ORDER BY marked_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<Homework> homeworks = new ArrayList<>();
            while (rs.next()) {
                homeworks.add(extractHomeworkFromResultSet(rs));
            }
            return homeworks;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all homework records", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Homework> findByLessonId(int lessonId) {
        String sql = "SELECT * FROM homework WHERE lesson_id = ? ORDER BY student_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Homework> homeworks = new ArrayList<>();
            while (rs.next()) {
                homeworks.add(extractHomeworkFromResultSet(rs));
            }
            return homeworks;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find homework by lesson ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<Homework> findByStudentId(int studentId) {
        String sql = "SELECT * FROM homework WHERE student_id = ? ORDER BY marked_at DESC";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            List<Homework> homeworks = new ArrayList<>();
            while (rs.next()) {
                homeworks.add(extractHomeworkFromResultSet(rs));
            }
            return homeworks;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find homework by student ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public Homework findByLessonAndStudent(int lessonId, int studentId) {
        String sql = "SELECT * FROM homework WHERE lesson_id = ? AND student_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            pstmt.setInt(2, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractHomeworkFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find homework by lesson and student", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public int countByStudentAndStatus(int studentId, HomeworkStatus status) {
        String sql = "SELECT COUNT(*) FROM homework WHERE student_id = ? AND status = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setString(2, status.name());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to count homework by student and status", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean bulkInsert(List<Homework> homeworkList) {
        if (homeworkList == null || homeworkList.isEmpty()) {
            return false;
        }
        
        String sql = "INSERT INTO homework (lesson_id, student_id, status, marked_at, marked_by) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            int count = 0;
            for (Homework homework : homeworkList) {
                pstmt.setInt(1, homework.getLessonId());
                pstmt.setInt(2, homework.getStudentId());
                pstmt.setString(3, homework.getStatus().name());
                pstmt.setString(4, homework.getMarkedAt().toString());
                pstmt.setInt(5, homework.getMarkedBy());
                
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
            throw new DAOException("Failed to bulk insert homework", e);
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
    public Map<HomeworkStatus, Integer> getHomeworkStatsByLesson(int lessonId) {
        String sql = "SELECT status, COUNT(*) as count FROM homework WHERE lesson_id = ? GROUP BY status";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            
            // Initialize map with all statuses at 0
            Map<HomeworkStatus, Integer> stats = new HashMap<>();
            for (HomeworkStatus status : HomeworkStatus.values()) {
                stats.put(status, 0);
            }
            
            // Fill in actual counts
            while (rs.next()) {
                HomeworkStatus status = HomeworkStatus.valueOf(rs.getString("status"));
                int count = rs.getInt("count");
                stats.put(status, count);
            }
            
            return stats;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to get homework stats by lesson", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract Homework object from ResultSet
    private Homework extractHomeworkFromResultSet(ResultSet rs) throws SQLException {
        Homework homework = new Homework();
        homework.setHomeworkId(rs.getInt("homework_id"));
        homework.setLessonId(rs.getInt("lesson_id"));
        homework.setStudentId(rs.getInt("student_id"));
        homework.setStatus(HomeworkStatus.valueOf(rs.getString("status")));
        
        String markedAt = rs.getString("marked_at");
        homework.setMarkedAt(markedAt != null ? LocalDateTime.parse(markedAt) : null);
        
        homework.setMarkedBy(rs.getInt("marked_by"));
        
        return homework;
    }
}