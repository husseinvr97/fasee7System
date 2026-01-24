package com.studenttracker.service;

import com.studenttracker.model.TargetAchievementStreak;
import java.util.List;

public interface TargetService {
    
    /**
     * Increments the streak for a student when they achieve their target.
     * TODO: Implement logic to determine when a student achieves their target
     * 
     * @param studentId the ID of the student
     */
    void incrementStreak(int studentId);
    
    /**
     * Resets the streak for a student to 0 when they fail to achieve their target.
     * TODO: Implement logic to determine when a student's performance degrades
     * 
     * @param studentId the ID of the student
     */
    void resetStreak(int studentId);
    
    /**
     * Retrieves the streak record for a specific student.
     * 
     * @param studentId the ID of the student
     * @return the streak record, or null if not found
     */
    TargetAchievementStreak getStudentStreak(int studentId);
    
    /**
     * Retrieves the top N students by current streak for leaderboard display.
     * 
     * @param limit the number of top students to retrieve
     * @return list of streak records
     */
    List<TargetAchievementStreak> getStreakLeaderboard(int limit);
    
    /**
     * Retrieves all students with a minimum streak value.
     * TODO: Use this for identifying students eligible for rewards/recognition
     * 
     * @param minStreak the minimum streak value
     * @return list of streak records
     */
    List<TargetAchievementStreak> getStudentsWithMinStreak(int minStreak);
    
    /**
     * Updates points earned for a student's streak.
     * TODO: Implement point calculation logic based on achievements
     * 
     * @param studentId the ID of the student
     * @param pointsToAdd points to add to total
     */
    void addPoints(int studentId, int pointsToAdd);
}