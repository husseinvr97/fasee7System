package com.studenttracker.service.impl;

import com.studenttracker.dao.BehavioralIncidentDAO;
import com.studenttracker.dao.ConsecutivityTrackingDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.exception.ServiceException;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.ConsecutivityTracking;
import com.studenttracker.model.ConsecutivityTracking.TrackingType;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.ConsecutiveThresholdReachedEvent;
import com.studenttracker.service.event.ConsecutivityUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConsecutivityTrackingServiceImpl
 */
public class ConsecutivityTrackingServiceImplTest {

    private ConsecutivityTrackingDAO consecutivityDAO;
    private BehavioralIncidentDAO behavioralIncidentDAO;
    private EventBusService eventBus;
    private ConsecutivityTrackingServiceImpl service;

    @BeforeEach
    void setUp() {
        consecutivityDAO = mock(ConsecutivityTrackingDAO.class);
        behavioralIncidentDAO = mock(BehavioralIncidentDAO.class);
        eventBus = mock(EventBusService.class);
        service = new ConsecutivityTrackingServiceImpl(consecutivityDAO, behavioralIncidentDAO, eventBus);
    }

    // ==================== Test 9.1 ====================
    @Test
    @DisplayName("Test 9.1: Update Absence Tracking - First Absence")
    void testUpdateAbsenceTracking_FirstAbsence() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        Integer lessonId = 50;
        AttendanceStatus status = AttendanceStatus.ABSENT;

        // No existing tracking record
        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(null);

        // When
        service.updateAbsenceTracking(studentId, lessonId, status);

        // Then
        ArgumentCaptor<ConsecutivityTracking> trackingCaptor = ArgumentCaptor.forClass(ConsecutivityTracking.class);
        verify(consecutivityDAO).upsert(trackingCaptor.capture());

        ConsecutivityTracking savedTracking = trackingCaptor.getValue();
        assertEquals(1, savedTracking.getConsecutiveCount());
        assertEquals(lessonId, savedTracking.getLastLessonId());
        assertEquals(studentId, savedTracking.getStudentId());
        assertEquals(TrackingType.ABSENCE, savedTracking.getTrackingType());

        // Verify ConsecutivityUpdatedEvent published
        ArgumentCaptor<ConsecutivityUpdatedEvent> updateEventCaptor = ArgumentCaptor.forClass(ConsecutivityUpdatedEvent.class);
        verify(eventBus, times(1)).publish(updateEventCaptor.capture());

        ConsecutivityUpdatedEvent updateEvent = updateEventCaptor.getValue();
        assertEquals(studentId, updateEvent.getStudentId());
        assertEquals(TrackingType.ABSENCE, updateEvent.getTrackingType());
        assertEquals(1, updateEvent.getConsecutiveCount());
        assertEquals(lessonId, updateEvent.getLastLessonId());

