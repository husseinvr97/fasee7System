package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an archived student is restored.
 * Listeners can react to send re-enrollment notification, reset tracking data, etc.
 */
public class StudentRestoredEvent {
    
    private final Integer studentId;
    private final LocalDateTime restoredAt;
    private final Integer restoredBy;
    
    public StudentRestoredEvent(Integer studentId, LocalDateTime restoredAt, Integer restoredBy) {
        this.studentId = studentId;
        this.restoredAt = restoredAt;
        this.restoredBy = restoredBy;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public LocalDateTime getRestoredAt() {
        return restoredAt;
    }
    
    public Integer getRestoredBy() {
        return restoredBy;
    }
    
    @Override
    public String toString() {
        return "StudentRestoredEvent{" +
                "studentId=" + studentId +
                ", restoredAt=" + restoredAt +
                ", restoredBy=" + restoredBy +
                '}';
    }
}