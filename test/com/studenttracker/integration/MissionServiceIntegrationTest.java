package com.studenttracker.integration;

import com.studenttracker.dao.MissionDAO;
import com.studenttracker.dao.MissionDraftDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.dao.LessonDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Lesson;
import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionStatus;
import com.studenttracker.model.Mission.MissionType;
import com.studenttracker.model.MissionDraft;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.MissionService;
import com.studenttracker.service.event.MissionAssignedEvent;
import com.studenttracker.service.event.MissionCompletedEvent;
import com.studenttracker.service.event.MissionDraftSavedEvent;
import com.studenttracker.service.event.MissionReassignedEvent;
import com.studenttracker.service.impl.MissionServiceImpl;
import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MissionService Integration Tests")
public class MissionServiceIntegrationTest extends BaseIntegrationTest {

    private MissionService missionService;
    private MissionDAO missionDAO;
    private MissionDraftDAO missionDraftDAO;
    private UserDAO userDAO;
    private LessonDAO lessonDAO;
    private EventBusService eventBus;
    private TestEventListener eventListener;

    // Test data IDs
    private Integer adminUserId;
    private Integer assistant1UserId;
    private Integer assistant2UserId;
    private Integer lessonId;

    @BeforeEach
    void setUp() {
        missionDAO = TestDAOFactory.createMissionDAO();
        missionDraftDAO = TestDAOFactory.createMissionDraftDAO();
        userDAO = TestDAOFactory.createUserDAO();
        lessonDAO = TestDAOFactory.createLessonDAO();
        
        eventBus = EventBusService.getInstance();
        eventListener = new TestEventListener();
        eventBus.register(eventListener);
        
        missionService = new MissionServiceImpl(missionDAO, missionDraftDAO, userDAO, eventBus);

        // Set up test data
        setupTestData();
    }

    private void setupTestData() {
        // Create admin user
        User admin = new User("admin", "hash123", "Admin User", UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setActive(true);
        adminUserId = userDAO.insert(admin);

        // Create assistant users
        User assistant1 = new User("assistant1", "hash123", "Assistant One", UserRole.ASSISTANT);
        assistant1.setCreatedAt(LocalDateTime.now());
        assistant1.setActive(true);
        assistant1UserId = userDAO.insert(assistant1);

        User assistant2 = new User("assistant2", "hash123", "Assistant Two", UserRole.ASSISTANT);
        assistant2.setCreatedAt(LocalDateTime.now());
        assistant2.setActive(true);
        assistant2UserId = userDAO.insert(assistant2);

        // Create a lesson
        Lesson lesson = new Lesson(LocalDate.now(), "2025-01", adminUserId);
        lessonId = lessonDAO.insert(lesson);
    }

    @Test
    @DisplayName("Integration Test 5.1: Assign Mission - Admin Assigns to Assistant")
    void testAssignMission_AdminAssignsToAssistant_Integration() {
        // Execute
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        // Verify mission was created
        assertNotNull(missionId);
        Mission mission = missionDAO.findById(missionId);
        assertNotNull(mission);
        assertEquals(lessonId, mission.getLessonId());
        assertEquals(MissionType.ATTENDANCE_HOMEWORK, mission.getMissionType());
        assertEquals(assistant1UserId, mission.getAssignedTo());
        assertEquals(adminUserId, mission.getAssignedBy());
        assertEquals(MissionStatus.IN_PROGRESS, mission.getStatus());
        assertNotNull(mission.getAssignedAt());
        assertNull(mission.getCompletedAt());

        // Verify event was published
        assertEquals(1, eventListener.missionAssignedEvents.size());
        MissionAssignedEvent event = eventListener.missionAssignedEvents.get(0);
        assertEquals(missionId, event.getMissionId());
        assertEquals(lessonId, event.getLessonId());
        assertEquals(MissionType.ATTENDANCE_HOMEWORK, event.getType());
    }

    @Test
    @DisplayName("Integration Test 5.2: Assign Mission - Non-Admin Tries to Assign")
    void testAssignMission_NonAdminTriesToAssign_Integration() {
        // Execute & Verify
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            missionService.assignMission(
                lessonId, 
                MissionType.ATTENDANCE_HOMEWORK, 
                assistant2UserId, 
                assistant1UserId  // Assistant trying to assign
            );
        });

        assertEquals("Only admins can assign missions", exception.getMessage());
        
        // Verify no mission was created
        List<Mission> missions = missionDAO.findByLessonId(lessonId);
        assertTrue(missions.isEmpty());
        
