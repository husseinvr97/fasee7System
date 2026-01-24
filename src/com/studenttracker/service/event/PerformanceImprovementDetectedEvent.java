package com.studenttracker.service.event;

import com.studenttracker.model.PerformanceIndicator.TopicCategory;

public class PerformanceImprovementDetectedEvent implements Event {
    private final Integer studentId;
    private final TopicCategory category;
    private final int previousPi;
    private final int currentPi;
    private final int improvementAmount;

    public PerformanceImprovementDetectedEvent(Integer studentId, TopicCategory category,
                                               int previousPi, int currentPi, 
                                               int improvementAmount) {
        this.studentId = studentId;
        this.category = category;
        this.previousPi = previousPi;
        this.currentPi = currentPi;
        this.improvementAmount = improvementAmount;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public TopicCategory getCategory() {
        return category;
    }

    public int getPreviousPi() {
        return previousPi;
    }

    public int getCurrentPi() {
        return currentPi;
    }

    public int getImprovementAmount() {
        return improvementAmount;
    }

    @Override
    public String toString() {
        return "PerformanceImprovementDetectedEvent{" +
                "studentId=" + studentId +
                ", category=" + category +
                ", previousPi=" + previousPi +
                ", currentPi=" + currentPi +
                ", improvementAmount=" + improvementAmount +
                '}';
    }
}