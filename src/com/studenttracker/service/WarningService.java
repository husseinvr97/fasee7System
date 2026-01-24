package com.studenttracker.service;

import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing warnings.
 * Warnings are auto-generated based on business rules and auto-resolved when conditions change.
 */
public interface WarningService {
    
    // ========== Generate Warnings (Called by event subscribers) ==========
    
    /**
     * Generate an absence warning based on consecutive count.
     * - 2 consecutive absences → CONSECUTIVE_ABSENCE warning
     * - 3 consecutive absences → ARCHIVED warning
     * 
     * @param studentId The student ID
     * @param consecutiveCount The consecutive absence count
     * @return Generated warning ID
     */
    Integer generateAbsenceWarning(Integer studentId, int consecutiveCount);
    
    /**
     * Generate a behavioral warning.
     * 
     * @param studentId The student ID
     * @param reason The reason for the warning
     * @return Generated warning ID
     */
    Integer generateBehavioralWarning(Integer studentId, String reason);
    
    
    // ========== Resolve Warnings ==========
    
    /**
     * Resolve a warning by ID.
     * 
     * @param warningId The warning ID
     * @param resolvedReason The reason for resolution
     * @return true if successful, false otherwise
     */
    boolean resolveWarning(Integer warningId, String resolvedReason);
    
    /**
     * Resolve all warnings of a specific type for a student.
     * 
     * @param studentId The student ID
     * @param type The warning type to resolve
     * @return true if any warnings were resolved, false otherwise
     */
    boolean resolveWarningsByStudent(Integer studentId, WarningType type);
    
    
    // ========== Retrieval ==========
    
    /**
     * Get a warning by ID.
     * 
     * @param warningId The warning ID
     * @return The warning or null if not found
     */
    Warning getWarningById(Integer warningId);
    
    /**
     * Get all active warnings.
     * 
     * @return List of active warnings
     */
    List<Warning> getActiveWarnings();
    
    /**
     * Get all resolved warnings.
     * 
     * @return List of resolved warnings
     */
    List<Warning> getResolvedWarnings();
    
    /**
     * Get all warnings for a student.
     * 
     * @param studentId The student ID
     * @return List of warnings
     */
    List<Warning> getWarningsByStudent(Integer studentId);
    
    /**
     * Get active warnings for a student.
     * 
     * @param studentId The student ID
     * @return List of active warnings
     */
    List<Warning> getActiveWarningsByStudent(Integer studentId);
    
    /**
     * Get warnings by type.
     * 
     * @param type The warning type
     * @return List of warnings of that type
     */
    List<Warning> getWarningsByType(WarningType type);
    
    
    // ========== Statistics ==========
    
    /**
     * Get count of active warnings.
     * 
     * @return Number of active warnings
     */
    int getActiveWarningCount();
    
    /**
     * Get count of warnings by type.
     * 
     * @param type The warning type
     * @return Number of warnings of that type
     */
    int getWarningCountByType(WarningType type);
    
    /**
     * Get breakdown of active warnings by type.
     * 
     * @return Map of warning type to count
     */
    Map<WarningType, Integer> getWarningTypeBreakdown();
    
    
    // ========== Check and Generate ==========
    
    /**
     * Check if warnings should be generated for a student and generate them if needed.
     * Checks:
     * - Consecutive absences (2 or 3)
     * - 2 consecutive same-type behavioral incidents
     * - 3 behavioral incidents in a month
     * 
     * @param studentId The student ID
     */
    void checkAndGenerateWarnings(Integer studentId);
}