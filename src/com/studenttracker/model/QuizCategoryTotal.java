package com.studenttracker.model;

import com.studenttracker.model.LessonTopic.TopicCategory;

public class QuizCategoryTotal {

    private Integer totalId;
    private Integer quizId;
    private Integer studentId;
    private TopicCategory category;
    private Double pointsEarned;
    private Double totalPoints;

    public QuizCategoryTotal() {
    }

    public QuizCategoryTotal(Integer quizId, Integer studentId, TopicCategory category, 
                             Double pointsEarned, Double totalPoints) {
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

    public Double getPointsEarned() {
        return pointsEarned;
    }

    public void setPointsEarned(Double pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Double getPercentage() {
        if (totalPoints == null || Double.compare( totalPoints,0.0) == 0) {
            return 0.0;
        }
        return (pointsEarned / 4) * 100;
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