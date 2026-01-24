package com.studenttracker.service;

import com.studenttracker.exception.ServiceException;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.ConsecutivityTracking;

/**
 * Service interface for tracking consecutive student absences and behavioral incidents.
 * 
 * This service is primarily called by event subscribers to update tracking automatically
 * when attendance is marked or behavioral incidents are recorded.
 */
public interface ConsecutivityTrackingService {
    
    // ========== Update Operations (Called by Event Subscribers) ==========
    
    /**
     * Updates absence tracking when attendance is marked.
     * - If ABSENT: increments consecutive count
     * - If PRESENT: resets consecutive count to 0
     * 
     * Publishes events:
     * - ConsecutivityUpdatedEvent (always)
     * - ConsecutiveThresholdReachedEvent (if threshold reached: 2 or 3)
     * 
     * @param studentId The student ID
     * @param lessonId The lesson ID
     * @param status The attendance status (PRESENT/ABSENT)
     * @throws ServiceException if update fails
     */
    void updateAbsenceTracking(Integer studentId, Integer lessonId, AttendanceStatus status) 
            throws ServiceException;
    
    /**
     * Updates behavioral tracking when an incident is recorded.
     * - If same type as last incident: increments consecutive count
     * - If different type: resets to 1
     * 
     * Publishes events:
     * - ConsecutivityUpdatedEvent (always)
     * - ConsecutiveThresholdReachedEvent (if threshold reached: 2)
     * 
     * @param studentId The student ID
     * @param lessonId The lesson ID
     * @param type The incident type
     * @throws ServiceException if update fails
     */
    void updateBehavioralTracking(Integer studentId, Integer lessonId, IncidentType type) 
            throws ServiceException;
    
    
    // ========== Retrieval Operations ==========
    
    /**
     * Gets absence tracking record for a student.
     * 
     * @param studentId The student ID
     * @return The absence tracking record, or null if not found
     * @throws ServiceException if query fails
     */
    ConsecutivityTracking getAbsenceTracking(Integer studentId) throws ServiceException;
    
    /**
     * Gets behavioral tracking record for a student.
     * 
     * @param studentId The student ID
     * @return The behavioral tracking record, or null if not found
     * @throws ServiceException if query fails
     */
    ConsecutivityTracking getBehavioralTracking(Integer studentId) throws ServiceException;
    
    /**
     * Gets the current consecutive absence count for a student.
     * 
     * @param studentId The student ID
     * @return The consecutive absence count (0 if no tracking exists)
     * @throws ServiceException if query fails
     */
    int getConsecutiveAbsenceCount(Integer studentId) throws ServiceException;
    
    
    // ========== Reset Operations ==========
    
    /**
     * Resets all tracking (absence + behavioral) for a student.
     * Called when a student is restored from archived status.
     * 
     * @param studentId The student ID
     * @throws ServiceException if reset fails
     */
    void resetAllTracking(Integer studentId) throws ServiceException;
    
    /**
     * Legacy method for backward compatibility.
     * Delegates to resetAllTracking().
     * 
     * @param studentId The student ID
     */
    void resetConsecutivity(Integer studentId);
    
    
    // ========== Threshold Checks ==========
    
    /** 
     * Checks if student has reached warning threshold (2 consecutive absences).
     * 
     * @param studentId The student ID
     * @return true if consecutive absences >= 2
     * @throws ServiceException if query fails
     */
    boolean hasReachedAbsenceWarningThreshold(Integer studentId) throws ServiceException;
    
    /**
     * Checks if student has reached archival threshold (3 consecutive absences).
     * 
     * @param studentId The student ID
     * @return true if consecutive absences >= 3
     * @throws ServiceException if query fails
     */
    boolean hasReachedAbsenceArchivalThreshold(Integer studentId) throws ServiceException;
}