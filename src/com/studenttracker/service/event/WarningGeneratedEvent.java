package com.studenttracker.service.event;

import com.studenttracker.model.Warning.WarningType;
import java.time.LocalDateTime;

/**
 * Event published when a warning is generated for a student.
 * Consumed by notification systems and monitoring services.
 */
public class WarningGeneratedEvent implements Event {
    
    private final Integer warningId;
    private final Integer studentId;
    private final WarningType warningType;
    private final String reason;
    private final LocalDateTime createdAt;
    
    public WarningGeneratedEvent(Integer warningId, Integer studentId, WarningType warningType,
                                String reason, LocalDateTime createdAt) {
        this.warningId = warningId;
        this.studentId = studentId;
        this.warningType = warningType;
        this.reason = reason;
        this.createdAt = createdAt;
    }
    
    public Integer getWarningId() {
        return warningId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public WarningType getWarningType() {
        return warningType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public String toString() {
        return "WarningGeneratedEvent{" +
                "warningId=" + warningId +
                ", studentId=" + studentId +
                ", warningType=" + warningType +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}