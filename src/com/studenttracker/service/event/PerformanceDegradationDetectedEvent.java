package com.studenttracker.service.event;

import com.studenttracker.model.LessonTopic;

public class PerformanceDegradationDetectedEvent implements Event {
    private final Integer studentId;
    private final  LessonTopic.TopicCategory category;
    private final int previousPi;
    private final int currentPi;
    private final int degradationAmount;

    public PerformanceDegradationDetectedEvent(Integer studentId,  LessonTopic.TopicCategory category,
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

    public  LessonTopic.TopicCategory getCategory() {
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