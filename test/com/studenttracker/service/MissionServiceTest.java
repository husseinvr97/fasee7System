package com.studenttracker.service;

import com.studenttracker.dao.MissionDAO;
import com.studenttracker.dao.MissionDraftDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.exception.UserNotFoundException;
import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionStatus;
import com.studenttracker.model.Mission.MissionType;
import com.studenttracker.model.MissionDraft;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.event.MissionAssignedEvent;
import com.studenttracker.service.event.MissionCompletedEvent;
import com.studenttracker.service.event.MissionDraftSavedEvent;
import com.studenttracker.service.event.MissionReassignedEvent;
import com.studenttracker.service.impl.MissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MissionService Tests")
public class MissionServiceTest {

    private MissionDAO missionDAO;
    private MissionDraftDAO missionDraftDAO;
    private UserDAO userDAO;
    private EventBusService eventBus;
    private MissionService missionService;

    @BeforeEach
    void setUp() {
        missionDAO = mock(MissionDAO.class);
        missionDraftDAO = mock(MissionDraftDAO.class);
        userDAO = mock(UserDAO.class);
        eventBus = mock(EventBusService.class);
        missionService = new MissionServiceImpl(missionDAO, missionDraftDAO, userDAO, eventBus);
    }

    @Test
    @DisplayName("Test 5.1: Assign Mission - Admin Assigns to Assistant")
    void testAssignMission_AdminAssignsToAssistant() {
        // Setup
        Integer lessonId = 50;
        MissionType type = MissionType.ATTENDANCE_HOMEWORK;
        Integer assignedTo = 5;
        Integer assignedBy = 1;

        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);

