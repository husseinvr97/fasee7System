package com.studenttracker.service.event;

public class StudentUpdatedEvent implements Event 
{
    private final Integer studentId;
    private final Integer updatedBy;
    public StudentUpdatedEvent(Integer studentId, Integer updatedBy) {
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
        return "StudentUpdatedEvent [studentId=" + studentId + ", updatedBy=" + updatedBy + "]";
    }
}
