package com.studenttracker.model;

import java.time.LocalDateTime;

public class ConsecutivityTracking {
    public enum TrackingType {
        ABSENCE,
        BEHAVIORAL_INCIDENT
    }

    private Integer trackingId;
    private Integer studentId;
    private TrackingType trackingType;
    private int consecutiveCount;
    private Integer lastLessonId;
    private LocalDateTime lastUpdated;

    public ConsecutivityTracking() {
    }

    public ConsecutivityTracking(Integer studentId, TrackingType trackingType, 
                                 int consecutiveCount, Integer lastLessonId, 
                                 LocalDateTime lastUpdated) {
        this.studentId = studentId;
        this.trackingType = trackingType;
        this.consecutiveCount = consecutiveCount;
        this.lastLessonId = lastLessonId;
        this.lastUpdated = lastUpdated;
    }

    public Integer getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(Integer trackingId) {
        this.trackingId = trackingId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public TrackingType getTrackingType() {
        return trackingType;
    }

    public void setTrackingType(TrackingType trackingType) {
        this.trackingType = trackingType;
    }

    public int getConsecutiveCount() {
        return consecutiveCount;
    }

    public void setConsecutiveCount(int consecutiveCount) {
        this.consecutiveCount = consecutiveCount;
    }

    public Integer getLastLessonId() {
        return lastLessonId;
    }

    public void setLastLessonId(Integer lastLessonId) {
        this.lastLessonId = lastLessonId;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void increment() {
        this.consecutiveCount++;
        this.lastUpdated = LocalDateTime.now();
    }

    public void reset() {
        this.consecutiveCount = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public boolean hasReachedThreshold(int threshold) {
        return this.consecutiveCount >= threshold;
    }

    @Override
    public String toString() {
        return "ConsecutivityTracking{" +
                "trackingId=" + trackingId +
                ", studentId=" + studentId +
                ", trackingType=" + trackingType +
                ", consecutiveCount=" + consecutiveCount +
                ", lastLessonId=" + lastLessonId +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}