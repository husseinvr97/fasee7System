package com.studenttracker.dao;

import com.studenttracker.model.PerformanceIndicator;
import com.studenttracker.model.PerformanceIndicator.TopicCategory;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for PerformanceIndicator operations.
 * Manages CRUD operations and custom queries for performance indicators.
 */
public interface PerformanceIndicatorDAO {
    
    /**
     * Insert a new performance indicator.
     * @param pi PerformanceIndicator to insert
     * @return Generated PI ID
     */
    Integer insert(PerformanceIndicator pi);
    
    /**
     * Update an existing performance indicator.
     * @param pi PerformanceIndicator with updated data
     * @return true if update successful, false otherwise
     */
    boolean update(PerformanceIndicator pi);
    
    /**
     * Delete a performance indicator by ID.
     * @param piId Performance indicator ID to delete
     * @return true if deletion successful, false otherwise
     */
    boolean delete(int piId);
    
    /**
     * Find a performance indicator by ID.
     * @param piId Performance indicator ID
     * @return PerformanceIndicator object or null if not found
     */
    PerformanceIndicator findById(int piId);
    
    /**
     * Get all performance indicators.
     * @return List of all performance indicators
     */
    List<PerformanceIndicator> findAll();
    
    /**
     * Get all performance indicators for a student, ordered by calculated_at.
     * @param studentId Student ID
     * @return List of performance indicators ordered chronologically
     */
    List<PerformanceIndicator> findByStudentId(int studentId);
    
    /**
     * Get performance indicators for a specific student and category, ordered by calculated_at.
     * @param studentId Student ID
     * @param category Topic category
     * @return List of performance indicators for the category
     */
    List<PerformanceIndicator> findByStudentAndCategory(int studentId, TopicCategory category);
    
    /**
     * Get the most recent performance indicator for a specific student and category.
     * @param studentId Student ID
     * @param category Topic category
     * @return Latest PerformanceIndicator or null if none exists
     */
    PerformanceIndicator findLatestByStudentAndCategory(int studentId, TopicCategory category);
    
    /**
     * Get the current (latest) cumulative PI for each category for a student.
     * @param studentId Student ID
     * @return Map of category to cumulative PI value
     */
    Map<TopicCategory, Integer> getCurrentPIsByStudent(int studentId);
    
    /**
     * Get all performance indicators calculated from a specific quiz.
     * @param quizId Quiz ID
     * @return List of performance indicators from this quiz
     */
    List<PerformanceIndicator> findByQuizId(int quizId);
}