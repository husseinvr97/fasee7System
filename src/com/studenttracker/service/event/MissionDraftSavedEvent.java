package com.studenttracker.service.event;

import java.time.LocalDateTime;

public class MissionDraftSavedEvent implements Event {
    private final Integer missionId;
    private final String draftData;
    private final LocalDateTime savedAt = LocalDateTime.now();
    
    public MissionDraftSavedEvent(Integer missionId, String draftData, LocalDateTime savedAt) {
        this.missionId = missionId;
        this.draftData = draftData;
    }
    
    public Integer getMissionId() { return missionId; }
    public String getDraftData() { return draftData; }
    public LocalDateTime getSavedAt() { return savedAt; }
}