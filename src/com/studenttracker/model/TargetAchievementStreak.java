package com.studenttracker.model;

import java.time.LocalDateTime;

public class TargetAchievementStreak {
    private Integer streakId;
    private Integer studentId;
    private int currentStreak;
    private LocalDateTime lastAchievementAt;
    private int totalPointsEarned;

    public TargetAchievementStreak() {
    }

    public TargetAchievementStreak(Integer studentId, int currentStreak, 
                                   LocalDateTime lastAchievementAt, int totalPointsEarned) {
        this.studentId = studentId;
        this.currentStreak = currentStreak;
        this.lastAchievementAt = lastAchievementAt;
        this.totalPointsEarned = totalPointsEarned;
    }

    public Integer getStreakId() {
        return streakId;
    }

    public void setStreakId(Integer streakId) {
        this.streakId = streakId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public LocalDateTime getLastAchievementAt() {
        return lastAchievementAt;
    }

    public void setLastAchievementAt(LocalDateTime lastAchievementAt) {
        this.lastAchievementAt = lastAchievementAt;
    }

    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void setTotalPointsEarned(int totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public void incrementStreak() {
        this.currentStreak++;
        this.lastAchievementAt = LocalDateTime.now();
        int points = calculateStreakPoints();
        this.totalPointsEarned += points;
    }

    public void resetStreak() {
        this.currentStreak = 0;
    }

    public int calculateStreakPoints() {
        return this.currentStreak;
    }

    @Override
    public String toString() {
        return "TargetAchievementStreak{" +
                "streakId=" + streakId +
                ", studentId=" + studentId +
                ", currentStreak=" + currentStreak +
                ", lastAchievementAt=" + lastAchievementAt +
                ", totalPointsEarned=" + totalPointsEarned +
                '}';
    }
}