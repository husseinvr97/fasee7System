package com.studenttracker.service.event;

import com.studenttracker.model.Quiz;
import java.time.LocalDateTime;

/**
 * Event published when a quiz is created.
 */
public class QuizCreatedEvent implements Event {
    private Quiz quiz;
    private Integer createdBy;
    private LocalDateTime timestamp;
    
    public QuizCreatedEvent(Quiz quiz, Integer createdBy) {
        this.quiz = quiz;
        this.createdBy = createdBy;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public Quiz getQuiz() { return quiz; }
    public Integer getCreatedBy() { return createdBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "QuizCreatedEvent{quiz=" + quiz + ", createdBy=" + createdBy + "}";
    }
}