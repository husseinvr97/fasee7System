package com.studenttracker.model;

import java.time.LocalDateTime;

public class Target {
    // Reusing TopicCategory enum
    public enum TopicCategory {
        MATH,
        SCIENCE,
        ENGLISH,
        HISTORY,
        PROGRAMMING,
        OTHER
    }

    private Integer targetId;
    private Integer studentId;
    private TopicCategory category;
    private int targetPiValue;
    private LocalDateTime createdAt;
    private boolean isAchieved;
    private LocalDateTime achievedAt;

    public Target() {
    }

    public Target(Integer studentId, TopicCategory category, int targetPiValue, 
                  LocalDateTime createdAt, boolean isAchieved, LocalDateTime achievedAt) {
        this.studentId = studentId;
        this.category = category;
        this.targetPiValue = targetPiValue;
        this.createdAt = createdAt;
        this.isAchieved = isAchieved;
        this.achievedAt = achievedAt;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public TopicCategory getCategory() {
        return category;
    }

    public void setCategory(TopicCategory category) {
        this.category = category;
    }

    public int getTargetPiValue() {
        return targetPiValue;
    }

    public void setTargetPiValue(int targetPiValue) {
        this.targetPiValue = targetPiValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAchieved() {
        return isAchieved;
    }

    public void setAchieved(boolean achieved) {
        isAchieved = achieved;
    }

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(LocalDateTime achievedAt) {
        this.achievedAt = achievedAt;
    }

    public void achieve() {
        this.isAchieved = true;
        this.achievedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Target{" +
                "targetId=" + targetId +
                ", studentId=" + studentId +
                ", category=" + category +
                ", targetPiValue=" + targetPiValue +
                ", createdAt=" + createdAt +
                ", isAchieved=" + isAchieved +
                ", achievedAt=" + achievedAt +
                '}';
    }
}