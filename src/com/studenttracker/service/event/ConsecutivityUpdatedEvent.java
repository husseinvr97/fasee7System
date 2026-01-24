package com.studenttracker.service.event;

import com.studenttracker.model.ConsecutivityTracking.TrackingType;
import java.time.LocalDateTime;

/**
 * Event published when consecutivity tracking is updated for a student.
 * This event is fired every time tracking changes (increment or reset).
 */
public class ConsecutivityUpdatedEvent implements Event {
    
    private final Integer studentId;
    private final TrackingType trackingType;
    private final int consecutiveCount;
    private final Integer lastLessonId;
    private final LocalDateTime updatedAt;
    
    public ConsecutivityUpdatedEvent(Integer studentId, TrackingType trackingType, 
                                    int consecutiveCount, Integer lastLessonId,
                                    LocalDateTime updatedAt) {
        this.studentId = studentId;
        this.trackingType = trackingType;
        this.consecutiveCount = consecutiveCount;
        this.lastLessonId = lastLessonId;
        this.updatedAt = updatedAt;
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
    
    public Integer getLastLessonId() {
        return lastLessonId;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public String toString() {
        return "ConsecutivityUpdatedEvent{" +
                "studentId=" + studentId +
                ", trackingType=" + trackingType +
                ", consecutiveCount=" + consecutiveCount +
                ", lastLessonId=" + lastLessonId +
                ", updatedAt=" + updatedAt +
                '}';
    }
}