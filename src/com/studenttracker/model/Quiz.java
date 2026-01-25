package com.studenttracker.model;

import java.time.LocalDateTime;

public class Quiz {
    private Integer quizId;
    private Integer lessonId;
    private byte[] quizPdfData;       // Binary PDF data (or String pdfPath if storing path)
    private Double totalMarks;
    private LocalDateTime createdAt;
    private Integer createdBy;
    
    // Constructors
    public Quiz() {}
    
    public Quiz(Integer lessonId, byte[] quizPdfData, Double totalMarks, Integer createdBy) {
        this.lessonId = lessonId;
        this.quizPdfData = quizPdfData;
        this.totalMarks = totalMarks;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getQuizId() { return quizId; }
    public void setQuizId(Integer quizId) { this.quizId = quizId; }
    
    public Integer getLessonId() { return lessonId; }
    public void setLessonId(Integer lessonId) { this.lessonId = lessonId; }
    
    public byte[] getQuizPdfData() { return quizPdfData; }
    public void setQuizPdfData(byte[] quizPdfData) { this.quizPdfData = quizPdfData; }
    
    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    
    @Override
    public String toString() {
        return "Quiz{id=" + quizId + ", lesson=" + lessonId + ", totalMarks=" + totalMarks + "}";
    }
}