        // Verify NO ThresholdReachedEvent (count = 1, threshold = 2)
        verify(eventBus, never()).publish(any(ConsecutiveThresholdReachedEvent.class));
    }

    // ==================== Test 9.2 ====================
    @Test
    @DisplayName("Test 9.2: Update Absence Tracking - Second Consecutive Absence (Warning Threshold)")
    void testUpdateAbsenceTracking_SecondAbsence_WarningThreshold() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        Integer lessonId = 51;
        AttendanceStatus status = AttendanceStatus.ABSENT;

        // Existing tracking with count = 1
        ConsecutivityTracking existingTracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 1, 50, LocalDateTime.now()
        );
        existingTracking.setTrackingId(1);

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(existingTracking);

        // When
        service.updateAbsenceTracking(studentId, lessonId, status);

        // Then
        ArgumentCaptor<ConsecutivityTracking> trackingCaptor = ArgumentCaptor.forClass(ConsecutivityTracking.class);
        verify(consecutivityDAO).upsert(trackingCaptor.capture());

        ConsecutivityTracking savedTracking = trackingCaptor.getValue();
        assertEquals(2, savedTracking.getConsecutiveCount());
        assertEquals(lessonId, savedTracking.getLastLessonId());

        // Verify both events published
        ArgumentCaptor<Object> allEventsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventBus, times(2)).publish(allEventsCaptor.capture());

        List<Object> publishedEvents = allEventsCaptor.getAllValues();
        
        // Find threshold event
        ConsecutiveThresholdReachedEvent thresholdEvent = (ConsecutiveThresholdReachedEvent) publishedEvents.stream()
                .filter(e -> e instanceof ConsecutiveThresholdReachedEvent)
                .findFirst()
                .orElse(null);

        assertNotNull(thresholdEvent);
        assertEquals(studentId, thresholdEvent.getStudentId());
        assertEquals(TrackingType.ABSENCE, thresholdEvent.getTrackingType());
        assertEquals(2, thresholdEvent.getConsecutiveCount());
        assertEquals(2, thresholdEvent.getThreshold());
        assertEquals("WARNING", thresholdEvent.getThresholdType());
        assertTrue(thresholdEvent.isWarningThreshold());
        assertFalse(thresholdEvent.isArchivalThreshold());

        // Find update event
        ConsecutivityUpdatedEvent updateEvent = (ConsecutivityUpdatedEvent) publishedEvents.stream()
                .filter(e -> e instanceof ConsecutivityUpdatedEvent)
                .findFirst()
                .orElse(null);

        assertNotNull(updateEvent);
        assertEquals(2, updateEvent.getConsecutiveCount());
    }

    // ==================== Test 9.3 ====================
    @Test
    @DisplayName("Test 9.3: Update Absence Tracking - Third Consecutive Absence (Archival Threshold)")
    void testUpdateAbsenceTracking_ThirdAbsence_ArchivalThreshold() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        Integer lessonId = 52;
        AttendanceStatus status = AttendanceStatus.ABSENT;

        // Existing tracking with count = 2
        ConsecutivityTracking existingTracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 2, 51, LocalDateTime.now()
        );
        existingTracking.setTrackingId(1);

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(existingTracking);

        // When
        service.updateAbsenceTracking(studentId, lessonId, status);

        // Then
        ArgumentCaptor<ConsecutivityTracking> trackingCaptor = ArgumentCaptor.forClass(ConsecutivityTracking.class);
        verify(consecutivityDAO).upsert(trackingCaptor.capture());

        ConsecutivityTracking savedTracking = trackingCaptor.getValue();
        assertEquals(3, savedTracking.getConsecutiveCount());
        assertEquals(lessonId, savedTracking.getLastLessonId());

        // Verify both events published
        ArgumentCaptor<Object> allEventsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventBus, times(2)).publish(allEventsCaptor.capture());

        List<Object> publishedEvents = allEventsCaptor.getAllValues();
        
        // Find threshold event
        ConsecutiveThresholdReachedEvent thresholdEvent = (ConsecutiveThresholdReachedEvent) publishedEvents.stream()
                .filter(e -> e instanceof ConsecutiveThresholdReachedEvent)
                .findFirst()
                .orElse(null);

        assertNotNull(thresholdEvent);
        assertEquals(studentId, thresholdEvent.getStudentId());
        assertEquals(3, thresholdEvent.getConsecutiveCount());
        assertEquals(3, thresholdEvent.getThreshold());
        assertEquals("ARCHIVAL", thresholdEvent.getThresholdType());
        assertTrue(thresholdEvent.isArchivalThreshold());
        assertFalse(thresholdEvent.isWarningThreshold());
    }

    // ==================== Test 9.4 ====================
    @Test
    @DisplayName("Test 9.4: Update Absence Tracking - Attendance Resets Count")
    void testUpdateAbsenceTracking_AttendanceResetsCount() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        Integer lessonId = 53;
        AttendanceStatus status = AttendanceStatus.PRESENT;

        // Existing tracking with count = 2
        ConsecutivityTracking existingTracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 2, 52, LocalDateTime.now()
        );
        existingTracking.setTrackingId(1);

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(existingTracking);

        // When
        service.updateAbsenceTracking(studentId, lessonId, status);

        // Then
        ArgumentCaptor<ConsecutivityTracking> trackingCaptor = ArgumentCaptor.forClass(ConsecutivityTracking.class);
        verify(consecutivityDAO).upsert(trackingCaptor.capture());

        ConsecutivityTracking savedTracking = trackingCaptor.getValue();
        assertEquals(0, savedTracking.getConsecutiveCount()); // Reset to 0
        assertEquals(lessonId, savedTracking.getLastLessonId());

        // Verify only ConsecutivityUpdatedEvent published
        ArgumentCaptor<ConsecutivityUpdatedEvent> updateEventCaptor = ArgumentCaptor.forClass(ConsecutivityUpdatedEvent.class);
        verify(eventBus, times(1)).publish(updateEventCaptor.capture());

        ConsecutivityUpdatedEvent updateEvent = updateEventCaptor.getValue();
        assertEquals(0, updateEvent.getConsecutiveCount());

        // Verify NO ThresholdReachedEvent
        verify(eventBus, never()).publish(any(ConsecutiveThresholdReachedEvent.class));
    }

    // ==================== Test 9.5 ====================
    @Test
    @DisplayName("Test 9.5: Update Behavioral Tracking - Same Type Consecutive")
    void testUpdateBehavioralTracking_SameTypeConsecutive() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        Integer lessonId = 50;
        IncidentType type = IncidentType.LATE;

        // Student's incident history
        List<BehavioralIncident> incidents = new ArrayList<>();
        incidents.add(new BehavioralIncident(studentId, lessonId, IncidentType.LATE, "Current", LocalDateTime.now(), 1)); // Current (index 0)
        incidents.add(new BehavioralIncident(studentId, 49, IncidentType.LATE, "Last", LocalDateTime.now().minusDays(1), 1)); // Last (index 1)
        incidents.add(new BehavioralIncident(studentId, 48, IncidentType.DISRESPECTFUL, "Before", LocalDateTime.now().minusDays(2), 1));
        incidents.add(new BehavioralIncident(studentId, 47, IncidentType.LATE, "Earlier", LocalDateTime.now().minusDays(3), 1));

        when(behavioralIncidentDAO.findByStudentId(studentId)).thenReturn(incidents);

        // Existing tracking with count = 1
        ConsecutivityTracking existingTracking = new ConsecutivityTracking(
                studentId, TrackingType.BEHAVIORAL_INCIDENT, 1, 49, LocalDateTime.now()
        );
        existingTracking.setTrackingId(1);

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.BEHAVIORAL_INCIDENT))
                .thenReturn(existingTracking);

        // When
        service.updateBehavioralTracking(studentId, lessonId, type);

        // Then
        ArgumentCaptor<ConsecutivityTracking> trackingCaptor = ArgumentCaptor.forClass(ConsecutivityTracking.class);
        verify(consecutivityDAO).upsert(trackingCaptor.capture());

        ConsecutivityTracking savedTracking = trackingCaptor.getValue();
        assertEquals(2, savedTracking.getConsecutiveCount()); // Incremented from 1 to 2
        assertEquals(lessonId, savedTracking.getLastLessonId());

        // Verify threshold event published (count = 2)
        ArgumentCaptor<Object> allEventsCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventBus, times(2)).publish(allEventsCaptor.capture());

        List<Object> publishedEvents = allEventsCaptor.getAllValues();
        
        ConsecutiveThresholdReachedEvent thresholdEvent = (ConsecutiveThresholdReachedEvent) publishedEvents.stream()
                .filter(e -> e instanceof ConsecutiveThresholdReachedEvent)
                .findFirst()
                .orElse(null);

        assertNotNull(thresholdEvent);
        assertEquals(2, thresholdEvent.getConsecutiveCount());
        assertEquals(2, thresholdEvent.getThreshold());
        assertEquals("WARNING", thresholdEvent.getThresholdType());
    }

    // ==================== Test 9.6 ====================
    @Test
    @DisplayName("Test 9.6: Update Behavioral Tracking - Different Type Resets")
    void testUpdateBehavioralTracking_DifferentTypeResets() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        Integer lessonId = 50;
        IncidentType type = IncidentType.DISRESPECTFUL;

        // Student's incident history - last was LATE, current is DISRESPECTFUL
        List<BehavioralIncident> incidents = new ArrayList<>();
        incidents.add(new BehavioralIncident(studentId, lessonId, IncidentType.DISRESPECTFUL, "Current", LocalDateTime.now(), 1)); // Current
        incidents.add(new BehavioralIncident(studentId, 49, IncidentType.LATE, "Last", LocalDateTime.now().minusDays(1), 1)); // Last (different type)

        when(behavioralIncidentDAO.findByStudentId(studentId)).thenReturn(incidents);

        // Existing tracking with count = 1 (from LATE)
        ConsecutivityTracking existingTracking = new ConsecutivityTracking(
                studentId, TrackingType.BEHAVIORAL_INCIDENT, 1, 49, LocalDateTime.now()
        );
        existingTracking.setTrackingId(1);

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.BEHAVIORAL_INCIDENT))
                .thenReturn(existingTracking);

        // When
        service.updateBehavioralTracking(studentId, lessonId, type);

        // Then
        ArgumentCaptor<ConsecutivityTracking> trackingCaptor = ArgumentCaptor.forClass(ConsecutivityTracking.class);
        verify(consecutivityDAO).upsert(trackingCaptor.capture());

        ConsecutivityTracking savedTracking = trackingCaptor.getValue();
        assertEquals(1, savedTracking.getConsecutiveCount()); // Reset to 1 (new type)
        assertEquals(lessonId, savedTracking.getLastLessonId());

        // Verify only ConsecutivityUpdatedEvent published
        ArgumentCaptor<ConsecutivityUpdatedEvent> updateEventCaptor = ArgumentCaptor.forClass(ConsecutivityUpdatedEvent.class);
        verify(eventBus, times(1)).publish(updateEventCaptor.capture());

        ConsecutivityUpdatedEvent updateEvent = updateEventCaptor.getValue();
        assertEquals(1, updateEvent.getConsecutiveCount());

        // Verify NO ThresholdReachedEvent (count = 1, threshold = 2)
        verify(eventBus, never()).publish(any(ConsecutiveThresholdReachedEvent.class));
    }

    // ==================== Test 9.7 ====================
    @Test
    @DisplayName("Test 9.7: Reset All Tracking - Student Restored")
    void testResetAllTracking_StudentRestored() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;

        // When
        service.resetAllTracking(studentId);

        // Then
        verify(consecutivityDAO).resetByStudentId(studentId);
    }

    // ==================== Test 9.8 ====================
    @Test
    @DisplayName("Test 9.8: Get Consecutive Absence Count")
    void testGetConsecutiveAbsenceCount() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        ConsecutivityTracking tracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 2, 50, LocalDateTime.now()
        );

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(tracking);

        // When
        int count = service.getConsecutiveAbsenceCount(studentId);

        // Then
        assertEquals(2, count);
        verify(consecutivityDAO).findByStudentAndType(studentId, TrackingType.ABSENCE);
    }

    // ==================== Test 9.9 ====================
    @Test
    @DisplayName("Test 9.9: Has Reached Absence Warning Threshold")
    void testHasReachedAbsenceWarningThreshold() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        ConsecutivityTracking tracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 2, 50, LocalDateTime.now()
        );

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(tracking);

        // When
        boolean hasReached = service.hasReachedAbsenceWarningThreshold(studentId);

        // Then
        assertTrue(hasReached); // count = 2, threshold = 2
    }

    // ==================== Test 9.10 ====================
    @Test
    @DisplayName("Test 9.10: Has Reached Absence Archival Threshold")
    void testHasReachedAbsenceArchivalThreshold() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        ConsecutivityTracking tracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 3, 50, LocalDateTime.now()
        );

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(tracking);

        // When
        boolean hasReached = service.hasReachedAbsenceArchivalThreshold(studentId);

        // Then
        assertTrue(hasReached); // count = 3, threshold = 3
    }

    // ==================== Test 9.11 ====================
    @Test
    @DisplayName("Test 9.11: Has NOT Reached Archival Threshold")
    void testHasNotReachedAbsenceArchivalThreshold() throws DAOException, ServiceException {
        // Given
        Integer studentId = 1;
        ConsecutivityTracking tracking = new ConsecutivityTracking(
                studentId, TrackingType.ABSENCE, 2, 50, LocalDateTime.now()
        );

        when(consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE))
                .thenReturn(tracking);

        // When
        boolean hasReached = service.hasReachedAbsenceArchivalThreshold(studentId);

        // Then
        assertFalse(hasReached); // count = 2, threshold = 3
    }
}