package com.studenttracker.dao;

import com.studenttracker.model.TargetAchievementStreak;
import java.util.List;

public interface TargetAchievementStreakDAO {
    
    // Standard CRUD operations
    Integer insert(TargetAchievementStreak streak);
    boolean update(TargetAchievementStreak streak);
    boolean delete(int streakId);
    TargetAchievementStreak findById(int streakId);
    List<TargetAchievementStreak> findAll();
    
    // Custom methods
    TargetAchievementStreak findByStudentId(int studentId);
    boolean upsert(TargetAchievementStreak streak);
    List<TargetAchievementStreak> findByMinStreak(int minStreak);
    List<TargetAchievementStreak> getTopStreaks(int limit);
}