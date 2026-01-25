package com.studenttracker.service.event;

import java.time.LocalDateTime;
import java.util.List;

import com.studenttracker.model.Student;

/**
 * Event published when rankings change.
 */
public class Fasee7RankingsChangedEvent implements Event {
    
    private List<Student> topStudents; // top 10 or all
    private LocalDateTime updatedAt; // updatedAt
    
    public Fasee7RankingsChangedEvent(List<Student> topStudents, LocalDateTime updatedAt) {
        this.topStudents = topStudents;
        this.updatedAt = updatedAt;
    }
    
    public List<Student> getTopStudents() {
        return topStudents;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    @Override
    public String toString() {
        return "Fasee7RankingsChangedEvent{" +
                "topStudents=" + topStudents +
                ", updatedAt=" + updatedAt +
                '}';
    }
}