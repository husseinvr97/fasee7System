package com.studenttracker.service;

import com.studenttracker.model.Target;
import com.studenttracker.model.Target.TopicCategory;
import com.studenttracker.model.TargetAchievementStreak;

import java.util.List;

/**
 * Service interface for Target System operations.
 * Handles target generation, achievement, and streak management.
 */
public interface TargetService {
    
    // ========== Target Generation ==========
     
    /**
     * Create a new target for a student.
     * @param studentId Student ID
     * @param category Topic category
     * @param targetPiValue Target PI value to achieve
     * @return Created target ID
     */
    Integer createTarget(Integer studentId, TopicCategory category, int targetPiValue);
    
    /**
     * Generate stacked targets when PI degrades.
     * Creates targets for each step back to previous PI.
     * @param studentId Student ID
     * @param category Topic category
     * @param previousPi Previous PI value
     * @param currentPi Current (degraded) PI value
     */
    void generateTargetsOnDegradation(Integer studentId, TopicCategory category, 
                                      int previousPi, int currentPi);
    
    
    // ========== Target Achievement ==========
    
    /**
     * Check and achieve all targets that student has reached.
     * @param studentId Student ID
     * @param category Topic category
     * @param newPi New PI value achieved
     */
    void checkAndAchieveTargets(Integer studentId, TopicCategory category, int newPi);
    
    /**
     * Mark a specific target as achieved.
     * @param targetId Target ID
     * @return true if achievement successful
     */
    boolean achieveTarget(Integer targetId);
    
    
    // ========== Streak Management ==========
    
    /**
     * Update achievement streak for student.
     * @param studentId Student ID
     * @param achieved true if target achieved, false if degradation
     */
    void updateStreak(Integer studentId, boolean achieved);
    
    /**
     * Reset achievement streak to 0.
     * @param studentId Student ID
     */
    void resetStreak(Integer studentId);
    
    
    // ========== Retrieval ==========
    
    /**
     * Get target by ID.
     * @param targetId Target ID
     * @return Target object or null
     */
    Target getTargetById(Integer targetId);
    
    /**
     * Get all active targets for student.
     * @param studentId Student ID
     * @return List of active targets
     */
    List<Target> getActiveTargets(Integer studentId);
    
    /**
     * Get all achieved targets for student.
     * @param studentId Student ID
     * @return List of achieved targets
     */
    List<Target> getAchievedTargets(Integer studentId);
    
    /**
     * Get targets by category for student.
     * @param studentId Student ID
     * @param category Topic category
     * @return List of targets in category
     */
    List<Target> getTargetsByCategory(Integer studentId, TopicCategory category);
    
    /**
     * Get achievement streak for student.
     * @param studentId Student ID
     * @return Streak object
     */
    TargetAchievementStreak getStreak(Integer studentId);
    
    /**
     * Get current streak count.
     * @param studentId Student ID
     * @return Current streak value
     */
    int getCurrentStreak(Integer studentId);
    
    /**
     * Get total points earned from targets.
     * @param studentId Student ID
     * @return Total points
     */
    int getTotalTargetPoints(Integer studentId);
    
    
    // ========== Statistics ==========
    
    /**
     * Get count of active targets for student.
     * @param studentId Student ID
     * @return Number of active targets
     */
    int getActiveTargetCount(Integer studentId);
    
    /**
     * Get count of achieved targets for student.
     * @param studentId Student ID
     * @return Number of achieved targets
     */
    int getAchievedTargetCount(Integer studentId);
    
    /**
     * Get top students by achievement streak.
     * @param limit Maximum number of results
     * @return List of top streaks
     */
    List<TargetAchievementStreak> getTopStreaks(int limit);
    
    
    // ========== Default Target ==========
    
    /**
     * Check if student has any active targets.
     * @param studentId Student ID
     * @return true if has active targets
     */
    boolean hasActiveTargets(Integer studentId);
    
    /**
     * Get default target message when no active targets.
     * @param studentId Student ID
     * @return Default message string
     */
    String getDefaultTargetMessage(Integer studentId);
}