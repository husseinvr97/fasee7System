package com.studenttracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Lesson {
    private Integer lessonId;
    private LocalDate lessonDate;
    private String monthGroup;        // e.g., "Month 2"
    private LocalDateTime createdAt;
    private Integer createdBy;        // user_id
    
    // Constructors
    public Lesson() {}
    
    public Lesson(LocalDate lessonDate, String monthGroup, Integer createdBy) {
        this.lessonDate = lessonDate;
        this.monthGroup = monthGroup;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    
    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }
    
    public String getMonthGroup() { return monthGroup; }
    public void setMonthGroup(String monthGroup) { this.monthGroup = monthGroup; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    
    @Override
    public String toString() {
        return "Lesson{id=" + lessonId + ", date=" + lessonDate + ", month=" + monthGroup + "}";
    }
}