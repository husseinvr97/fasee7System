package com.studenttracker.model;

import java.time.LocalDateTime;

public class PerformanceIndicator {
    // Reusing TopicCategory enum - should match the one from LessonTopic/QuizCategoryTotal
    public enum TopicCategory {
        MATH,
        SCIENCE,
        ENGLISH,
        HISTORY,
        PROGRAMMING,
        OTHER
    }

    private Integer piId;
    private Integer studentId;
    private TopicCategory category;
    private Integer quizId;
    private int correctAnswers;
    private int wrongAnswers;
    private int piValue;
    private int cumulativePi;
    private LocalDateTime calculatedAt;

    public PerformanceIndicator() {
    }

    public PerformanceIndicator(Integer studentId, TopicCategory category, Integer quizId, 
                                int correctAnswers, int wrongAnswers, int piValue, 
                                int cumulativePi, LocalDateTime calculatedAt) {
        this.studentId = studentId;
        this.category = category;
        this.quizId = quizId;
        this.correctAnswers = correctAnswers;
        this.wrongAnswers = wrongAnswers;
        this.piValue = piValue;
        this.cumulativePi = cumulativePi;
        this.calculatedAt = calculatedAt;
    }

    public Integer getPiId() {
        return piId;
    }

    public void setPiId(Integer piId) {
        this.piId = piId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public TopicCategory getCategory() {
        return category;
    }

    public void setCategory(TopicCategory category) {
        this.category = category;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    public int getPiValue() {
        return piValue;
    }

    public void setPiValue(int piValue) {
        this.piValue = piValue;
    }

    public int getCumulativePi() {
        return cumulativePi;
    }

    public void setCumulativePi(int cumulativePi) {
        this.cumulativePi = cumulativePi;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public int calculatePiValue() {
        return correctAnswers - wrongAnswers;
    }

    @Override
    public String toString() {
        return "PerformanceIndicator{" +
                "piId=" + piId +
                ", studentId=" + studentId +
                ", category=" + category +
                ", quizId=" + quizId +
                ", correctAnswers=" + correctAnswers +
                ", wrongAnswers=" + wrongAnswers +
                ", piValue=" + piValue +
                ", cumulativePi=" + cumulativePi +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}