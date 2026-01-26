package com.studenttracker.service;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.BehavioralIncidentDAO;
import com.studenttracker.dao.LessonDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.dao.UpdateRequestDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.StudentNotFoundException;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.UserNotFoundException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.Lesson;
import com.studenttracker.model.Student;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.event.BehavioralIncidentAddedEvent;
import com.studenttracker.service.impl.BehavioralIncidentServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BehavioralIncidentService Tests")
public class BehavioralIncidentServiceTest {

    private BehavioralIncidentService incidentService;
    private BehavioralIncidentDAO incidentDAO;
    private StudentDAO studentDAO;
    private AttendanceDAO attendanceDAO;
    private UserDAO userDAO;
    private UpdateRequestDAO updateRequestDAO;
    private LessonDAO lessonDAO;
    private EventBusService eventBusService;
    private TestEventListener eventListener;

    @BeforeEach
    void setUp() {
        // Create mocks
        incidentDAO = mock(BehavioralIncidentDAO.class);
        studentDAO = mock(StudentDAO.class);
        attendanceDAO = mock(AttendanceDAO.class);
        userDAO = mock(UserDAO.class);
        updateRequestDAO = mock(UpdateRequestDAO.class);
        lessonDAO = mock(LessonDAO.class);

        // Create service instance
        incidentService = new BehavioralIncidentServiceImpl(
            incidentDAO,
            studentDAO,
            attendanceDAO,
            userDAO,
            updateRequestDAO,
            lessonDAO
        );

        // Get singleton EventBusService and register listener
        eventBusService = EventBusService.getInstance();
        eventListener = new TestEventListener();
        eventBusService.register(eventListener);
    }

    @AfterEach
    void tearDown() {
        // Unregister event listener
        if (eventListener != null) {
            eventBusService.unregister(eventListener);
        }
    }

    // ========== Test 8.1: Add Incident - Valid ==========
    @Test
    @DisplayName("Test 8.1: Add Incident - Valid")
    void testAddIncident_Valid() {
        // Arrange
        Integer studentId = 1;
        Integer lessonId = 50;
        IncidentType type = IncidentType.LATE;
        String notes = "Arrived 15 minutes late";
        Integer createdBy = 2;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(2);
        when(userDAO.findById(2)).thenReturn(adminUser);

        // Mock active student
        Student student = new Student("John Doe", "1234567890", "0987654321");
        student.setStudentId(1);
        student.setStatus(StudentStatus.ACTIVE);
        when(studentDAO.findById(1)).thenReturn(student);

        // Mock attendance - student present
        Attendance attendance = new Attendance(50, 1, AttendanceStatus.PRESENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 1)).thenReturn(attendance);

        // Mock incident insertion
        when(incidentDAO.insert(any(BehavioralIncident.class))).thenReturn(100);

        // Act
        Integer incidentId = incidentService.addIncident(studentId, lessonId, type, notes, createdBy);

        // Assert
        assertNotNull(incidentId);
        assertEquals(100, incidentId);

        // Verify user validation
        verify(userDAO, times(1)).findById(2);

        // Verify student validation
        verify(studentDAO, times(1)).findById(1);

