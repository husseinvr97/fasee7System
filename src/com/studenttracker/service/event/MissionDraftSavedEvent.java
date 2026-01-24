package com.studenttracker.service.event;

public class MissionDraftSavedEvent implements Event {
    private final Integer missionId;
    
    public MissionDraftSavedEvent(Integer missionId) {
        this.missionId = missionId;
    }
    
    public Integer getMissionId() { return missionId; }
}