package com.studenttracker.service.event;

import java.util.List;

import com.studenttracker.model.QuizQuestion;

/**
 * Event published when a student's quiz is graded.
 */
public class QuizGradedEvent implements Event {
    private final Integer quizId;
    private final Integer studentId;
    private final List<QuizQuestion> questions;
    private final Double pointsEarned;
    private final Integer enteredBy;
    
    public QuizGradedEvent(Integer quizId, Integer studentId, List<QuizQuestion> questions, Double pointsEarned, Integer enteredBy) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.questions = questions;
        this.pointsEarned = pointsEarned;
        this.enteredBy = enteredBy;
    }
    
    public Integer getQuizId() { return quizId; }
    public Integer getStudentId() { return studentId; }
    public List<QuizQuestion> getQuestionId() { return questions; }
    public Double getPointsEarned() { return pointsEarned; }
    public Integer getEnteredBy() { return enteredBy; }
    
    @Override
    public String toString() {
        return "QuizGradedEvent{" +
                "quizId=" + quizId +
                ", studentId=" + studentId +
                ", questions=" + questions +
                ", pointsEarned=" + pointsEarned +
                ", enteredBy=" + enteredBy +
                '}';
    }
}