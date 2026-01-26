package com.studenttracker.service.event;

import static com.studenttracker.model.LessonTopic.TopicCategory;
import java.time.LocalDateTime;

public class TargetCreatedEvent implements Event {
    private final Integer targetId;
    private final Integer studentId;
    private final TopicCategory category;
    private final int targetPiValue;
    private final LocalDateTime createdAt = LocalDateTime.now();

    public TargetCreatedEvent(Integer targetId, Integer studentId, TopicCategory category,
                             int targetPiValue, LocalDateTime createdAt) {
        this.targetId = targetId;
        this.studentId = studentId;
        this.category = category;
        this.targetPiValue = targetPiValue;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public TopicCategory getCategory() {
        return category;
    }

    public int getTargetPiValue() {
        return targetPiValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "TargetCreatedEvent{" +
                "targetId=" + targetId +
                ", studentId=" + studentId +
                ", category=" + category +
                ", targetPiValue=" + targetPiValue +
                ", createdAt=" + createdAt +
                '}';
    }
}