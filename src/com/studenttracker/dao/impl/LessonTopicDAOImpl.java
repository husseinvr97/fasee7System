package com.studenttracker.dao.impl;

import com.studenttracker.dao.LessonTopicDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.LessonTopic;
import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LessonTopicDAOImpl implements LessonTopicDAO {
    
    private final DatabaseConnection dbConn = DatabaseConnection.getInstance();
    
    @Override
    public Integer insert(LessonTopic topic) {
        String sql = "INSERT INTO lesson_topics (lesson_id, category, specific_topic) VALUES (?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, topic.getLessonId());
            pstmt.setString(2, topic.getCategory().name());
            pstmt.setString(3, topic.getSpecificTopic());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DAOException("Insert lesson topic failed, no rows affected");
            }
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new DAOException("Insert lesson topic failed, no ID obtained");
            }
            
        } catch (SQLException e) {
            throw new DAOException("Failed to insert lesson topic", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean update(LessonTopic topic) {
        String sql = "UPDATE lesson_topics SET lesson_id = ?, category = ?, specific_topic = ? WHERE topic_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, topic.getLessonId());
            pstmt.setString(2, topic.getCategory().name());
            pstmt.setString(3, topic.getSpecificTopic());
            pstmt.setInt(4, topic.getTopicId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to update lesson topic", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean delete(int topicId) {
        String sql = "DELETE FROM lesson_topics WHERE topic_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, topicId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete lesson topic", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public LessonTopic findById(int topicId) {
        String sql = "SELECT * FROM lesson_topics WHERE topic_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, topicId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractLessonTopicFromResultSet(rs);
            }
            return null;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lesson topic by ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<LessonTopic> findAll() {
        String sql = "SELECT * FROM lesson_topics ORDER BY topic_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            List<LessonTopic> topics = new ArrayList<>();
            while (rs.next()) {
                topics.add(extractLessonTopicFromResultSet(rs));
            }
            return topics;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find all lesson topics", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<LessonTopic> findByLessonId(int lessonId) {
        String sql = "SELECT * FROM lesson_topics WHERE lesson_id = ? ORDER BY topic_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            ResultSet rs = pstmt.executeQuery();
            List<LessonTopic> topics = new ArrayList<>();
            while (rs.next()) {
                topics.add(extractLessonTopicFromResultSet(rs));
            }
            return topics;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lesson topics by lesson ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<LessonTopic> findByCategory(TopicCategory category) {
        String sql = "SELECT * FROM lesson_topics WHERE category = ? ORDER BY topic_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.name());
            
            ResultSet rs = pstmt.executeQuery();
            List<LessonTopic> topics = new ArrayList<>();
            while (rs.next()) {
                topics.add(extractLessonTopicFromResultSet(rs));
            }
            return topics;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to find lesson topics by category", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public boolean deleteByLessonId(int lessonId) {
        String sql = "DELETE FROM lesson_topics WHERE lesson_id = ?";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, lessonId);
            
            pstmt.executeUpdate();
            return true; // Returns true even if 0 rows deleted (no topics existed)
            
        } catch (SQLException e) {
            throw new DAOException("Failed to delete lesson topics by lesson ID", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    @Override
    public List<LessonTopic> searchBySpecificTopic(String searchTerm) {
        String sql = "SELECT * FROM lesson_topics WHERE specific_topic LIKE ? ORDER BY topic_id";
        
        Connection conn = null;
        try {
            conn = dbConn.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + searchTerm + "%");
            
            ResultSet rs = pstmt.executeQuery();
            List<LessonTopic> topics = new ArrayList<>();
            while (rs.next()) {
                topics.add(extractLessonTopicFromResultSet(rs));
            }
            return topics;
            
        } catch (SQLException e) {
            throw new DAOException("Failed to search lesson topics", e);
        } finally {
            dbConn.closeConnection(conn);
        }
    }
    
    // Helper method to extract LessonTopic object from ResultSet
    private LessonTopic extractLessonTopicFromResultSet(ResultSet rs) throws SQLException {
        LessonTopic topic = new LessonTopic();
        topic.setTopicId(rs.getInt("topic_id"));
        topic.setLessonId(rs.getInt("lesson_id"));
        topic.setCategory(TopicCategory.valueOf(rs.getString("category")));
        topic.setSpecificTopic(rs.getString("specific_topic"));
        
        return topic;
    }
}