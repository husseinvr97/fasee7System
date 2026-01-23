package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when a student is archived.
 * Listeners can react to notify admin, update reports, archive attendance records, etc.
 */
public class StudentArchivedEvent implements Event {
    
    private final Integer studentId;
    private final LocalDateTime archivedAt;
    private final Integer archivedBy;
    private final String reason;  // e.g., "3 consecutive absences"
    
    public StudentArchivedEvent(Integer studentId, LocalDateTime archivedAt, 
                               Integer archivedBy, String reason) {
        this.studentId = studentId;
        this.archivedAt = archivedAt;
        this.archivedBy = archivedBy;
        this.reason = reason;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }
    
    public Integer getArchivedBy() {
        return archivedBy;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return "StudentArchivedEvent{" +
                "studentId=" + studentId +
                ", archivedAt=" + archivedAt +
                ", archivedBy=" + archivedBy +
                ", reason='" + reason + '\'' +
                '}';
    }
}