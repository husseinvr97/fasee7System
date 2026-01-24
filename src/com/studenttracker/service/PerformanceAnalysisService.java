package com.studenttracker.service;

import com.studenttracker.model.PerformanceIndicator;
import com.studenttracker.model.PerformanceIndicator.TopicCategory;
import com.studenttracker.model.PerformanceTrend;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Performance Indicator analysis and management.
 * Calculates, tracks, and analyzes student performance across quiz categories.
 */
public interface PerformanceAnalysisService {
    
    // ========== Calculate PI ==========
    
    /**
     * Calculate performance indicators for a student after quiz grading.
     * Called automatically after quiz grading completion.
     * 
     * @param quizId Quiz ID
     * @param studentId Student ID
     */
    void calculatePerformanceIndicators(Integer quizId, Integer studentId);
    
    /**
     * Recalculate all PIs for a student from scratch.
     * Used when quiz scores are corrected.
     * 
     * @param studentId Student ID
     */
    void recalculateAllPIs(Integer studentId);
    
    
    // ========== Retrieval ==========
    
    /**
     * Get complete PI history for a student, ordered by quiz date.
     * 
     * @param studentId Student ID
     * @return List of all performance indicators
     */
    List<PerformanceIndicator> getStudentPIHistory(Integer studentId);
    
    /**
     * Get PI history for a specific category, ordered by quiz date.
     * 
     * @param studentId Student ID
     * @param category Topic category
     * @return List of performance indicators for the category
     */
    List<PerformanceIndicator> getStudentPIByCategory(Integer studentId, TopicCategory category);
    
    /**
     * Get the most recent PI for a category.
     * 
     * @param studentId Student ID
     * @param category Topic category
     * @return Latest PerformanceIndicator or null if none exists
     */
    PerformanceIndicator getLatestPI(Integer studentId, TopicCategory category);
    
    /**
     * Get current cumulative PI for a category.
     * 
     * @param studentId Student ID
     * @param category Topic category
     * @return Cumulative PI value (0 if no data)
     */
    int getCurrentCumulativePI(Integer studentId, TopicCategory category);
    
    /**
     * Get current cumulative PI for all categories.
     * 
     * @param studentId Student ID
     * @return Map of category to cumulative PI
     */
    Map<TopicCategory, Integer> getAllCategoryPIs(Integer studentId);
    
    
    // ========== Analysis ==========
    
    /**
     * Analyze performance trend for a category.
     * 
     * @param studentId Student ID
     * @param category Topic category
     * @return PerformanceTrend (IMPROVING, DEGRADING, STABLE)
     */
    PerformanceTrend getPerformanceTrend(Integer studentId, TopicCategory category);
    
    /**
     * Calculate overall PI across all categories.
     * 
     * @param studentId Student ID
     * @return Total PI (sum of all category PIs)
     */
    int calculateOverallPI(Integer studentId);
    
    /**
     * Identify weak categories (below average PI).
     * 
     * @param studentId Student ID
     * @return List of weak categories
     */
    List<TopicCategory> getWeakCategories(Integer studentId);
    
    /**
     * Identify strong categories (above average PI).
     * 
     * @param studentId Student ID
     * @return List of strong categories
     */
    List<TopicCategory> getStrongCategories(Integer studentId);
    
    
    // ========== Charts/UI Data ==========
    
    /**
     * Get PI progression over quizzes for a specific category.
     * 
     * @param studentId Student ID
     * @param category Topic category
     * @return Map of quiz_id to cumulative_pi
     */
    Map<Integer, Integer> getPIProgressionByCategory(Integer studentId, TopicCategory category);
    
    /**
     * Get overall PI progression over all quizzes.
     * 
     * @param studentId Student ID
     * @return Map of quiz_id to overall_pi
     */
    Map<Integer, Integer> getOverallPIProgression(Integer studentId);
}