package com.studenttracker.dao;

import com.studenttracker.model.LessonTopic;
import com.studenttracker.model.LessonTopic.TopicCategory;
import java.util.List;

/**
 * Data Access Object interface for LessonTopic entity operations.
 */
public interface LessonTopicDAO {
    
    // Standard CRUD operations
    Integer insert(LessonTopic topic);
    boolean update(LessonTopic topic);
    boolean delete(int topicId);
    LessonTopic findById(int topicId);
    List<LessonTopic> findAll();
    
    // Custom query methods
    List<LessonTopic> findByLessonId(int lessonId);
    List<LessonTopic> findByCategory(TopicCategory category);
    boolean deleteByLessonId(int lessonId);
    List<LessonTopic> searchBySpecificTopic(String searchTerm);
}