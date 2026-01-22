package com.studenttracker.model;

import java.time.LocalDateTime;

public class Homework {
    private Integer homeworkId;
    private Integer lessonId;
    private Integer studentId;
    private HomeworkStatus status;
    private LocalDateTime markedAt;
    private Integer markedBy;
    
    public enum HomeworkStatus {
        DONE(3),                  // 3 points
        PARTIALLY_DONE(1),        // 1 point
        NOT_DONE(0);              // 0 points
        
        private final int points;
        
        HomeworkStatus(int points) {
            this.points = points;
        }
        
        public int getPoints() {
            return points;
        }
    }
    
    // Constructors
    public Homework() {}
    
    public Homework(Integer lessonId, Integer studentId, HomeworkStatus status, Integer markedBy) {
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.status = status;
        this.markedBy = markedBy;
        this.markedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getHomeworkId() { return homeworkId; }
    public void setHomeworkId(Integer homeworkId) { this.homeworkId = homeworkId; }
    
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    
    public HomeworkStatus getStatus() { return status; }
    public void setStatus(HomeworkStatus status) { this.status = status; }
    
    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
    
    public Integer getMarkedBy() { return markedBy; }
    public void setMarkedBy(Integer markedBy) { this.markedBy = markedBy; }
    
    // Helper methods
    public int getPoints() {
        return status.getPoints();
    }
    
    @Override
    public String toString() {
        return "Homework{lesson=" + lessonId + ", student=" + studentId + ", status=" + status + "}";
    }
}