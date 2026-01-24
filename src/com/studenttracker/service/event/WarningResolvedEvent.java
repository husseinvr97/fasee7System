package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when a warning is resolved.
 * Consumed by notification systems and monitoring services.
 */
public class WarningResolvedEvent implements Event {
    
    private final Integer warningId;
    private final Integer studentId;
    private final LocalDateTime resolvedAt;
    private final String resolvedReason;
    
    public WarningResolvedEvent(Integer warningId, Integer studentId, 
                               LocalDateTime resolvedAt, String resolvedReason) {
        this.warningId = warningId;
        this.studentId = studentId;
        this.resolvedAt = resolvedAt;
        this.resolvedReason = resolvedReason;
    }
    
    public Integer getWarningId() {
        return warningId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public String getResolvedReason() {
        return resolvedReason;
    }
    
    @Override
    public String toString() {
        return "WarningResolvedEvent{" +
                "warningId=" + warningId +
                ", studentId=" + studentId +
                ", resolvedAt=" + resolvedAt +
                ", resolvedReason='" + resolvedReason + '\'' +
                '}';
    }
}