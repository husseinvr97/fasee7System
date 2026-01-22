package com.studenttracker.model;

import java.time.LocalDateTime;

public class BehavioralIncident {
    public enum IncidentType {
        LATE,
        DISRESPECTFUL,
        LEFT_EARLY,
        OTHER
    }

    private Integer incidentId;
    private Integer studentId;
    private Integer lessonId;
    private IncidentType incidentType;
    private String notes;
    private LocalDateTime createdAt;
    private Integer createdBy;

    public BehavioralIncident() {
    }

    public BehavioralIncident(Integer studentId, Integer lessonId, IncidentType incidentType, 
                              String notes, LocalDateTime createdAt, Integer createdBy) {
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.incidentType = incidentType;
        this.notes = notes;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public Integer getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Integer incidentId) {
        this.incidentId = incidentId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "BehavioralIncident{" +
                "incidentId=" + incidentId +
                ", studentId=" + studentId +
                ", lessonId=" + lessonId +
                ", incidentType=" + incidentType +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                '}';
    }
}