        // Verify attendance check
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 1);

        // Verify incident insertion
        ArgumentCaptor<BehavioralIncident> incidentCaptor = ArgumentCaptor.forClass(BehavioralIncident.class);
        verify(incidentDAO, times(1)).insert(incidentCaptor.capture());
        BehavioralIncident capturedIncident = incidentCaptor.getValue();
        assertEquals(studentId, capturedIncident.getStudentId());
        assertEquals(lessonId, capturedIncident.getLessonId());
        assertEquals(type, capturedIncident.getIncidentType());
        assertEquals(notes, capturedIncident.getNotes());
        assertEquals(createdBy, capturedIncident.getCreatedBy());

        // Verify BehavioralIncidentAddedEvent published
        BehavioralIncidentAddedEvent event = eventListener.getEventOfType(BehavioralIncidentAddedEvent.class);
        assertNotNull(event, "BehavioralIncidentAddedEvent should be published");
        assertEquals(100, event.getIncidentId());
        assertEquals(1, event.getStudentId());
        assertEquals(50, event.getLessonId());
        assertEquals(IncidentType.LATE, event.getIncidentType());
        assertEquals(2, event.getCreatedBy());
    }

    // ========== Test 8.2: Add Incident - Student Absent ==========
    @Test
    @DisplayName("Test 8.2: Add Incident - Student Absent")
    void testAddIncident_StudentAbsent() {
        // Arrange
        Integer studentId = 3;
        Integer lessonId = 50;
        IncidentType type = IncidentType.LATE;
        String notes = "Late";
        Integer createdBy = 2;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(2);
        when(userDAO.findById(2)).thenReturn(adminUser);

        // Mock active student
        Student student = new Student("Jane Smith", "1234567890", "0987654321");
        student.setStudentId(3);
        student.setStatus(StudentStatus.ACTIVE);
        when(studentDAO.findById(3)).thenReturn(student);

        // Mock attendance - student absent
        Attendance attendance = new Attendance(50, 3, AttendanceStatus.ABSENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 3)).thenReturn(attendance);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            incidentService.addIncident(studentId, lessonId, type, notes, createdBy);
        });

        assertEquals("Cannot add incident for absent student", exception.getMessage());

        // Verify no incident insertion
        verify(incidentDAO, never()).insert(any(BehavioralIncident.class));
    }

    // ========== Test 8.3: Add Incident - Archived Student ==========
    @Test
    @DisplayName("Test 8.3: Add Incident - Archived Student")
    void testAddIncident_ArchivedStudent() {
        // Arrange
        Integer studentId = 5;
        Integer lessonId = 50;
        IncidentType type = IncidentType.LATE;
        Integer createdBy = 2;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(2);
        when(userDAO.findById(2)).thenReturn(adminUser);

        // Mock archived student
        Student student = new Student("Archived Student", "1234567890", "0987654321");
        student.setStudentId(5);
        student.setStatus(StudentStatus.ARCHIVED);
        when(studentDAO.findById(5)).thenReturn(student);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            incidentService.addIncident(studentId, lessonId, type, null, createdBy);
        });

        assertEquals("Cannot add incident for archived student", exception.getMessage());

        // Verify no attendance check
        verify(attendanceDAO, never()).findByLessonAndStudent(anyInt(), anyInt());

        // Verify no incident insertion
        verify(incidentDAO, never()).insert(any(BehavioralIncident.class));
    }

    // ========== Test 8.4: Get Consecutive Same-Type Incidents ==========
    @Test
    @DisplayName("Test 8.4: Get Consecutive Same-Type Incidents")
    void testGetConsecutiveSameTypeIncidents() {
        // Arrange
        Integer studentId = 1;

        // Create incidents with specific times to ensure order
        LocalDateTime now = LocalDateTime.now();
        List<BehavioralIncident> incidents = new ArrayList<>();
        
        // Most recent consecutive LATE incidents (longest sequence = 2)
        BehavioralIncident incident1 = createIncident(1, 50, IncidentType.LATE, now.minusDays(0));
        BehavioralIncident incident2 = createIncident(1, 49, IncidentType.LATE, now.minusDays(1));
        
        // Break in chain
        BehavioralIncident incident3 = createIncident(1, 48, IncidentType.DISRESPECTFUL, now.minusDays(2));
        
        // Older LATE incident (only 1, not consecutive)
        BehavioralIncident incident4 = createIncident(1, 47, IncidentType.LATE, now.minusDays(3));

        incidents.add(incident1);
        incidents.add(incident2);
        incidents.add(incident3);
        incidents.add(incident4);

        when(incidentDAO.findByStudentId(1)).thenReturn(incidents);

        // Act
        List<BehavioralIncident> result = incidentService.getConsecutiveSameTypeIncidents(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 consecutive LATE incidents");
        assertTrue(result.stream().allMatch(i -> i.getIncidentType() == IncidentType.LATE), 
            "All incidents should be LATE type");
        
        // Verify the incidents are the most recent consecutive ones (lessons 50 and 49)
        // Since they're sorted by createdAt ascending in the helper, the result should contain both
        assertTrue(result.stream().anyMatch(i -> i.getLessonId() == 50), 
            "Should include incident from lesson 50");
        assertTrue(result.stream().anyMatch(i -> i.getLessonId() == 49), 
            "Should include incident from lesson 49");
    }

    // ========== Test 8.5: Get Incidents in Month ==========
    @Test
    @DisplayName("Test 8.5: Get Incidents in Month")
    void testGetIncidentsInMonth() {
        // Arrange
        Integer studentId = 1;
        String monthGroup = "Month 2";

        // Create incidents
        List<BehavioralIncident> allIncidents = new ArrayList<>();
        allIncidents.add(createIncident(1, 50, IncidentType.LATE, LocalDateTime.now())); // Month 2
        allIncidents.add(createIncident(1, 51, IncidentType.LATE, LocalDateTime.now())); // Month 2
        allIncidents.add(createIncident(1, 52, IncidentType.LATE, LocalDateTime.now())); // Month 2
        allIncidents.add(createIncident(1, 40, IncidentType.LATE, LocalDateTime.now())); // Month 1
        allIncidents.add(createIncident(1, 41, IncidentType.LATE, LocalDateTime.now())); // Month 1

        when(incidentDAO.findByStudentId(1)).thenReturn(allIncidents);

        // Mock lessons
        when(lessonDAO.findById(50)).thenReturn(createLesson(50, "Month 2"));
        when(lessonDAO.findById(51)).thenReturn(createLesson(51, "Month 2"));
        when(lessonDAO.findById(52)).thenReturn(createLesson(52, "Month 2"));
        when(lessonDAO.findById(40)).thenReturn(createLesson(40, "Month 1"));
        when(lessonDAO.findById(41)).thenReturn(createLesson(41, "Month 1"));

        // Act
        List<BehavioralIncident> result = incidentService.getIncidentsInMonth(studentId, monthGroup);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size(), "Should return 3 incidents from Month 2");
        
        // Verify all returned incidents are from Month 2 lessons
        assertTrue(result.stream().allMatch(i -> 
            i.getLessonId() == 50 || i.getLessonId() == 51 || i.getLessonId() == 52
        ));
    }

    // ========== Test 8.6: Count Incidents by Student ==========
    @Test
    @DisplayName("Test 8.6: Count Incidents by Student")
    void testCountIncidentsByStudent() {
        // Arrange
        Integer studentId = 1;
        when(incidentDAO.countByStudentId(1)).thenReturn(7);

        // Act
        int count = incidentService.countIncidentsByStudent(studentId);

        // Assert
        assertEquals(7, count);
        verify(incidentDAO, times(1)).countByStudentId(1);
    }

    // ========== Test 8.7: Get Incident Type Breakdown ==========
    @Test
    @DisplayName("Test 8.7: Get Incident Type Breakdown")
    void testGetIncidentTypeBreakdown() {
        // Arrange
        Integer studentId = 1;

        List<BehavioralIncident> incidents = new ArrayList<>();
        incidents.add(createIncident(1, 50, IncidentType.LATE, LocalDateTime.now()));
        incidents.add(createIncident(1, 51, IncidentType.LATE, LocalDateTime.now()));
        incidents.add(createIncident(1, 52, IncidentType.LATE, LocalDateTime.now()));
        incidents.add(createIncident(1, 53, IncidentType.DISRESPECTFUL, LocalDateTime.now()));
        incidents.add(createIncident(1, 54, IncidentType.DISRESPECTFUL, LocalDateTime.now()));
        incidents.add(createIncident(1, 55, IncidentType.LEFT_EARLY, LocalDateTime.now()));

        when(incidentDAO.findByStudentId(1)).thenReturn(incidents);

        // Act
        Map<IncidentType, Integer> breakdown = incidentService.getIncidentTypeBreakdown(studentId);

        // Assert
        assertNotNull(breakdown);
        assertEquals(3, breakdown.get(IncidentType.LATE));
        assertEquals(2, breakdown.get(IncidentType.DISRESPECTFUL));
        assertEquals(1, breakdown.get(IncidentType.LEFT_EARLY));
        assertEquals(0, breakdown.get(IncidentType.OTHER));
    }

    // ========== Test 8.8: Update Incident - Admin Only ==========
    @Test
    @DisplayName("Test 8.8: Update Incident - Admin Only")
    void testUpdateIncident_AdminOnly() {
        // Arrange
        Integer incidentId = 100;
        IncidentType type = IncidentType.DISRESPECTFUL;
        String notes = "Updated notes";
        Integer updatedBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Mock existing incident
        BehavioralIncident incident = createIncident(1, 50, IncidentType.LATE, LocalDateTime.now());
        incident.setIncidentId(100);
        when(incidentDAO.findById(100)).thenReturn(incident);

        // Mock update
        when(incidentDAO.update(any(BehavioralIncident.class))).thenReturn(true);

        // Act
        boolean result = incidentService.updateIncident(incidentId, type, notes, updatedBy);

        // Assert
        assertTrue(result);

        // Verify admin validation
        verify(userDAO, times(1)).findById(1);

        // Verify update
        ArgumentCaptor<BehavioralIncident> incidentCaptor = ArgumentCaptor.forClass(BehavioralIncident.class);
        verify(incidentDAO, times(1)).update(incidentCaptor.capture());
        BehavioralIncident updatedIncident = incidentCaptor.getValue();
        assertEquals(type, updatedIncident.getIncidentType());
        assertEquals(notes, updatedIncident.getNotes());
    }

    // ========== Test 8.9: Update Incident - Assistant Tries ==========
    @Test
    @DisplayName("Test 8.9: Update Incident - Assistant Tries")
    void testUpdateIncident_AssistantTries() {
        // Arrange
        Integer incidentId = 100;
        IncidentType type = IncidentType.DISRESPECTFUL;
        String notes = "Updated notes";
        Integer updatedBy = 5;

        // Mock assistant user
        User assistantUser = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistantUser.setUserId(5);
        when(userDAO.findById(5)).thenReturn(assistantUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            incidentService.updateIncident(incidentId, type, notes, updatedBy);
        });

        assertEquals("Only admins can perform this action", exception.getMessage());

        // Verify no update
        verify(incidentDAO, never()).update(any(BehavioralIncident.class));
    }

    // ========== Test 8.10: Delete Incident - Admin Only ==========
    @Test
    @DisplayName("Test 8.10: Delete Incident - Admin Only")
    void testDeleteIncident_AdminOnly() {
        // Arrange
        Integer incidentId = 100;
        Integer deletedBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Mock existing incident
        BehavioralIncident incident = createIncident(1, 50, IncidentType.LATE, LocalDateTime.now());
        incident.setIncidentId(100);
        when(incidentDAO.findById(100)).thenReturn(incident);

        // Mock delete
        when(incidentDAO.delete(100)).thenReturn(true);

        // Act
        boolean result = incidentService.deleteIncident(incidentId, deletedBy);

        // Assert
        assertTrue(result);

        // Verify admin validation
        verify(userDAO, times(1)).findById(1);

        // Verify delete
        verify(incidentDAO, times(1)).delete(100);
    }

    // ========== Helper Methods ==========

    private BehavioralIncident createIncident(Integer studentId, Integer lessonId, 
                                             IncidentType type, LocalDateTime createdAt) {
        BehavioralIncident incident = new BehavioralIncident();
        incident.setStudentId(studentId);
        incident.setLessonId(lessonId);
        incident.setIncidentType(type);
        incident.setCreatedAt(createdAt);
        incident.setCreatedBy(2);
        return incident;
    }

    private Lesson createLesson(Integer lessonId, String monthGroup) {
        Lesson lesson = new Lesson();
        lesson.setLessonId(lessonId);
        lesson.setMonthGroup(monthGroup);
        lesson.setLessonDate(LocalDate.now());
        return lesson;
    }

    // ========== Event Listener for Testing ==========
    private static class TestEventListener {
        private final List<Object> capturedEvents = new ArrayList<>();

        @Subscribe
        public void onEvent(Object event) {
            capturedEvents.add(event);
        }

        public List<Object> getCapturedEvents() {
            return capturedEvents;
        }

        public void clear() {
            capturedEvents.clear();
        }

        public <T> T getEventOfType(Class<T> eventType) {
            return capturedEvents.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .findFirst()
                .orElse(null);
        }

        public <T> List<T> getEventsOfType(Class<T> eventType) {
            return capturedEvents.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .toList();
        }
    }
}