package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when bulk quiz grading is completed.
 */
public class QuizGradingCompletedEvent implements Event {
    private Integer quizId;
    private int studentsGraded;
    private Integer gradedBy;
    private LocalDateTime timestamp;
    
    public QuizGradingCompletedEvent(Integer quizId, int studentsGraded, Integer gradedBy) {
        this.quizId = quizId;
        this.studentsGraded = studentsGraded;
        this.gradedBy = gradedBy;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public Integer getQuizId() { return quizId; }
    public int getStudentsGraded() { return studentsGraded; }
    public Integer getGradedBy() { return gradedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "QuizGradingCompletedEvent{quiz=" + quizId + 
               ", studentsGraded=" + studentsGraded + "}";
    }
}