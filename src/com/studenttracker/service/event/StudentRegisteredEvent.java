package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when a new student is registered.
 * Listeners can react to send welcome SMS, log audit trail, update statistics, etc.
 */
public class StudentRegisteredEvent {
    
    private final Integer studentId;
    private final String fullName;
    private final LocalDateTime registrationDate;
    private final Integer registeredBy;
    
    public StudentRegisteredEvent(Integer studentId, String fullName, 
                                 LocalDateTime registrationDate, Integer registeredBy) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.registrationDate = registrationDate;
        this.registeredBy = registeredBy;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }
    
    public Integer getRegisteredBy() {
        return registeredBy;
    }
    
    @Override
    public String toString() {
        return "StudentRegisteredEvent{" +
                "studentId=" + studentId +
                ", fullName='" + fullName + '\'' +
                ", registrationDate=" + registrationDate +
                ", registeredBy=" + registeredBy +
                '}';
    }
}