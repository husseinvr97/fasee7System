package com.studenttracker.dao;

import com.studenttracker.model.Quiz;

import java.util.List;

/**
 * Data Access Object interface for Quiz entity operations.
 */
public interface QuizDAO {
    
    // Standard CRUD operations
    Integer insert(Quiz quiz);
    boolean update(Quiz quiz);
    boolean delete(int quizId);
    Quiz findById(int quizId);
    List<Quiz> findAll();
    
    // Custom query methods
    Quiz findByLessonId(int lessonId);
    byte[] getQuizPdf(int quizId);
    int countAll();
}