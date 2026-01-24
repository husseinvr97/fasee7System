package com.studenttracker.service.impl;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.BehavioralIncidentDAO;
import com.studenttracker.dao.ConsecutivityTrackingDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.exception.ServiceException;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.ConsecutivityTracking;
import com.studenttracker.model.ConsecutivityTracking.TrackingType;
import com.studenttracker.service.ConsecutivityTrackingService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.ConsecutiveThresholdReachedEvent;
import com.studenttracker.service.event.ConsecutivityUpdatedEvent;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ConsecutivityTrackingService.
 * Handles tracking of consecutive absences and behavioral incidents.
 * 
 * This service is event-driven and primarily called by event subscribers.
 */
public class ConsecutivityTrackingServiceImpl implements ConsecutivityTrackingService {
    
    private final ConsecutivityTrackingDAO consecutivityDAO;
    private final AttendanceDAO attendanceDAO;
    private final BehavioralIncidentDAO behavioralIncidentDAO;
    private final EventBusService eventBus;
    
    // Threshold constants
    private static final int ABSENCE_WARNING_THRESHOLD = 2;
    private static final int ABSENCE_ARCHIVAL_THRESHOLD = 3;
    private static final int BEHAVIORAL_WARNING_THRESHOLD = 2;
    
    /**
     * Constructor with dependency injection.
     */
    public ConsecutivityTrackingServiceImpl(ConsecutivityTrackingDAO consecutivityDAO,
                                           AttendanceDAO attendanceDAO,
                                           BehavioralIncidentDAO behavioralIncidentDAO,
                                           EventBusService eventBus) {
        this.consecutivityDAO = consecutivityDAO;
        this.attendanceDAO = attendanceDAO;
        this.behavioralIncidentDAO = behavioralIncidentDAO;
        this.eventBus = eventBus;
    }
    
    
    // ========== Update Operations ==========
    
    @Override
    public void updateAbsenceTracking(Integer studentId, Integer lessonId, AttendanceStatus status) 
            throws ServiceException {
        try {
            // Step 1: Get current tracking record (or null if doesn't exist)
            ConsecutivityTracking tracking = consecutivityDAO.findByStudentAndType(
                studentId, TrackingType.ABSENCE
            );
            
            // Step 2: Initialize tracking if it doesn't exist
            if (tracking == null) {
                tracking = new ConsecutivityTracking(
                    studentId, 
                    TrackingType.ABSENCE, 
                    0, 
                    null, 
                    LocalDateTime.now()
                );
            }
            
            // Step 3: Update based on attendance status
            if (status == AttendanceStatus.ABSENT) {
                // Increment consecutive count
                tracking.increment();
                tracking.setLastLessonId(lessonId);
            } else if (status == AttendanceStatus.PRESENT) {
                // Reset consecutive count
                tracking.reset();
                tracking.setLastLessonId(lessonId);
            }
            
            // Step 4: Persist changes (upsert handles both insert and update)
            consecutivityDAO.upsert(tracking);
            
            // Step 5: Check if threshold reached and publish event if needed
            int count = tracking.getConsecutiveCount();
            if (status == AttendanceStatus.ABSENT) {
                if (count == ABSENCE_ARCHIVAL_THRESHOLD) {
                    publishThresholdEvent(studentId, TrackingType.ABSENCE, count, 
                                        ABSENCE_ARCHIVAL_THRESHOLD, "ARCHIVAL");
                } else if (count == ABSENCE_WARNING_THRESHOLD) {
                    publishThresholdEvent(studentId, TrackingType.ABSENCE, count, 
                                        ABSENCE_WARNING_THRESHOLD, "WARNING");
                }
            }
            
            // Step 6: Always publish update event
            publishUpdateEvent(studentId, TrackingType.ABSENCE, count, lessonId);
            
        } catch (DAOException e) {
            throw new ServiceException("Failed to update absence tracking for student " + studentId, e);
        }
    }
    
