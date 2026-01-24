package com.studenttracker.service.event;

import com.studenttracker.model.ConsecutivityTracking.TrackingType;
import java.time.LocalDateTime;

/**
 * Event published when a student reaches a consecutivity threshold.
 * 
 * Thresholds:
 * - Absence: 2 (WARNING), 3 (ARCHIVAL)
 * - Behavioral: 2 (WARNING)
 * 
 * Consumed by Warning System and Student Archival System.
 */
public class ConsecutiveThresholdReachedEvent implements Event {
    
    private final Integer studentId;
    private final TrackingType trackingType;
    private final int consecutiveCount;
    private final int threshold;
    private final String thresholdType; // "WARNING" or "ARCHIVAL"
    private final LocalDateTime triggeredAt;
    
    public ConsecutiveThresholdReachedEvent(Integer studentId, TrackingType trackingType,
                                           int consecutiveCount, int threshold,
                                           String thresholdType, LocalDateTime triggeredAt) {
        this.studentId = studentId;
        this.trackingType = trackingType;
        this.consecutiveCount = consecutiveCount;
        this.threshold = threshold;
        this.thresholdType = thresholdType;
        this.triggeredAt = triggeredAt;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public TrackingType getTrackingType() {
        return trackingType;
    }
    
    public int getConsecutiveCount() {
        return consecutiveCount;
    }
    
    public int getThreshold() {
        return threshold;
    }
    
    public String getThresholdType() {
        return thresholdType;
    }
    
    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }
    
    public boolean isWarningThreshold() {
        return "WARNING".equals(thresholdType);
    }
    
    public boolean isArchivalThreshold() {
        return "ARCHIVAL".equals(thresholdType);
    }
    
    @Override
    public String toString() {
        return "ConsecutiveThresholdReachedEvent{" +
                "studentId=" + studentId +
                ", trackingType=" + trackingType +
                ", consecutiveCount=" + consecutiveCount +
                ", threshold=" + threshold +
                ", thresholdType='" + thresholdType + '\'' +
                ", triggeredAt=" + triggeredAt +
                '}';
    }
}