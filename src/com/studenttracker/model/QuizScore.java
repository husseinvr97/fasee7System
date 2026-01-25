package com.studenttracker.model;

import java.time.LocalDateTime;

public class QuizScore {
    private Integer scoreId;
    private Integer quizId;
    private Integer studentId;
    private Integer questionId;
    private Double pointsEarned;
    private LocalDateTime enteredAt;
    private Integer enteredBy;

    public QuizScore() {
    }

    public QuizScore(Integer quizId, Integer studentId, Integer questionId, 
                     Double pointsEarned, LocalDateTime enteredAt, Integer enteredBy) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.questionId = questionId;
        this.pointsEarned = pointsEarned;
        this.enteredAt = enteredAt;
        this.enteredBy = enteredBy;
    }

    public Integer getScoreId() {
        return scoreId;
    }

    public void setScoreId(Integer scoreId) {
        this.scoreId = scoreId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Double getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public LocalDateTime getEnteredAt() {
        return enteredAt;
    }

    public void setEnteredAt(LocalDateTime enteredAt) {
        this.enteredAt = enteredAt;
    }

    public Integer getEnteredBy() {
        return enteredBy;
    }

    public void setEnteredBy(Integer enteredBy) {
        this.enteredBy = enteredBy;
    }

    @Override
    public String toString() {
        return "QuizScore{" +
                "scoreId=" + scoreId +
                ", quizId=" + quizId +
                ", studentId=" + studentId +
                ", questionId=" + questionId +
                ", pointsEarned=" + pointsEarned +
                ", enteredAt=" + enteredAt +
                ", enteredBy=" + enteredBy +
                '}';
    }
}