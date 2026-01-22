package com.studenttracker.model;

import java.time.LocalDateTime;

public class MissionDraft {
    private Integer draftId;
    private Integer missionId;
    private String draftData;
    private LocalDateTime lastSaved;

    public MissionDraft() {
    }

    public MissionDraft(Integer missionId, String draftData, LocalDateTime lastSaved) {
        this.missionId = missionId;
        this.draftData = draftData;
        this.lastSaved = lastSaved;
    }

    public Integer getDraftId() {
        return draftId;
    }

    public void setDraftId(Integer draftId) {
        this.draftId = draftId;
    }

    public Integer getMissionId() {
        return missionId;
    }

    public void setMissionId(Integer missionId) {
        this.missionId = missionId;
    }

    public String getDraftData() {
        return draftData;
    }

    public void setDraftData(String draftData) {
        this.draftData = draftData;
    }

    public LocalDateTime getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(LocalDateTime lastSaved) {
        this.lastSaved = lastSaved;
    }

    public void updateDraftData(String jsonData) {
        this.draftData = jsonData;
        this.lastSaved = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "MissionDraft{" +
                "draftId=" + draftId +
                ", missionId=" + missionId +
                ", draftData='" + draftData + '\'' +
                ", lastSaved=" + lastSaved +
                '}';
    }
}