package com.studenttracker.dao;


import java.util.List;

import com.studenttracker.model.QuizScore;

/**
 * Data Access Object interface for QuizScore entity operations.
 * Note: QuizScore entity should match the quiz_scores table schema
 */
public interface QuizScoreDAO {
    
    // Standard CRUD operations
    Integer insert(QuizScore score);
    boolean update(QuizScore score);
    boolean delete(int scoreId);
    QuizScore findById(int scoreId);
    List<QuizScore> findAll();
    
    // Custom query methods
    List<QuizScore> findByQuizId(int quizId);
    List<QuizScore> findByStudentId(int studentId);
    List<QuizScore> findByQuizAndStudent(int quizId, int studentId);
    boolean bulkInsert(List<QuizScore> scores);
    Double getTotalScoreForStudent(int quizId, int studentId);
}