        // Verify no event was published
        assertTrue(eventListener.missionAssignedEvents.isEmpty());
    }

    @Test
    @DisplayName("Integration Test 5.3: Assign Mission - Duplicate Active Mission")
    void testAssignMission_DuplicateActiveMission_Integration() {
        // First assignment
        missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        // Try to assign duplicate
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            missionService.assignMission(
                lessonId, 
                MissionType.ATTENDANCE_HOMEWORK, 
                assistant2UserId, 
                adminUserId
            );
        });

        assertTrue(exception.getMessage().contains("Active mission"));
        
        // Verify only one mission exists
        List<Mission> missions = missionDAO.findByLessonId(lessonId);
        assertEquals(1, missions.size());
    }

    @Test
    @DisplayName("Integration Test 5.4: Save Mission Draft - First Save")
    void testSaveMissionDraft_FirstSave_Integration() {
        // Create a mission first
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        String draftData = "{\"attendanceRecords\":[{\"studentId\":1,\"status\":\"PRESENT\"}],\"completedCount\":1,\"totalCount\":152}";

        // Execute
        boolean result = missionService.saveMissionDraft(missionId, draftData);

        // Verify
        assertTrue(result);
        
        MissionDraft draft = missionDraftDAO.findByMissionId(missionId);
        assertNotNull(draft);
        assertEquals(missionId, draft.getMissionId());
        assertEquals(draftData, draft.getDraftData());
        assertNotNull(draft.getLastSaved());

        // Verify event
        assertEquals(1, eventListener.missionDraftSavedEvents.size());
    }

    @Test
    @DisplayName("Integration Test 5.5: Save Mission Draft - Update Existing")
    void testSaveMissionDraft_UpdateExisting_Integration() {
        // Create mission and first draft
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        String firstDraft = "{\"completedCount\":1}";
        missionService.saveMissionDraft(missionId, firstDraft);

        // Get draft ID
        MissionDraft draft1 = missionDraftDAO.findByMissionId(missionId);
        Integer draftId = draft1.getDraftId();
        LocalDateTime firstSaveTime = draft1.getLastSaved();

        // Wait a bit to ensure timestamp difference
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // Update draft
        String secondDraft = "{\"completedCount\":50}";
        boolean result = missionService.saveMissionDraft(missionId, secondDraft);

        // Verify
        assertTrue(result);
        
        MissionDraft draft2 = missionDraftDAO.findByMissionId(missionId);
        assertNotNull(draft2);
        assertEquals(draftId, draft2.getDraftId()); // Same ID
        assertEquals(secondDraft, draft2.getDraftData());
        assertTrue(draft2.getLastSaved().isAfter(firstSaveTime)); // Updated timestamp

        // Verify only one draft exists
        List<MissionDraft> allDrafts = missionDraftDAO.findAll();
        assertEquals(1, allDrafts.size());
    }

    @Test
    @DisplayName("Integration Test 5.6: Complete Mission - Success")
    void testCompleteMission_Success_Integration() {
        // Create mission and draft
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        String draftData = "{\"data\":\"test\"}";
        missionService.saveMissionDraft(missionId, draftData);

        // Verify draft exists
        assertNotNull(missionDraftDAO.findByMissionId(missionId));

        // Execute
        boolean result = missionService.completeMission(missionId, assistant1UserId);

        // Verify
        assertTrue(result);
        
        Mission mission = missionDAO.findById(missionId);
        assertEquals(MissionStatus.COMPLETED, mission.getStatus());
        assertNotNull(mission.getCompletedAt());

        // Verify draft was deleted
        assertNull(missionDraftDAO.findByMissionId(missionId));

        // Verify event
        assertEquals(1, eventListener.missionCompletedEvents.size());
        MissionCompletedEvent event = eventListener.missionCompletedEvents.get(0);
        assertEquals(missionId, event.getMissionId());
        assertEquals(assistant1UserId, event.getCompletedBy());
    }

    @Test
    @DisplayName("Integration Test 5.7: Complete Mission - Wrong User")
    void testCompleteMission_WrongUser_Integration() {
        // Create mission assigned to assistant1
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        // Try to complete with different user
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            missionService.completeMission(missionId, assistant2UserId);
        });

        assertEquals("Only the assigned user can complete this mission", exception.getMessage());
        
        // Verify mission still in progress
        Mission mission = missionDAO.findById(missionId);
        assertEquals(MissionStatus.IN_PROGRESS, mission.getStatus());
        assertNull(mission.getCompletedAt());

        // Verify no event published
        assertTrue(eventListener.missionCompletedEvents.isEmpty());
    }

    @Test
    @DisplayName("Integration Test 5.8: Complete Mission - Already Completed")
    void testCompleteMission_AlreadyCompleted_Integration() {
        // Create and complete mission
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        missionService.completeMission(missionId, assistant1UserId);

        // Try to complete again
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            missionService.completeMission(missionId, assistant1UserId);
        });

        assertEquals("Mission is already completed", exception.getMessage());
    }

    @Test
    @DisplayName("Integration Test 5.9: Get Pending Missions for User")
    void testGetMissionsByUser_Integration() {
        // Create missions for assistant1
        Integer mission1 = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        // Create another lesson and mission
        Lesson lesson2 = new Lesson(LocalDate.now().plusDays(1), "2025-01", adminUserId);
        Integer lessonId2 = lessonDAO.insert(lesson2);
        
        Integer mission2 = missionService.assignMission(
            lessonId2, 
            MissionType.QUIZ_GRADING, 
            assistant1UserId, 
            adminUserId
        );

        // Complete one mission
        missionService.completeMission(mission1, assistant1UserId);

        // Get all missions for user
        List<Mission> missions = missionService.getMissionsByUser(assistant1UserId);

        // Verify
        assertNotNull(missions);
        assertEquals(2, missions.size());
        
        // Verify one is completed, one is in progress
        long completedCount = missions.stream()
            .filter(m -> m.getStatus() == MissionStatus.COMPLETED)
            .count();
        long inProgressCount = missions.stream()
            .filter(m -> m.getStatus() == MissionStatus.IN_PROGRESS)
            .count();
        
        assertEquals(1, completedCount);
        assertEquals(1, inProgressCount);
    }

    @Test
    @DisplayName("Integration Test 5.10: Reassign Mission - Admin Reassigns")
    void testReassignMission_AdminReassigns_Integration() {
        // Create mission assigned to assistant1
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        // Reassign to assistant2
        boolean result = missionService.reassignMission(missionId, assistant2UserId, adminUserId);

        // Verify
        assertTrue(result);
        
        Mission mission = missionDAO.findById(missionId);
        assertEquals(assistant2UserId, mission.getAssignedTo());
        assertEquals(MissionStatus.IN_PROGRESS, mission.getStatus());

        // Verify event
        assertEquals(1, eventListener.missionReassignedEvents.size());
        MissionReassignedEvent event = eventListener.missionReassignedEvents.get(0);
        assertEquals(missionId, event.getMissionId());
        assertEquals(assistant1UserId, event.getOldAssignedTo());
        assertEquals(assistant2UserId, event.getNewAssignedTo());
        assertEquals(adminUserId, event.getReassignedBy());
    }

    @Test
    @DisplayName("Integration Test: Complete Workflow - Assign, Draft, Complete")
    void testCompleteWorkflow_Integration() {
        // Assign mission
        Integer missionId = missionService.assignMission(
            lessonId, 
            MissionType.ATTENDANCE_HOMEWORK, 
            assistant1UserId, 
            adminUserId
        );

        // Save multiple drafts
        missionService.saveMissionDraft(missionId, "{\"progress\":25}");
        missionService.saveMissionDraft(missionId, "{\"progress\":50}");
        missionService.saveMissionDraft(missionId, "{\"progress\":100}");

        // Verify draft updated (not duplicated)
        List<MissionDraft> drafts = missionDraftDAO.findAll();
        assertEquals(1, drafts.size());
        assertTrue(drafts.get(0).getDraftData().contains("100"));

        // Complete mission
        missionService.completeMission(missionId, assistant1UserId);

        // Verify final state
        Mission mission = missionDAO.findById(missionId);
        assertEquals(MissionStatus.COMPLETED, mission.getStatus());
        assertNull(missionDraftDAO.findByMissionId(missionId));

        // Verify all events fired
        assertEquals(1, eventListener.missionAssignedEvents.size());
        assertEquals(3, eventListener.missionDraftSavedEvents.size());
        assertEquals(1, eventListener.missionCompletedEvents.size());
    }

    // Event listener for testing
    private static class TestEventListener {
        List<MissionAssignedEvent> missionAssignedEvents = new ArrayList<>();
        List<MissionCompletedEvent> missionCompletedEvents = new ArrayList<>();
        List<MissionDraftSavedEvent> missionDraftSavedEvents = new ArrayList<>();
        List<MissionReassignedEvent> missionReassignedEvents = new ArrayList<>();

        @Subscribe
        public void onMissionAssigned(MissionAssignedEvent event) {
            missionAssignedEvents.add(event);
        }

        @Subscribe
        public void onMissionCompleted(MissionCompletedEvent event) {
            missionCompletedEvents.add(event);
        }

        @Subscribe
        public void onMissionDraftSaved(MissionDraftSavedEvent event) {
            missionDraftSavedEvents.add(event);
        }

        @Subscribe
        public void onMissionReassigned(MissionReassignedEvent event) {
            missionReassignedEvents.add(event);
        }
    }
}