package com.studenttracker.service.event;

import java.time.LocalDateTime;

public class MissionReassignedEvent implements Event {
    private final Integer missionId;
    private final Integer oldAssignedTo;
    private final Integer newAssignedTo;
    private final Integer reassignedBy;
    private final LocalDateTime occurredAt = LocalDateTime.now();
    
    public MissionReassignedEvent(Integer missionId, Integer oldAssignedTo, 
                                 Integer newAssignedTo, Integer reassignedBy) {
        this.missionId = missionId;
        this.oldAssignedTo = oldAssignedTo;
        this.newAssignedTo = newAssignedTo;
        this.reassignedBy = reassignedBy;
    }
    
    public Integer getMissionId() { return missionId; }
    public Integer getOldAssignedTo() { return oldAssignedTo; }
    public Integer getNewAssignedTo() { return newAssignedTo; }
    public Integer getReassignedBy() { return reassignedBy; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}