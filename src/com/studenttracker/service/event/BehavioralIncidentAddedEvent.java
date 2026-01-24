package com.studenttracker.service.event;

import com.studenttracker.model.BehavioralIncident.IncidentType;
import java.time.LocalDateTime;

/**
 * Event published when a behavioral incident is added.
 * Consumed by the Warning System to check for warning triggers.
 */
public class BehavioralIncidentAddedEvent implements Event {
    
    private final Integer incidentId;
    private final Integer studentId;
    private final Integer lessonId;
    private final IncidentType incidentType;
    private final Integer createdBy;
    private final LocalDateTime createdAt;
    
    public BehavioralIncidentAddedEvent(Integer incidentId, Integer studentId, Integer lessonId,
                                       IncidentType incidentType, Integer createdBy, 
                                       LocalDateTime createdAt) {
        this.incidentId = incidentId;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.incidentType = incidentType;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
    
    public Integer getIncidentId() {
        return incidentId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public Integer getLessonId() {
        return lessonId;
    }
    
    public IncidentType getIncidentType() {
        return incidentType;
    }
    
    public Integer getCreatedBy() {
        return createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    @Override
    public String toString() {
        return "BehavioralIncidentAddedEvent{" +
                "incidentId=" + incidentId +
                ", studentId=" + studentId +
                ", lessonId=" + lessonId +
                ", incidentType=" + incidentType +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}