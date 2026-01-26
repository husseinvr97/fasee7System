package com.studenttracker.service.event;

import static com.studenttracker.model.LessonTopic.TopicCategory;
import java.time.LocalDateTime;

public class TargetAchievedEvent implements Event {
    private final Integer targetId;
    private final Integer studentId;
    private final TopicCategory category;
    private final LocalDateTime achievedAt;

    public TargetAchievedEvent(Integer targetId, Integer studentId, 
                              TopicCategory category, LocalDateTime achievedAt) {
        this.targetId = targetId;
        this.studentId = studentId;
        this.category = category;
        this.achievedAt = achievedAt;
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

    public LocalDateTime getAchievedAt() {
        return achievedAt;
    }

    @Override
    public String toString() {
        return "TargetAchievedEvent{" +
                "targetId=" + targetId +
                ", studentId=" + studentId +
                ", category=" + category +
                ", achievedAt=" + achievedAt +
                '}';
    }
}