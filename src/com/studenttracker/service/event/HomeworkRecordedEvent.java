package com.studenttracker.service.event;

import com.studenttracker.model.Homework;
import java.time.LocalDateTime;

/**
 * Event published when homework is recorded or updated.
 */
public class HomeworkRecordedEvent implements Event {
    private Homework homework;
    private Integer markedBy;
    private LocalDateTime timestamp;
    
    public HomeworkRecordedEvent(Homework homework, Integer markedBy) {
        this.homework = homework;
        this.markedBy = markedBy;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public Homework getHomework() { return homework; }
    public Integer getMarkedBy() { return markedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "HomeworkRecordedEvent{homework=" + homework + ", markedBy=" + markedBy + "}";
    }
}