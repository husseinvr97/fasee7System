package com.studenttracker.service.event;

import java.time.LocalDateTime;

import com.studenttracker.model.Mission.MissionType;

public class MissionCompletedEvent implements Event {
    private final Integer missionId;
    private final Integer completedBy;
    private final Integer lessonId;
    private final MissionType type;
    private final LocalDateTime completedAt = LocalDateTime.now();
    
    public MissionCompletedEvent(Integer missionId, Integer completedBy, 
                                Integer lessonId, MissionType type) {
        this.missionId = missionId;
        this.completedBy = completedBy;
        this.lessonId = lessonId;
        this.type = type;
    }
    
    public Integer getMissionId() { return missionId; }
    public Integer getCompletedBy() { return completedBy; }
    public Integer getLessonId() { return lessonId; }
    public MissionType getType() { return type; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}