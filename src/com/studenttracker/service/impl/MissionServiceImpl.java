package com.studenttracker.service.impl;

import com.studenttracker.dao.MissionDAO;
import com.studenttracker.dao.MissionDraftDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.exception.UserNotFoundException;
import com.studenttracker.exception.ServiceException;
import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionStatus;
import com.studenttracker.model.Mission.MissionType;
import com.studenttracker.model.MissionDraft;
import com.studenttracker.model.User;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.MissionService;
import com.studenttracker.service.event.MissionAssignedEvent;
import com.studenttracker.service.event.MissionReassignedEvent;
import com.studenttracker.service.event.MissionCompletedEvent;
import com.studenttracker.service.event.MissionDraftSavedEvent;

import java.time.LocalDateTime;
import java.util.List;

public class MissionServiceImpl implements MissionService {
    
    private final MissionDAO missionDAO;
    private final MissionDraftDAO missionDraftDAO;
    private final UserDAO userDAO;
    private final EventBusService eventBus;
    
    public MissionServiceImpl(MissionDAO missionDAO, MissionDraftDAO missionDraftDAO, 
                             UserDAO userDAO, EventBusService eventBus) {
        this.missionDAO = missionDAO;
        this.missionDraftDAO = missionDraftDAO;
        this.userDAO = userDAO;
        this.eventBus = eventBus;
    }
    
    @Override
    public Integer assignMission(Integer lessonId, MissionType type, Integer assignedTo, Integer assignedBy) {
        // Validate assignedBy is Admin
        User assignerUser = userDAO.findById(assignedBy);
        if (assignerUser == null) {
            throw new UserNotFoundException("Assigner user not found: " + assignedBy);
        }
        if (!assignerUser.isAdmin()) {
            throw new UnauthorizedException("Only admins can assign missions");
        }
        
        // Validate assignedTo exists and is ACTIVE
        User assignedUser = userDAO.findById(assignedTo);
        if (assignedUser == null) {
            throw new UserNotFoundException("Assigned user not found: " + assignedTo);
        }
        if (!assignedUser.isActive()) {
            throw new ValidationException("Cannot assign mission to inactive user");
        }
        
        // Validate no active mission of same type for this lesson
        Mission existingMission = missionDAO.findByLessonAndType(lessonId, type);
        if (existingMission != null && existingMission.getStatus() == MissionStatus.IN_PROGRESS) {
            throw new ValidationException("Active mission of type " + type + " already exists for lesson " + lessonId);
        }
        
        // Create and insert mission
        Mission mission = new Mission();
        mission.setLessonId(lessonId);
        mission.setMissionType(type);
        mission.setAssignedTo(assignedTo);
        mission.setAssignedBy(assignedBy);
        mission.setAssignedAt(LocalDateTime.now());
        mission.setStatus(MissionStatus.IN_PROGRESS);
        
        Integer missionId = missionDAO.insert(mission);
        if (missionId == null) {
            throw new ServiceException("Failed to insert mission");
        }
        
        // Publish event
        eventBus.publish(new MissionAssignedEvent(missionId, lessonId, type, assignedTo, assignedBy));
        
        return missionId;
    }
    
    @Override
    public boolean reassignMission(Integer missionId, Integer newAssignedTo, Integer reassignedBy) {
        // Validate reassignedBy is Admin
        User reassignerUser = userDAO.findById(reassignedBy);
        if (reassignerUser == null) {
            throw new UserNotFoundException("Reassigner user not found: " + reassignedBy);
        }
        if (!reassignerUser.isAdmin()) {
            throw new UnauthorizedException("Only admins can reassign missions");
        }
        
        // Validate mission exists and is IN_PROGRESS
        Mission mission = missionDAO.findById(missionId);
        if (mission == null) {
            throw new ValidationException("Mission not found: " + missionId);
        }
        if (mission.getStatus() != MissionStatus.IN_PROGRESS) {
            throw new ValidationException("Can only reassign IN_PROGRESS missions");
        }
        
        // Validate new assignee exists and is ACTIVE
        User newAssignee = userDAO.findById(newAssignedTo);
        if (newAssignee == null) {
            throw new UserNotFoundException("New assignee not found: " + newAssignedTo);
        }
        if (!newAssignee.isActive()) {
            throw new ValidationException("Cannot reassign mission to inactive user");
        }
        
        // Update mission
        Integer oldAssignedTo = mission.getAssignedTo();
        mission.setAssignedTo(newAssignedTo);
        boolean success = missionDAO.update(mission);
        
        if (success) {
            // Publish event
            eventBus.publish(new MissionReassignedEvent(missionId, oldAssignedTo, newAssignedTo, reassignedBy));
        }
        
        return success;
    }
    
    @Override
    public boolean saveMissionDraft(Integer missionId, String draftDataJson) {
        // Check if draft exists
        MissionDraft existingDraft = missionDraftDAO.findByMissionId(missionId);
        
        boolean success;
        if (existingDraft != null) {
            // Update existing draft
            existingDraft.updateDraftData(draftDataJson);
            success = missionDraftDAO.update(existingDraft);
        } else {
            // Create new draft
            MissionDraft newDraft = new MissionDraft(missionId, draftDataJson, LocalDateTime.now());
            Integer draftId = missionDraftDAO.insert(newDraft);
            success = (draftId != null);
        }
        
        if (success) {
            // Publish event
            eventBus.publish(new MissionDraftSavedEvent(missionId , draftDataJson, LocalDateTime.now()));
        }
        
        return success;
    }
    
    @Override
    public MissionDraft getMissionDraft(Integer missionId) {
        return missionDraftDAO.findByMissionId(missionId);
    }
    
    @Override
    public boolean completeMission(Integer missionId, Integer completedBy) {
        // Validate mission exists
        Mission mission = missionDAO.findById(missionId);
        if (mission == null) {
            throw new ValidationException("Mission not found: " + missionId);
        }
        
        // Validate completedBy is assigned to this mission
        if (!mission.getAssignedTo().equals(completedBy)) {
            throw new UnauthorizedException("Only the assigned user can complete this mission");
        }
        
        // Update mission status to COMPLETED
        mission.complete();
        boolean missionUpdated = missionDAO.update(mission);
        
        if (!missionUpdated) {
            return false;
        }
        
        // Delete draft
        missionDraftDAO.deleteByMissionId(missionId);
        
        // Publish event
        eventBus.publish(new MissionCompletedEvent(missionId, completedBy, mission.getLessonId(), mission.getMissionType()));
        
        return true;
    }
    
    @Override
    public Mission getMissionById(Integer missionId) {
        return missionDAO.findById(missionId);
    }
    
    @Override
    public List<Mission> getPendingMissions() {
        return missionDAO.findByStatus(MissionStatus.IN_PROGRESS);
    }
    
    @Override
    public List<Mission> getCompletedMissions() {
        return missionDAO.findByStatus(MissionStatus.COMPLETED);
    }
    
    @Override
    public List<Mission> getMissionsByUser(Integer userId) {
        return missionDAO.findByAssignedTo(userId);
    }
    
    @Override
    public List<Mission> getMissionsByLesson(Integer lessonId) {
        return missionDAO.findByLessonId(lessonId);
    }
    
    @Override
    public int getPendingMissionCount() {
        return missionDAO.countByStatus(MissionStatus.IN_PROGRESS);
    }
}