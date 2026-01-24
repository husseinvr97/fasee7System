package com.studenttracker.service;

import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionType;
import com.studenttracker.model.MissionDraft;

import java.util.List;

public interface MissionService {
    
    // Mission Assignment
    Integer assignMission(Integer lessonId, MissionType type, Integer assignedTo, Integer assignedBy);
    boolean reassignMission(Integer missionId, Integer newAssignedTo, Integer reassignedBy);
    
    // Mission Execution
    boolean saveMissionDraft(Integer missionId, String draftDataJson);
    MissionDraft getMissionDraft(Integer missionId);
    boolean completeMission(Integer missionId, Integer completedBy);
    
    // Retrieval
    Mission getMissionById(Integer missionId);
    List<Mission> getPendingMissions();
    List<Mission> getCompletedMissions();
    List<Mission> getMissionsByUser(Integer userId);
    List<Mission> getMissionsByLesson(Integer lessonId);
    
    // Statistics
    int getPendingMissionCount();
}