package com.studenttracker.service.event;

import static com.studenttracker.model.LessonTopic.TopicCategory;
import java.time.LocalDateTime;

public class PerformanceIndicatorCalculatedEvent implements Event {
    private final Integer studentId;
    private final TopicCategory category;
    private final Integer quizId;
    private final int piValue;
    private final int cumulativePi;
    private final LocalDateTime calculatedAt = LocalDateTime.now();

    public PerformanceIndicatorCalculatedEvent(Integer studentId, TopicCategory category, 
                                              Integer quizId, int piValue, int cumulativePi,
                                              LocalDateTime calculatedAt) {
        this.studentId = studentId;
        this.category = category;
        this.quizId = quizId;
        this.piValue = piValue;
        this.cumulativePi = cumulativePi;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public TopicCategory getCategory() {
        return category;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public int getPiValue() {
        return piValue;
    }

    public int getCumulativePi() {
        return cumulativePi;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    @Override
    public String toString() {
        return "PerformanceIndicatorCalculatedEvent{" +
                "studentId=" + studentId +
                ", category=" + category +
                ", quizId=" + quizId +
                ", piValue=" + piValue +
                ", cumulativePi=" + cumulativePi +
                ", calculatedAt=" + calculatedAt +
                '}';
    }
}