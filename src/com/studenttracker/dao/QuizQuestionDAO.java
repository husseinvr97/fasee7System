package com.studenttracker.dao;

import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.QuizQuestion;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for QuizQuestion entity operations.
 */
public interface QuizQuestionDAO {
    
    // Standard CRUD operations
    Integer insert(QuizQuestion question);
    boolean update(QuizQuestion question);
    boolean delete(int questionId);
    QuizQuestion findById(int questionId);
    List<QuizQuestion> findAll();
    
    // Custom query methods
    List<QuizQuestion> findByQuizId(int quizId);
    boolean bulkInsert(List<QuizQuestion> questions);
    Map<TopicCategory, BigDecimal> getCategoryTotalsByQuiz(int quizId);
    boolean deleteByQuizId(int quizId);
}