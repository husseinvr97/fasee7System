package com.studenttracker.service.event;

public class LessonUpdatedEvent implements Event
{
    private final Integer studentId;
    private final Integer updatedBy;
    public LessonUpdatedEvent(Integer studentId, Integer updatedBy) {
        this.studentId = studentId;
        this.updatedBy = updatedBy;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public String toString() {
        return "LessonUpdatedEvent [studentId=" + studentId + ", updatedBy=" + updatedBy + "]";
    }
}
