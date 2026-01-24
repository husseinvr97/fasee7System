package com.studenttracker.service;

import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import java.util.List;
import java.util.Map;

/**
 * Service interface for managing behavioral incidents.
 * Handles incident tracking, validation, and analysis.
 */
public interface BehavioralIncidentService {
    
    /**
     * Add a new behavioral incident.
     * Requires approval from admin if added by assistant.
     * 
     * @param studentId The student ID
     * @param lessonId The lesson ID
     * @param type The incident type
     * @param notes Optional notes about the incident
     * @param createdBy User ID who is creating the incident
     * @return The incident ID (or UpdateRequest ID if assistant)
     */
    Integer addIncident(Integer studentId, Integer lessonId, IncidentType type, 
                       String notes, Integer createdBy);
    
    /**
     * Update an existing incident (Admin only).
     * 
     * @param incidentId The incident ID
     * @param type New incident type
     * @param notes New notes
     * @param updatedBy User ID performing the update (must be admin)
     * @return true if successful
     */
    boolean updateIncident(Integer incidentId, IncidentType type, String notes, 
                          Integer updatedBy);
    
    /**
     * Delete an incident (Admin only).
     * 
     * @param incidentId The incident ID
     * @param deletedBy User ID performing the deletion (must be admin)
     * @return true if successful
     */
    boolean deleteIncident(Integer incidentId, Integer deletedBy);
    
    /**
     * Retrieve an incident by ID.
     * 
     * @param incidentId The incident ID
     * @return The incident or null if not found
     */
    BehavioralIncident getIncidentById(Integer incidentId);
    
    /**
     * Get all incidents for a student, ordered by date descending.
     * 
     * @param studentId The student ID
     * @return List of incidents
     */
    List<BehavioralIncident> getIncidentsByStudent(Integer studentId);
    
    /**
     * Get all incidents for a lesson.
     * 
     * @param lessonId The lesson ID
     * @return List of incidents
     */
    List<BehavioralIncident> getIncidentsByLesson(Integer lessonId);
    
    /**
     * Get all incidents of a specific type.
     * 
     * @param type The incident type
     * @return List of incidents
     */
    List<BehavioralIncident> getIncidentsByType(IncidentType type);
    
    /**
     * Find consecutive incidents of the same type for a student.
     * Returns the longest consecutive sequence.
     * 
     * @param studentId The student ID
     * @return List of consecutive same-type incidents
     */
    List<BehavioralIncident> getConsecutiveSameTypeIncidents(Integer studentId);
    
    /**
     * Get all incidents for a student in a specific month group.
     * 
     * @param studentId The student ID
     * @param monthGroup The month group (e.g., "Month 1")
     * @return List of incidents in that month
     */
    List<BehavioralIncident> getIncidentsInMonth(Integer studentId, String monthGroup);
    
    /**
     * Count total incidents for a student.
     * 
     * @param studentId The student ID
     * @return Total count
     */
    int countIncidentsByStudent(Integer studentId);
    
    /**
     * Get breakdown of incidents by type for a student.
     * 
     * @param studentId The student ID
     * @return Map of incident type to count
     */
    Map<IncidentType, Integer> getIncidentTypeBreakdown(Integer studentId);
}