        User assistantUser = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistantUser.setUserId(5);
        assistantUser.setActive(true);

        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.findById(5)).thenReturn(assistantUser);
        when(missionDAO.findByLessonAndType(lessonId, type)).thenReturn(null);
        when(missionDAO.insert(any(Mission.class))).thenReturn(10);

        // Execute
        Integer missionId = missionService.assignMission(lessonId, type, assignedTo, assignedBy);

        // Verify
        assertEquals(10, missionId);
        
        // Verify DAO calls
        verify(userDAO).findById(1);
        verify(userDAO).findById(5);
        verify(missionDAO).findByLessonAndType(lessonId, type);
        
        ArgumentCaptor<Mission> missionCaptor = ArgumentCaptor.forClass(Mission.class);
        verify(missionDAO).insert(missionCaptor.capture());
        
        Mission capturedMission = missionCaptor.getValue();
        assertEquals(lessonId, capturedMission.getLessonId());
        assertEquals(type, capturedMission.getMissionType());
        assertEquals(assignedTo, capturedMission.getAssignedTo());
        assertEquals(assignedBy, capturedMission.getAssignedBy());
        assertEquals(MissionStatus.IN_PROGRESS, capturedMission.getStatus());
        assertNotNull(capturedMission.getAssignedAt());
        
        // Verify event published
        ArgumentCaptor<MissionAssignedEvent> eventCaptor = ArgumentCaptor.forClass(MissionAssignedEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        MissionAssignedEvent event = eventCaptor.getValue();
        assertEquals(10, event.getMissionId());
        assertEquals(lessonId, event.getLessonId());
        assertEquals(type, event.getType());
        assertEquals(assignedTo, event.getAssignedTo());
        assertEquals(assignedBy, event.getAssignedBy());
    }

    @Test
    @DisplayName("Test 5.2: Assign Mission - Non-Admin Tries to Assign")
    void testAssignMission_NonAdminTriesToAssign() {
        // Setup
        Integer assignedBy = 5;
        User assistantUser = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistantUser.setUserId(5);

        when(userDAO.findById(5)).thenReturn(assistantUser);

        // Execute & Verify
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            missionService.assignMission(50, MissionType.ATTENDANCE_HOMEWORK, 3, assignedBy);
        });

        assertEquals("Only admins can assign missions", exception.getMessage());
        verify(missionDAO, never()).insert(any(Mission.class));
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Test 5.3: Assign Mission - Duplicate Active Mission")
    void testAssignMission_DuplicateActiveMission() {
        // Setup
        Integer lessonId = 50;
        MissionType type = MissionType.ATTENDANCE_HOMEWORK;
        Integer assignedTo = 5;
        Integer assignedBy = 1;

        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);

        User assistantUser = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistantUser.setUserId(5);
        assistantUser.setActive(true);

        Mission existingMission = new Mission();
        existingMission.setMissionId(99);
        existingMission.setLessonId(lessonId);
        existingMission.setMissionType(type);
        existingMission.setStatus(MissionStatus.IN_PROGRESS);
        existingMission.setAssignedTo(3);

        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.findById(5)).thenReturn(assistantUser);
        when(missionDAO.findByLessonAndType(lessonId, type)).thenReturn(existingMission);

        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            missionService.assignMission(lessonId, type, assignedTo, assignedBy);
        });

        assertTrue(exception.getMessage().contains("Active mission"));
        verify(missionDAO, never()).insert(any(Mission.class));
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Test 5.4: Save Mission Draft - First Save")
    void testSaveMissionDraft_FirstSave() {
        // Setup
        Integer missionId = 10;
        String draftDataJson = "{\"attendanceRecords\":[{\"studentId\":1,\"status\":\"PRESENT\"}],\"completedCount\":1,\"totalCount\":152}";

        when(missionDraftDAO.findByMissionId(missionId)).thenReturn(null);
        when(missionDraftDAO.insert(any(MissionDraft.class))).thenReturn(1);

        // Execute
        boolean result = missionService.saveMissionDraft(missionId, draftDataJson);

        // Verify
        assertTrue(result);
        
        verify(missionDraftDAO).findByMissionId(missionId);
        
        ArgumentCaptor<MissionDraft> draftCaptor = ArgumentCaptor.forClass(MissionDraft.class);
        verify(missionDraftDAO).insert(draftCaptor.capture());
        
        MissionDraft capturedDraft = draftCaptor.getValue();
        assertEquals(missionId, capturedDraft.getMissionId());
        assertEquals(draftDataJson, capturedDraft.getDraftData());
        assertNotNull(capturedDraft.getLastSaved());
        
        verify(eventBus).publish(any(MissionDraftSavedEvent.class));
    }

    @Test
    @DisplayName("Test 5.5: Save Mission Draft - Update Existing")
    void testSaveMissionDraft_UpdateExisting() {
        // Setup
        Integer missionId = 10;
        String newDraftDataJson = "{\"attendanceRecords\":[...],\"completedCount\":50,\"totalCount\":152}";

        MissionDraft existingDraft = new MissionDraft();
        existingDraft.setDraftId(1);
        existingDraft.setMissionId(missionId);
        existingDraft.setDraftData("{\"old\":\"data\"}");
        existingDraft.setLastSaved(LocalDateTime.now().minusHours(1));

        when(missionDraftDAO.findByMissionId(missionId)).thenReturn(existingDraft);
        when(missionDraftDAO.update(any(MissionDraft.class))).thenReturn(true);

        // Execute
        boolean result = missionService.saveMissionDraft(missionId, newDraftDataJson);

        // Verify
        assertTrue(result);
        
        verify(missionDraftDAO).findByMissionId(missionId);
        verify(missionDraftDAO).update(existingDraft);
        verify(missionDraftDAO, never()).insert(any());
        
        assertEquals(newDraftDataJson, existingDraft.getDraftData());
        
        verify(eventBus).publish(any(MissionDraftSavedEvent.class));
    }

    @Test
    @DisplayName("Test 5.6: Complete Mission - Success")
    void testCompleteMission_Success() {
        // Setup
        Integer missionId = 10;
        Integer completedBy = 5;

        Mission mission = new Mission();
        mission.setMissionId(missionId);
        mission.setLessonId(50);
        mission.setMissionType(MissionType.ATTENDANCE_HOMEWORK);
        mission.setAssignedTo(5);
        mission.setStatus(MissionStatus.IN_PROGRESS);

        when(missionDAO.findById(missionId)).thenReturn(mission);
        when(missionDAO.update(any(Mission.class))).thenReturn(true);

        // Execute
        boolean result = missionService.completeMission(missionId, completedBy);

        // Verify
        assertTrue(result);
        
        verify(missionDAO).findById(missionId);
        verify(missionDAO).update(mission);
        
        assertEquals(MissionStatus.COMPLETED, mission.getStatus());
        assertNotNull(mission.getCompletedAt());
        
        verify(missionDraftDAO).deleteByMissionId(missionId);
        
        ArgumentCaptor<MissionCompletedEvent> eventCaptor = ArgumentCaptor.forClass(MissionCompletedEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        MissionCompletedEvent event = eventCaptor.getValue();
        assertEquals(missionId, event.getMissionId());
        assertEquals(completedBy, event.getCompletedBy());
    }

    @Test
    @DisplayName("Test 5.7: Complete Mission - Wrong User")
    void testCompleteMission_WrongUser() {
        // Setup
        Integer missionId = 10;
        Integer completedBy = 3;

        Mission mission = new Mission();
        mission.setMissionId(missionId);
        mission.setAssignedTo(5);
        mission.setStatus(MissionStatus.IN_PROGRESS);

        when(missionDAO.findById(missionId)).thenReturn(mission);

        // Execute & Verify
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            missionService.completeMission(missionId, completedBy);
        });

        assertEquals("Only the assigned user can complete this mission", exception.getMessage());
        
        verify(missionDAO, never()).update(any());
        verify(missionDraftDAO, never()).deleteByMissionId(anyInt());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Test 5.8: Complete Mission - Already Completed")
    void testCompleteMission_AlreadyCompleted() {
        // Setup
        Integer missionId = 10;
        Integer completedBy = 5;

        Mission mission = new Mission();
        mission.setMissionId(missionId);
        mission.setAssignedTo(5);
        mission.setStatus(MissionStatus.COMPLETED);
        mission.setCompletedAt(LocalDateTime.now().minusDays(1));

        when(missionDAO.findById(missionId)).thenReturn(mission);

        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            missionService.completeMission(missionId, completedBy);
        });

        assertEquals("Mission is already completed", exception.getMessage());
        verify(missionDAO, never()).update(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    @DisplayName("Test 5.9: Get Pending Missions for User")
    void testGetMissionsByUser() {
        // Setup
        Integer userId = 5;

        Mission mission1 = new Mission();
        mission1.setMissionId(10);
        mission1.setAssignedTo(5);
        mission1.setStatus(MissionStatus.IN_PROGRESS);

        Mission mission2 = new Mission();
        mission2.setMissionId(8);
        mission2.setAssignedTo(5);
        mission2.setStatus(MissionStatus.COMPLETED);

        List<Mission> missions = Arrays.asList(mission1, mission2);

        when(missionDAO.findByAssignedTo(userId)).thenReturn(missions);

        // Execute
        List<Mission> result = missionService.getMissionsByUser(userId);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(missionDAO).findByAssignedTo(userId);
    }

    @Test
    @DisplayName("Test 5.10: Reassign Mission - Admin Reassigns")
    void testReassignMission_AdminReassigns() {
        // Setup
        Integer missionId = 10;
        Integer newAssignedTo = 7;
        Integer reassignedBy = 1;

        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);

        User newAssistantUser = new User("assistant2", "hash", "New Assistant", UserRole.ASSISTANT);
        newAssistantUser.setUserId(7);
        newAssistantUser.setActive(true);

        Mission mission = new Mission();
        mission.setMissionId(missionId);
        mission.setAssignedTo(5);
        mission.setStatus(MissionStatus.IN_PROGRESS);

        when(userDAO.findById(1)).thenReturn(adminUser);
        when(userDAO.findById(7)).thenReturn(newAssistantUser);
        when(missionDAO.findById(missionId)).thenReturn(mission);
        when(missionDAO.update(any(Mission.class))).thenReturn(true);

        // Execute
        boolean result = missionService.reassignMission(missionId, newAssignedTo, reassignedBy);

        // Verify
        assertTrue(result);
        
        verify(userDAO).findById(1);
        verify(userDAO).findById(7);
        verify(missionDAO).findById(missionId);
        verify(missionDAO).update(mission);
        
        assertEquals(newAssignedTo, mission.getAssignedTo());
        
        ArgumentCaptor<MissionReassignedEvent> eventCaptor = ArgumentCaptor.forClass(MissionReassignedEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        MissionReassignedEvent event = eventCaptor.getValue();
        assertEquals(missionId, event.getMissionId());
        assertEquals(5, event.getOldAssignedTo());
        assertEquals(7, event.getNewAssignedTo());
        assertEquals(1, event.getReassignedBy());
    }
}