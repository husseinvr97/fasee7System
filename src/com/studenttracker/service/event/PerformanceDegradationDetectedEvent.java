package com.studenttracker.service.event;

import com.studenttracker.model.PerformanceIndicator.TopicCategory;

public class PerformanceDegradationDetectedEvent implements Event {
    private final Integer studentId;
    private final TopicCategory category;
    private final int previousPi;
    private final int currentPi;
    private final int degradationAmount;

    public PerformanceDegradationDetectedEvent(Integer studentId, TopicCategory category,
                                               int previousPi, int currentPi, 
                                               int degradationAmount) {
        this.studentId = studentId;
        this.category = category;
        this.previousPi = previousPi;
        this.currentPi = currentPi;
        this.degradationAmount = degradationAmount;
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

    public int getDegradationAmount() {
        return degradationAmount;
    }

    @Override
    public String toString() {
        return "PerformanceDegradationDetectedEvent{" +
                "studentId=" + studentId +
                ", category=" + category +
                ", previousPi=" + previousPi +
                ", currentPi=" + currentPi +
                ", degradationAmount=" + degradationAmount +
                '}';
    }
}