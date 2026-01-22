package com.studenttracker.model;

import java.time.LocalDateTime;

public class Attendance {
    private Integer attendanceId;
    private Integer lessonId;
    private Integer studentId;
    private AttendanceStatus status;
    private LocalDateTime markedAt;
    private Integer markedBy;
    
    public enum AttendanceStatus {
        PRESENT, ABSENT
    }
    
    // Constructors
    public Attendance() {}
    
    public Attendance(Integer lessonId, Integer studentId, AttendanceStatus status, Integer markedBy) {
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.status = status;
        this.markedBy = markedBy;
        this.markedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Integer attendanceId) { this.attendanceId = attendanceId; }
    
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    
    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
    
    public Integer getMarkedBy() { return markedBy; }
    public void setMarkedBy(Integer markedBy) { this.markedBy = markedBy; }
    
    // Helper methods
    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT;
    }
    
    public boolean isAbsent() {
        return status == AttendanceStatus.ABSENT;
    }
    
    @Override
    public String toString() {
        return "Attendance{lesson=" + lessonId + ", student=" + studentId + ", status=" + status + "}";
    }
}