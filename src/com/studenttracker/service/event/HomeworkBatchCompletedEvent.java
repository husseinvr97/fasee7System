package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when a batch of homework records is completed.
 */
public class HomeworkBatchCompletedEvent implements Event {
    private int count;
    private Integer markedBy;
    private LocalDateTime timestamp;
    
    public HomeworkBatchCompletedEvent(int count, Integer markedBy) {
        this.count = count;
        this.markedBy = markedBy;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public int getCount() { return count; }
    public Integer getMarkedBy() { return markedBy; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "HomeworkBatchCompletedEvent{count=" + count + ", markedBy=" + markedBy + "}";
    }
}