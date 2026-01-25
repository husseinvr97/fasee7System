package com.studenttracker.service.event;


import com.studenttracker.model.Homework.HomeworkStatus;



/**
 * Event published when homework is recorded or updated.
 */
public class HomeworkRecordedEvent implements Event {
    private final Integer lessonId;
    private final Integer studentId;
    private final HomeworkStatus status; // DONE/PARTIALLY_DONE/NOT_DONE
    private final Integer markedBy;
    
    public HomeworkRecordedEvent(Integer lessonId, Integer studentId, HomeworkStatus status ,Integer markedBy) {
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.status = status;
        this.markedBy = markedBy;
    }
    
    // Getters
    public Integer getLessonId() {
        return lessonId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public HomeworkStatus getStatus() {
        return status;
    }
    
    public Integer getMarkedBy() {
        return markedBy;
    }
    
    @Override
    public String toString() {
        return "HomeworkRecordedEvent{" +
                "lesson=" + lessonId +
                ", student=" + studentId +
                ", status=" + status +
                ", markedBy=" + markedBy +
                '}';
    }
}