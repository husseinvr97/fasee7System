package com.studenttracker.service.event;

import java.time.LocalDateTime;

import com.studenttracker.model.Mission.MissionType;

public class MissionAssignedEvent implements Event {
    private final Integer missionId;
    private final Integer lessonId;
    private final MissionType type;
    private final Integer assignedTo;
    private final Integer assignedBy;
    private final LocalDateTime assignedAt = LocalDateTime.now();
    
    public MissionAssignedEvent(Integer missionId, Integer lessonId, MissionType type, 
                               Integer assignedTo, Integer assignedBy) {
        this.missionId = missionId;
        this.lessonId = lessonId;
        this.type = type;
        this.assignedTo = assignedTo;
        this.assignedBy = assignedBy;
    }
    
    public Integer getMissionId() { return missionId; }
    public Integer getLessonId() { return lessonId; }
    public MissionType getType() { return type; }
    public Integer getAssignedTo() { return assignedTo; }
    public Integer getAssignedBy() { return assignedBy; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
}