    @Override
    public void updateBehavioralTracking(Integer studentId, Integer lessonId, IncidentType type) 
            throws ServiceException {
        try {
            // Step 1: Get incidents ordered by date (most recent first)
            List<BehavioralIncident> incidents = behavioralIncidentDAO.findByStudentId(studentId);
            
            // Step 2: Get current tracking record
            ConsecutivityTracking tracking = consecutivityDAO.findByStudentAndType(
                studentId, TrackingType.BEHAVIORAL_INCIDENT
            );
            
            // Step 3: Initialize tracking if it doesn't exist
            if (tracking == null) {
                tracking = new ConsecutivityTracking(
                    studentId, 
                    TrackingType.BEHAVIORAL_INCIDENT, 
                    0, 
                    null, 
                    LocalDateTime.now()
                );
            }
            
            // Step 4: Determine if same type as last incident
            boolean isSameTypeAsLast = false;
            if (incidents.size() > 1) {
                // Current incident is incidents[0], check against incidents[1]
                IncidentType lastType = incidents.get(1).getIncidentType();
                isSameTypeAsLast = (type == lastType);
            }
            
            // Step 5: Update tracking based on incident type pattern
            if (isSameTypeAsLast) {
                // Same type: increment
                tracking.increment();
            } else {
                // Different type or first incident: reset to 1
                tracking.reset();
                tracking.increment();
            }
            tracking.setLastLessonId(lessonId);
            
            // Step 6: Persist changes
            consecutivityDAO.upsert(tracking);
            
            // Step 7: Check if threshold reached
            int count = tracking.getConsecutiveCount();
            if (count >= BEHAVIORAL_WARNING_THRESHOLD) {
                publishThresholdEvent(studentId, TrackingType.BEHAVIORAL_INCIDENT, count, 
                                    BEHAVIORAL_WARNING_THRESHOLD, "WARNING");
            }
            
            // Step 8: Always publish update event
            publishUpdateEvent(studentId, TrackingType.BEHAVIORAL_INCIDENT, count, lessonId);
            
        } catch (DAOException e) {
            throw new ServiceException("Failed to update behavioral tracking for student " + studentId, e);
        }
    }
    
    
    // ========== Retrieval Operations ==========
    
    @Override
    public ConsecutivityTracking getAbsenceTracking(Integer studentId) throws ServiceException {
        try {
            return consecutivityDAO.findByStudentAndType(studentId, TrackingType.ABSENCE);
        } catch (DAOException e) {
            throw new ServiceException("Failed to get absence tracking for student " + studentId, e);
        }
    }
    
    @Override
    public ConsecutivityTracking getBehavioralTracking(Integer studentId) throws ServiceException {
        try {
            return consecutivityDAO.findByStudentAndType(studentId, TrackingType.BEHAVIORAL_INCIDENT);
        } catch (DAOException e) {
            throw new ServiceException("Failed to get behavioral tracking for student " + studentId, e);
        }
    }
    
    @Override
    public int getConsecutiveAbsenceCount(Integer studentId) throws ServiceException {
        ConsecutivityTracking tracking = getAbsenceTracking(studentId);
        return (tracking != null) ? tracking.getConsecutiveCount() : 0;
    }
    
    
    // ========== Reset Operations ==========
    
    @Override
    public void resetAllTracking(Integer studentId) throws ServiceException {
        try {
            consecutivityDAO.resetByStudentId(studentId);
        } catch (DAOException e) {
            throw new ServiceException("Failed to reset tracking for student " + studentId, e);
        }
    }
    
    @Override
    public void resetConsecutivity(Integer studentId) {
        try {
            resetAllTracking(studentId);
        } catch (ServiceException e) {
            // Log but don't throw - backward compatibility
            System.err.println("Warning: Failed to reset consecutivity for student " + 
                             studentId + ": " + e.getMessage());
        }
    }
    
    
    // ========== Threshold Checks ==========
    
    @Override
    public boolean hasReachedAbsenceWarningThreshold(Integer studentId) throws ServiceException {
        int count = getConsecutiveAbsenceCount(studentId);
        return count >= ABSENCE_WARNING_THRESHOLD;
    }
    
    @Override
    public boolean hasReachedAbsenceArchivalThreshold(Integer studentId) throws ServiceException {
        int count = getConsecutiveAbsenceCount(studentId);
        return count >= ABSENCE_ARCHIVAL_THRESHOLD;
    }
    
    
    // ========== Helper Methods ==========
    
    /**
     * Publishes a ConsecutivityUpdatedEvent.
     */
    private void publishUpdateEvent(Integer studentId, TrackingType type, 
                                   int count, Integer lessonId) {
        ConsecutivityUpdatedEvent event = new ConsecutivityUpdatedEvent(
            studentId, type, count, lessonId, LocalDateTime.now()
        );
        eventBus.publish(event);
    }
    
    /**
     * Publishes a ConsecutiveThresholdReachedEvent.
     */
    private void publishThresholdEvent(Integer studentId, TrackingType type, 
                                      int count, int threshold, String thresholdType) {
        ConsecutiveThresholdReachedEvent event = new ConsecutiveThresholdReachedEvent(
            studentId, type, count, threshold, thresholdType, LocalDateTime.now()
        );
        eventBus.publish(event);
    }
}