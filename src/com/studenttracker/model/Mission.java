package com.studenttracker.model;

import java.time.LocalDateTime;

public class Mission {
    public enum MissionType {
        ATTENDANCE_HOMEWORK,
        QUIZ_GRADING
    }

    public enum MissionStatus {
        IN_PROGRESS,
        COMPLETED
    }

    private Integer missionId;
    private Integer lessonId;
    private MissionType missionType;
    private Integer assignedTo;
    private Integer assignedBy;
    private LocalDateTime assignedAt;
    private MissionStatus status;
    private LocalDateTime completedAt;

    public Mission() {
    }

    public Mission(Integer lessonId, MissionType missionType, Integer assignedTo, 
                   Integer assignedBy, LocalDateTime assignedAt, MissionStatus status, 
                   LocalDateTime completedAt) {
        this.lessonId = lessonId;
        this.missionType = missionType;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.status = status;
        this.completedAt = completedAt;
    }

    public Integer getMissionId() {
        return missionId;
    }

    public void setMissionId(Integer missionId) {
        this.missionId = missionId;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public MissionType getMissionType() {
        return missionType;
    }

    public void setMissionType(MissionType missionType) {
        this.missionType = missionType;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Integer getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Integer assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void complete() {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == MissionStatus.COMPLETED;
    }

    @Override
    public String toString() {
        return "Mission{" +
                "missionId=" + missionId +
                ", lessonId=" + lessonId +
                ", missionType=" + missionType +
                ", assignedTo=" + assignedTo +
                ", assignedBy=" + assignedBy +
                ", assignedAt=" + assignedAt +
                ", status=" + status +
                ", completedAt=" + completedAt +
                '}';
    }
}