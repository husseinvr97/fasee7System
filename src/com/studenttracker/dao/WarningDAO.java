package com.studenttracker.dao;

import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;

import java.util.List;

/**
 * Data Access Object interface for Warning entity operations.
 * Warnings are never deleted, only resolved by setting is_active to false.
 */
public interface WarningDAO {
    
    // ========== Standard CRUD Methods ==========
    
    /**
     * Insert a new warning into the database.
     * @param warning Warning object to insert
     * @return Generated warning ID, or null if insert failed
     */
    Integer insert(Warning warning);
    
    /**
     * Update an existing warning.
     * @param warning Warning object with updated fields
     * @return true if update successful, false otherwise
     */
    boolean update(Warning warning);
    
    /**
     * Delete a warning by ID.
     * Note: In practice, warnings should be resolved, not deleted.
     * @param warningId ID of warning to delete
     * @return true if deletion successful, false otherwise
     */
    boolean delete(int warningId);
    
    /**
     * Find a warning by its ID.
     * @param warningId Warning ID to search for
     * @return Warning object if found, null otherwise
     */
    Warning findById(int warningId);
    
    /**
     * Retrieve all warnings from the database.
     * @return List of all warnings
     */
    List<Warning> findAll();
    
    // ========== Custom Query Methods ==========
    
    /**
     * Get all warnings for a specific student, ordered by creation date (newest first).
     * @param studentId Student ID
     * @return List of warnings for the student
     */
    List<Warning> findByStudentId(int studentId);
    
    /**
     * Get warnings filtered by active status.
     * @param isActive true for active warnings, false for resolved warnings
     * @return List of warnings matching the active status
     */
    List<Warning> findByActive(boolean isActive);
    
    /**
     * Get warnings for a specific student filtered by active status.
     * @param studentId Student ID
     * @param isActive true for active warnings, false for resolved warnings
     * @return List of warnings for the student matching the active status
     */
    List<Warning> findByStudentAndActive(int studentId, boolean isActive);
    
    /**
     * Get all warnings of a specific type.
     * @param type Warning type to filter by
     * @return List of warnings of the specified type
     */
    List<Warning> findByType(WarningType type);
    
    /**
     * Count all active warnings across all students.
     * @return Number of active warnings
     */
    int countActive();
    
    /**
     * Count warnings of a specific type (both active and resolved).
     * @param type Warning type to count
     * @return Number of warnings of the specified type
     */
    int countByType(WarningType type);
    
    /**
     * Resolve all active warnings of a specific type for a student.
     * Sets is_active to false and resolved_at to current timestamp.
     * @param studentId Student ID
     * @param type Warning type to resolve
     * @return true if any warnings were resolved, false otherwise
     */
    boolean resolveWarningsByStudent(int studentId, WarningType type);
}