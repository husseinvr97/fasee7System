package com.studenttracker.dao;

import com.studenttracker.exception.DAOException;
import com.studenttracker.model.ConsecutivityTracking;
import com.studenttracker.model.ConsecutivityTracking.TrackingType;

import java.util.List;

public interface ConsecutivityTrackingDAO {
    
    /**
     * Insert a new consecutivity tracking record
     * @param tracking The tracking record to insert
     * @return The generated tracking ID
     * @throws DAOException if insertion fails
     */
    Integer insert(ConsecutivityTracking tracking) throws DAOException;
    
    /**
     * Update an existing consecutivity tracking record
     * @param tracking The tracking record to update
     * @return true if update successful, false otherwise
     * @throws DAOException if update fails
     */
    boolean update(ConsecutivityTracking tracking) throws DAOException;
    
    /**
     * Delete a consecutivity tracking record by ID
     * @param trackingId The tracking ID
     * @return true if deletion successful, false otherwise
     * @throws DAOException if deletion fails
     */
    boolean delete(int trackingId) throws DAOException;
    
    /**
     * Find a consecutivity tracking record by ID
     * @param trackingId The tracking ID
     * @return The tracking record or null if not found
     * @throws DAOException if query fails
     */
    ConsecutivityTracking findById(int trackingId) throws DAOException;
    
    /**
     * Find all consecutivity tracking records
     * @return List of all tracking records
     * @throws DAOException if query fails
     */
    List<ConsecutivityTracking> findAll() throws DAOException;
    
    /**
     * Find tracking record for a specific student and type
     * @param studentId The student ID
     * @param type The tracking type (ABSENCE or BEHAVIORAL_INCIDENT)
     * @return The tracking record or null if not found
     * @throws DAOException if query fails
     */
    ConsecutivityTracking findByStudentAndType(int studentId, TrackingType type) throws DAOException;
    
    /**
     * Insert or update a tracking record (handles UNIQUE constraint)
     * Attempts insert first, if fails due to duplicate, performs update instead
     * @param tracking The tracking record to upsert
     * @return true if operation successful
     * @throws DAOException if operation fails
     */
    boolean upsert(ConsecutivityTracking tracking) throws DAOException;
    
    /**
     * Find all tracking records with consecutive count >= threshold
     * @param type The tracking type
     * @param minCount Minimum consecutive count threshold
     * @return List of tracking records meeting threshold
     * @throws DAOException if query fails
     */
    List<ConsecutivityTracking> findByThreshold(TrackingType type, int minCount) throws DAOException;
    
    /**
     * Reset all tracking records for a student (set consecutive_count = 0)
     * @param studentId The student ID
     * @throws DAOException if operation fails
     */
    void resetByStudentId(int studentId) throws DAOException;
}