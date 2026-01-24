package com.studenttracker.service.impl.helpers;

import com.studenttracker.dao.TargetDAO;
import com.studenttracker.dao.TargetAchievementStreakDAO;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Target;
import com.studenttracker.model.Target.TopicCategory;
import com.studenttracker.model.TargetAchievementStreak;
import com.studenttracker.service.TargetService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class for TargetServiceImpl.
 * Contains utility methods for target generation and achievement logic.
 */
public class TargetServiceImplHelpers {
    
    private TargetServiceImplHelpers() {}
    
    
    // ========== Validation ==========
    
    /**
     * Validates that a target doesn't already exist for the student/category/value.
     * @param studentId Student ID
     * @param category Topic category
     * @param targetValue Target PI value
     * @param targetDAO Target DAO
     * @throws ValidationException if target already exists
     */
    public static void validateTargetDoesNotExist(Integer studentId, TopicCategory category, 
                                                  int targetValue, TargetDAO targetDAO) {
        boolean exists = targetDAO.hasActiveTarget(studentId, category, targetValue);
        if (exists) {
            throw new ValidationException(
                String.format("Active target already exists for student %d, category %s, PI value %d",
                    studentId, category, targetValue)
            );
        }
    }
    
    
    // ========== Target Creation ==========
    
    /**
     * Creates stacked targets for each step from currentPi to previousPi.
     * Example: PI 10 → 6 creates targets [7, 8, 9, 10]
     * @param studentId Student ID
     * @param category Topic category
     * @param previousPi Previous PI value
     * @param currentPi Current (degraded) PI value
     * @param service Target service instance (to call createTarget)
     * @return List of created target IDs
     */
    public static List<Integer> createStackedTargets(Integer studentId, TopicCategory category,
                                                     int previousPi, int currentPi, 
                                                     TargetService service) {
        List<Integer> targetIds = new ArrayList<>();
        
        // Create target for each step: (currentPi + 1) to previousPi
        for (int targetPi = currentPi + 1; targetPi <= previousPi; targetPi++) {
            try {
                Integer targetId = service.createTarget(studentId, category, targetPi);
                targetIds.add(targetId);
            } catch (ValidationException e) {
                // Target already exists, skip
                System.err.println("Skipping duplicate target: " + e.getMessage());
            }
        }
        
        return targetIds;
    }
    
    
    // ========== Target Achievement ==========
    
    /**
     * Filters targets that are achievable based on new PI value.
     * @param targets List of targets to filter
     * @param category Category to match
     * @param newPi New PI value achieved
     * @return List of achievable targets
     */
    public static List<Target> filterAchievableTargets(List<Target> targets, 
                                                       TopicCategory category, int newPi) {
        return targets.stream()
            .filter(t -> t.getCategory() == category)
            .filter(t -> t.getTargetPiValue() <= newPi)
            .filter(t -> !t.isAchieved())
            .collect(Collectors.toList());
    }
    
    
    // ========== Streak Management ==========
    
    /**
     * Gets existing streak or creates new one if doesn't exist.
     * @param studentId Student ID
     * @param streakDAO Streak DAO
     * @return Streak object (existing or new)
     */
    public static TargetAchievementStreak getOrCreateStreak(Integer studentId, 
                                                           TargetAchievementStreakDAO streakDAO) {
        TargetAchievementStreak streak = streakDAO.findByStudentId(studentId);
        
        if (streak == null) {
            // Create new streak record
            streak = new TargetAchievementStreak();
            streak.setStudentId(studentId);
            streak.setCurrentStreak(0);
            streak.setLastAchievementAt(null);
            streak.setTotalPointsEarned(0);
        }
        
        return streak;
    }
    
    /**
     * Calculates points earned for current streak level.
     * Points = streak number itself (streak 1 = 1 point, streak 5 = 5 points)
     * @param currentStreak Current streak value
     * @return Points earned
     */
    public static int calculateStreakPoints(int currentStreak) {
        return currentStreak;
    }
}