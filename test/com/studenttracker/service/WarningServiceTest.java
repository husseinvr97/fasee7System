package com.studenttracker.service;

import com.studenttracker.dao.WarningDAO;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;
import com.studenttracker.service.event.WarningGeneratedEvent;
import com.studenttracker.service.event.WarningResolvedEvent;
import com.studenttracker.service.impl.WarningServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class WarningServiceTest {
    
    private WarningDAO warningDAO;
    private ConsecutivityTrackingService consecutivityService;
    private BehavioralIncidentService behavioralService;
    private EventBusService eventBusService;
    private WarningService warningService;
    
    @BeforeEach
    void setUp() {
        warningDAO = mock(WarningDAO.class);
        consecutivityService = mock(ConsecutivityTrackingService.class);
        behavioralService = mock(BehavioralIncidentService.class);
        eventBusService = mock(EventBusService.class);
        
        warningService = new WarningServiceImpl(
            warningDAO,
            consecutivityService,
            behavioralService,
            eventBusService
        );
    }
    
    
    // ========== Test 10.1: Generate Absence Warning - 2 Consecutive ==========
    
    @Test
    void testGenerateAbsenceWarning_TwoConsecutive() {
        // Arrange
        Integer studentId = 1;
        int consecutiveCount = 2;
        Integer expectedWarningId = 100;
        
        when(warningDAO.insert(any(Warning.class))).thenReturn(expectedWarningId);
        
        // Act
        Integer warningId = warningService.generateAbsenceWarning(studentId, consecutiveCount);
        
        // Assert
        assertEquals(expectedWarningId, warningId);
        
        // Verify Warning object creation
        ArgumentCaptor<Warning> warningCaptor = ArgumentCaptor.forClass(Warning.class);
        verify(warningDAO).insert(warningCaptor.capture());
        
        Warning capturedWarning = warningCaptor.getValue();
        assertEquals(studentId, capturedWarning.getStudentId());
        assertEquals(WarningType.CONSECUTIVE_ABSENCE, capturedWarning.getWarningType());
        assertEquals("Student has 2 consecutive absences", capturedWarning.getWarningReason());
        assertTrue(capturedWarning.isActive());
        assertNotNull(capturedWarning.getCreatedAt());
        assertNull(capturedWarning.getResolvedAt());
        
        // Verify event published
        ArgumentCaptor<WarningGeneratedEvent> eventCaptor = ArgumentCaptor.forClass(WarningGeneratedEvent.class);
        verify(eventBusService).publish(eventCaptor.capture());
        
        WarningGeneratedEvent event = eventCaptor.getValue();
        assertEquals(expectedWarningId, event.getWarningId());
        assertEquals(studentId, event.getStudentId());
        assertEquals(WarningType.CONSECUTIVE_ABSENCE, event.getWarningType());
    }
    
    
    // ========== Test 10.2: Generate Absence Warning - 3 Consecutive (Archival) ==========
    
    @Test
    void testGenerateAbsenceWarning_ThreeConsecutive_Archived() {
        // Arrange
        Integer studentId = 1;
        int consecutiveCount = 3;
        Integer expectedWarningId = 101;
        
        when(warningDAO.insert(any(Warning.class))).thenReturn(expectedWarningId);
        
        // Act
        Integer warningId = warningService.generateAbsenceWarning(studentId, consecutiveCount);
        
        // Assert
        assertEquals(expectedWarningId, warningId);
        
        // Verify Warning type is ARCHIVED
        ArgumentCaptor<Warning> warningCaptor = ArgumentCaptor.forClass(Warning.class);
        verify(warningDAO).insert(warningCaptor.capture());
        
        Warning capturedWarning = warningCaptor.getValue();
        assertEquals(WarningType.ARCHIVED, capturedWarning.getWarningType());
        assertEquals("Student has 3 consecutive absences", capturedWarning.getWarningReason());
        
        // Verify event
        ArgumentCaptor<WarningGeneratedEvent> eventCaptor = ArgumentCaptor.forClass(WarningGeneratedEvent.class);
        verify(eventBusService).publish(eventCaptor.capture());
        assertEquals(WarningType.ARCHIVED, eventCaptor.getValue().getWarningType());
    }
    
    
    // ========== Test 10.3: Generate Behavioral Warning - 2 Same-Type Consecutive ==========
    
    @Test
    void testGenerateBehavioralWarning_TwoSameTypeConsecutive() {
        // Arrange
        Integer studentId = 1;
        String reason = "2 consecutive LATE incidents (Lessons 50, 49)";
        Integer expectedWarningId = 102;
        
        when(warningDAO.insert(any(Warning.class))).thenReturn(expectedWarningId);
        
        // Act
        Integer warningId = warningService.generateBehavioralWarning(studentId, reason);
        
        // Assert
        assertEquals(expectedWarningId, warningId);
        
        // Verify Warning object
        ArgumentCaptor<Warning> warningCaptor = ArgumentCaptor.forClass(Warning.class);
        verify(warningDAO).insert(warningCaptor.capture());
        
        Warning capturedWarning = warningCaptor.getValue();
        assertEquals(studentId, capturedWarning.getStudentId());
        assertEquals(WarningType.BEHAVIORAL, capturedWarning.getWarningType());
        assertEquals(reason, capturedWarning.getWarningReason());
        assertTrue(capturedWarning.isActive());
        
        // Verify event
        verify(eventBusService).publish(any(WarningGeneratedEvent.class));
    }
    
    
    // ========== Test 10.4: Generate Behavioral Warning - 3 Any-Type in Month ==========
    
    @Test
    void testGenerateBehavioralWarning_ThreeInMonth() {
        // Arrange
        Integer studentId = 1;
        String reason = "3 behavioral incidents in Month 2 (LATE, DISRESPECTFUL, LEFT_EARLY)";
        Integer expectedWarningId = 103;
        
        when(warningDAO.insert(any(Warning.class))).thenReturn(expectedWarningId);
        
        // Act
        Integer warningId = warningService.generateBehavioralWarning(studentId, reason);
        
        // Assert
        assertEquals(expectedWarningId, warningId);
        
        // Verify Warning
        ArgumentCaptor<Warning> warningCaptor = ArgumentCaptor.forClass(Warning.class);
        verify(warningDAO).insert(warningCaptor.capture());
        
        Warning capturedWarning = warningCaptor.getValue();
        assertEquals(WarningType.BEHAVIORAL, capturedWarning.getWarningType());
        assertEquals(reason, capturedWarning.getWarningReason());
        
        // Verify event
        verify(eventBusService).publish(any(WarningGeneratedEvent.class));
    }
    
    
    // ========== Test 10.5: Resolve Warning ==========
    
    @Test
    void testResolveWarning() {
        // Arrange
        Integer warningId = 100;
        String resolvedReason = "Student attended lesson, consecutive absences broken";
        
        Warning warning = new Warning();
        warning.setWarningId(warningId);
        warning.setStudentId(1);
        warning.setWarningType(WarningType.CONSECUTIVE_ABSENCE);
        warning.setActive(true);
        warning.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        when(warningDAO.findById(warningId)).thenReturn(warning);
        when(warningDAO.update(any(Warning.class))).thenReturn(true);
        
        // Act
        boolean result = warningService.resolveWarning(warningId, resolvedReason);
        
        // Assert
        assertTrue(result);
        
        // Verify warning was resolved
        ArgumentCaptor<Warning> warningCaptor = ArgumentCaptor.forClass(Warning.class);
        verify(warningDAO).update(warningCaptor.capture());
        
        Warning updatedWarning = warningCaptor.getValue();
        assertFalse(updatedWarning.isActive());
        assertNotNull(updatedWarning.getResolvedAt());
        
        // Verify event published
        ArgumentCaptor<WarningResolvedEvent> eventCaptor = ArgumentCaptor.forClass(WarningResolvedEvent.class);
        verify(eventBusService).publish(eventCaptor.capture());
        
        WarningResolvedEvent event = eventCaptor.getValue();
        assertEquals(warningId, event.getWarningId());
        assertEquals(1, event.getStudentId());
        assertEquals(resolvedReason, event.getResolvedReason());
    }
    
    
    // ========== Test 10.6: Resolve Warnings by Student and Type ==========
    
    @Test
    void testResolveWarningsByStudent() {
        // Arrange
        Integer studentId = 1;
        WarningType type = WarningType.CONSECUTIVE_ABSENCE;
        
        Warning warning1 = new Warning();
        warning1.setWarningId(100);
        warning1.setStudentId(studentId);
        warning1.setWarningType(WarningType.CONSECUTIVE_ABSENCE);
        warning1.setActive(true);
        
        Warning warning2 = new Warning();
        warning2.setWarningId(101);
        warning2.setStudentId(studentId);
        warning2.setWarningType(WarningType.BEHAVIORAL);
        warning2.setActive(true);
        
        List<Warning> warnings = Arrays.asList(warning1, warning2);
        
        when(warningDAO.findByStudentAndActive(studentId, true)).thenReturn(warnings);
        when(warningDAO.update(any(Warning.class))).thenReturn(true);
        
        // Act
        boolean result = warningService.resolveWarningsByStudent(studentId, type);
        
        // Assert
        assertTrue(result);
        
        // Verify only CONSECUTIVE_ABSENCE warning was updated
        verify(warningDAO, times(1)).update(argThat(w -> 
            w.getWarningId().equals(100) && !w.isActive()
        ));
        
        // Verify event published for resolved warning
        verify(eventBusService, times(1)).publish(any(WarningResolvedEvent.class));
    }
    
    
    // ========== Test 10.7: Get Active Warnings ==========
    
    @Test
    void testGetActiveWarnings() {
        // Arrange
        Warning warning1 = createWarning(100, 1, WarningType.CONSECUTIVE_ABSENCE, true);
        Warning warning2 = createWarning(101, 2, WarningType.BEHAVIORAL, true);
        Warning warning3 = createWarning(102, 3, WarningType.ARCHIVED, true);
        
        List<Warning> activeWarnings = Arrays.asList(warning1, warning2, warning3);
        
        when(warningDAO.findByActive(true)).thenReturn(activeWarnings);
        
        // Act
        List<Warning> result = warningService.getActiveWarnings();
        
        // Assert
        assertEquals(3, result.size());
        verify(warningDAO).findByActive(true);
    }
    
    
    // ========== Test 10.8: Get Active Warnings by Student ==========
    
    @Test
    void testGetActiveWarningsByStudent() {
        // Arrange
        Integer studentId = 1;
        
        Warning warning1 = createWarning(100, studentId, WarningType.CONSECUTIVE_ABSENCE, true);
        List<Warning> warnings = Collections.singletonList(warning1);
        
        when(warningDAO.findByStudentAndActive(studentId, true)).thenReturn(warnings);
        
        // Act
        List<Warning> result = warningService.getActiveWarningsByStudent(studentId);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getStudentId());
        assertTrue(result.get(0).isActive());
        verify(warningDAO).findByStudentAndActive(studentId, true);
    }
    
    
    // ========== Test 10.9: Get Warning Count by Type ==========
    
    @Test
    void testGetWarningCountByType() {
        // Arrange
        WarningType type = WarningType.CONSECUTIVE_ABSENCE;
        int expectedCount = 5;
        
        when(warningDAO.countByType(type)).thenReturn(expectedCount);
        
        // Act
        int count = warningService.getWarningCountByType(type);
        
        // Assert
        assertEquals(expectedCount, count);
        verify(warningDAO).countByType(type);
    }
    
    
    // ========== Test 10.10: Get Active Warning Count ==========
    
    @Test
    void testGetActiveWarningCount() {
        // Arrange
        int expectedCount = 12;
        
        when(warningDAO.countActive()).thenReturn(expectedCount);
        
        // Act
        int count = warningService.getActiveWarningCount();
        
        // Assert
        assertEquals(expectedCount, count);
        verify(warningDAO).countActive();
    }
    
    
    // ========== Test 10.11: Check and Generate Warnings - After Attendance Update ==========
    
    @Test
    void testCheckAndGenerateWarnings() throws Exception {
        // Arrange
        Integer studentId = 1;
        
        // Setup consecutive absences
        when(consecutivityService.getConsecutiveAbsenceCount(studentId)).thenReturn(2);
        
        // Setup behavioral incidents for consecutive same-type check
        BehavioralIncident incident1 = new BehavioralIncident();
        incident1.setIncidentType(BehavioralIncident.IncidentType.LATE);
        incident1.setCreatedAt(LocalDateTime.now().minusDays(1));
        
        BehavioralIncident incident2 = new BehavioralIncident();
        incident2.setIncidentType(BehavioralIncident.IncidentType.LATE);
        incident2.setCreatedAt(LocalDateTime.now());
        
        List<BehavioralIncident> consecutiveIncidents = Arrays.asList(incident1, incident2);
        when(behavioralService.getConsecutiveSameTypeIncidents(studentId)).thenReturn(consecutiveIncidents);
        
        // Setup behavioral incidents for 3-in-month check
        BehavioralIncident incident3 = new BehavioralIncident();
        incident3.setIncidentType(BehavioralIncident.IncidentType.DISRESPECTFUL);
        incident3.setCreatedAt(LocalDateTime.now().minusDays(5));
        
        List<BehavioralIncident> allIncidents = Arrays.asList(incident1, incident2, incident3);
        when(behavioralService.getIncidentsByStudent(studentId)).thenReturn(allIncidents);
        
        when(warningDAO.insert(any(Warning.class))).thenReturn(100, 101, 102);
        
        // Act
        warningService.checkAndGenerateWarnings(studentId);
        
        // Assert
        // Verify absence warning generated (2 consecutive)
        verify(warningDAO, atLeast(1)).insert(argThat(w -> 
            w.getWarningType() == WarningType.CONSECUTIVE_ABSENCE
        ));
        
        // Verify behavioral warning generated (2 consecutive same-type)
        verify(warningDAO, atLeast(1)).insert(argThat(w -> 
            w.getWarningType() == WarningType.BEHAVIORAL
        ));
        
        // Verify events published
        verify(eventBusService, atLeast(2)).publish(any(WarningGeneratedEvent.class));
    }
    
    
    // ========== Helper Methods ==========
    
    private Warning createWarning(Integer warningId, Integer studentId, WarningType type, boolean isActive) {
        Warning warning = new Warning();
        warning.setWarningId(warningId);
        warning.setStudentId(studentId);
        warning.setWarningType(type);
        warning.setWarningReason("Test reason");
        warning.setCreatedAt(LocalDateTime.now());
        warning.setActive(isActive);
        if (!isActive) {
            warning.setResolvedAt(LocalDateTime.now());
        }
        return warning;
    }
}