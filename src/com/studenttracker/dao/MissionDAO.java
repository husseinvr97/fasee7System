package com.studenttracker.dao;

import com.studenttracker.model.Mission;
import com.studenttracker.model.Mission.MissionStatus;
import com.studenttracker.model.Mission.MissionType;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for Mission entity operations.
 * Manages task assignments for lesson-related activities (attendance/homework, quiz grading).
 */
public interface MissionDAO {
    
    // ========== Standard CRUD Methods ==========
    
    /**
     * Insert a new mission into the database.
     * @param mission Mission object to insert
     * @return Generated mission ID, or null if insert failed
     */
    Integer insert(Mission mission);
    
    /**
     * Update an existing mission.
     * @param mission Mission object with updated fields
     * @return true if update successful, false otherwise
     */
    boolean update(Mission mission);
    
    /**
     * Delete a mission by ID.
     * @param missionId ID of mission to delete
     * @return true if deletion successful, false otherwise
     */
    boolean delete(int missionId);
    
    /**
     * Find a mission by its ID.
     * @param missionId Mission ID to search for
     * @return Mission object if found, null otherwise
     */
    Mission findById(int missionId);
    
    /**
     * Retrieve all missions from the database.
     * @return List of all missions
     */
    List<Mission> findAll();
    
    // ========== Custom Query Methods ==========
    
    /**
     * Get all missions for a specific lesson.
     * @param lessonId Lesson ID
     * @return List of missions for the lesson
     */
    List<Mission> findByLessonId(int lessonId);
    
    /**
     * Get all missions assigned to a specific user.
     * @param userId User ID who is assigned the missions
     * @return List of missions assigned to the user
     */
    List<Mission> findByAssignedTo(int userId);
    
    /**
     * Get missions filtered by status.
     * @param status Mission status (IN_PROGRESS or COMPLETED)
     * @return List of missions with the specified status
     */
    List<Mission> findByStatus(MissionStatus status);
    
    /**
     * Count missions by status.
     * @param status Mission status to count
     * @return Number of missions with the specified status
     */
    int countByStatus(MissionStatus status);
    
    /**
     * Get a specific mission by lesson and type.
     * Typically only one mission of each type exists per lesson.
     * @param lessonId Lesson ID
     * @param type Mission type (ATTENDANCE_HOMEWORK or QUIZ_GRADING)
     * @return Mission if found, null otherwise
     */
    Mission findByLessonAndType(int lessonId, MissionType type);
    
    /**
     * Get all IN_PROGRESS missions for a specific user.
     * @param userId User ID
     * @return List of pending missions for the user
     */
    List<Mission> findPendingByUser(int userId);
    
    /**
     * Get all completed missions within a date range.
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return List of completed missions in the date range
     */
    List<Mission> findCompletedByDateRange(LocalDate start, LocalDate end);
}