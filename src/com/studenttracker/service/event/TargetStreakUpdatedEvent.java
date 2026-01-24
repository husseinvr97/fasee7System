package com.studenttracker.service.event;

public class TargetStreakUpdatedEvent implements Event {
    private final Integer studentId;
    private final int currentStreak;
    private final int totalPointsEarned;

    public TargetStreakUpdatedEvent(Integer studentId, int currentStreak, int totalPointsEarned) {
        this.studentId = studentId;
        this.currentStreak = currentStreak;
        this.totalPointsEarned = totalPointsEarned;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getTotalPointsEarned() {
        return totalPointsEarned;
    }

    @Override
    public String toString() {
        return "TargetStreakUpdatedEvent{" +
                "studentId=" + studentId +
                ", currentStreak=" + currentStreak +
                ", totalPointsEarned=" + totalPointsEarned +
                '}';
    }
}