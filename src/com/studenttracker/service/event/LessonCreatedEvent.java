package com.studenttracker.service.event;

import java.time.LocalDate;
import java.util.List;

public class LessonCreatedEvent implements Event {
    private Integer lessonId;
    private LocalDate lessonDate;
    private String lessonType;
    private List<Integer> topicIds;
    private Integer createdBy;

    public LessonCreatedEvent(Integer lessonId, LocalDate lessonDate, String lessonType, 
                             List<Integer> topicIds, Integer createdBy) {
        this.lessonId = lessonId;
        this.lessonDate = lessonDate;
        this.lessonType = lessonType;
        this.topicIds = topicIds;
        this.createdBy = createdBy;
    }

    // Getters
    public Integer getLessonId() { return lessonId; }
    public LocalDate getLessonDate() { return lessonDate; }
    public String getLessonType() { return lessonType; }
    public List<Integer> getTopicIds() { return topicIds; }
    public Integer getCreatedBy() { return createdBy; }

    @Override
    public String toString() {
        return "LessonCreatedEvent{lessonId=" + lessonId + ", date=" + lessonDate + "}";
    }
}