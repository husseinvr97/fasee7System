package com.studenttracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class QuizCategoryTotal {
    // Assuming TopicCategory enum - you should use the one from LessonTopic
    public enum TopicCategory {
        MATH,
        SCIENCE,
        ENGLISH,
        HISTORY,
        PROGRAMMING,
        OTHER
    }

    private Integer totalId;
    private Integer quizId;
    private Integer studentId;
    private TopicCategory category;
    private BigDecimal pointsEarned;
    private BigDecimal totalPoints;

    public QuizCategoryTotal() {
    }

    public QuizCategoryTotal(Integer quizId, Integer studentId, TopicCategory category, 
                             BigDecimal pointsEarned, BigDecimal totalPoints) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.category = category;
        this.pointsEarned = pointsEarned;
        this.totalPoints = totalPoints;
    }

    public Integer getTotalId() {
        return totalId;
    }

    public void setTotalId(Integer totalId) {
        this.totalId = totalId;
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

    public TopicCategory getCategory() {
        return category;
    }

    public void setCategory(TopicCategory category) {
        this.category = category;
    }

    public BigDecimal getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(BigDecimal pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(BigDecimal totalPoints) {
        this.totalPoints = totalPoints;
    }

    public BigDecimal getPercentage() {
        if (totalPoints == null || totalPoints.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return pointsEarned.divide(totalPoints, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    @Override
    public String toString() {
        return "QuizCategoryTotal{" +
                "totalId=" + totalId +
                ", quizId=" + quizId +
                ", studentId=" + studentId +
                ", category=" + category +
                ", pointsEarned=" + pointsEarned +
                ", totalPoints=" + totalPoints +
                ", percentage=" + getPercentage() + "%" +
                '}';
    }
}