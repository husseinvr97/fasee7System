package com.studenttracker.service.event;

import com.studenttracker.model.Attendance.AttendanceStatus;
import java.time.LocalDateTime;

public class AttendanceMarkedEvent implements Event {
    private final Integer lessonId;
    private final Integer studentId;
    private final AttendanceStatus status;
    private final Integer markedBy;
    private final LocalDateTime markedAt;
    
    public AttendanceMarkedEvent(Integer lessonId, Integer studentId, 
                                AttendanceStatus status, Integer markedBy, 
                                LocalDateTime markedAt) {
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.status = status;
        this.markedBy = markedBy;
        this.markedAt = markedAt;
    }
    
    public Integer getLessonId() { return lessonId; }
    public Integer getStudentId() { return studentId; }
    public AttendanceStatus getStatus() { return status; }
    public Integer getMarkedBy() { return markedBy; }
    public LocalDateTime getMarkedAt() { return markedAt; }
    
    @Override
    public String toString() {
        return "AttendanceMarkedEvent{lesson=" + lessonId + ", student=" + studentId + 
               ", status=" + status + ", markedBy=" + markedBy + "}";
    }
}