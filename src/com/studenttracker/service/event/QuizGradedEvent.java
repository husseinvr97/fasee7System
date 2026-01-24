package com.studenttracker.service.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when a student's quiz is graded.
 */
public class QuizGradedEvent implements Event {
    private Integer quizId;
    private Integer studentId;
    private BigDecimal totalScore;
    private Integer gradedBy;
    private LocalDateTime timestamp;
    
    public QuizGradedEvent(Integer quizId, Integer studentId, 
                          BigDecimal totalScore, Integer gradedBy) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.totalScore = totalScore;
        this.gradedBy = gradedBy;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public Integer getQuizId() { return quizId; }
    public Integer getStudentId() { return studentId; }
    public BigDecimal getTotalScore() { return totalScore; }
    public Integer getGradedBy() { return gradedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "QuizGradedEvent{quiz=" + quizId + ", student=" + studentId + 
               ", score=" + totalScore + "}";
    }
}