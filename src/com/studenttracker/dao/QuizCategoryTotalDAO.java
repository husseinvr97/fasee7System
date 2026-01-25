package com.studenttracker.dao;

import com.studenttracker.model.LessonTopic;
import com.studenttracker.model.QuizCategoryTotal;
import java.util.List;
import java.util.Map;

public interface QuizCategoryTotalDAO {
    
    // Standard CRUD operations
    Integer insert(QuizCategoryTotal total);
    boolean update(QuizCategoryTotal total);
    boolean delete(int totalId);
    QuizCategoryTotal findById(int totalId);
    List<QuizCategoryTotal> findAll();
    
    // Custom methods
    List<QuizCategoryTotal> findByQuizAndStudent(int quizId, int studentId);
    List<QuizCategoryTotal> findByStudentId(int studentId);
    boolean bulkInsert(List<QuizCategoryTotal> totals);
    Map<LessonTopic.TopicCategory, Double> getCategoryTotalsForStudent(int studentId);
}