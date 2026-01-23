package com.studenttracker.dao;

import com.studenttracker.exception.DAOException;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.model.BehavioralIncident.IncidentType;

import java.time.LocalDate;
import java.util.List;

public interface BehavioralIncidentDAO {
    
    /**
     * Insert a new behavioral incident
     * @param incident The incident to insert
     * @return The generated incident ID
     * @throws DAOException if insertion fails
     */
    Integer insert(BehavioralIncident incident) throws DAOException;
    
    /**
     * Update an existing behavioral incident
     * @param incident The incident to update
     * @return true if update successful, false otherwise
     * @throws DAOException if update fails
     */
    boolean update(BehavioralIncident incident) throws DAOException;
    
    /**
     * Delete a behavioral incident by ID
     * @param incidentId The incident ID
     * @return true if deletion successful, false otherwise
     * @throws DAOException if deletion fails
     */
    boolean delete(int incidentId) throws DAOException;
    
    /**
     * Find a behavioral incident by ID
     * @param incidentId The incident ID
     * @return The incident or null if not found
     * @throws DAOException if query fails
     */
    BehavioralIncident findById(int incidentId) throws DAOException;
    
    /**
     * Find all behavioral incidents
     * @return List of all incidents ordered by created_at DESC
     * @throws DAOException if query fails
     */
    List<BehavioralIncident> findAll() throws DAOException;
    
    /**
     * Find all behavioral incidents for a specific student
     * @param studentId The student ID
     * @return List of incidents ordered by created_at DESC
     * @throws DAOException if query fails
     */
    List<BehavioralIncident> findByStudentId(int studentId) throws DAOException;
    
    /**
     * Find all behavioral incidents for a specific lesson
     * @param lessonId The lesson ID
     * @return List of incidents ordered by created_at DESC
     * @throws DAOException if query fails
     */
    List<BehavioralIncident> findByLessonId(int lessonId) throws DAOException;
    
    /**
     * Find all behavioral incidents of a specific type
     * @param type The incident type
     * @return List of incidents ordered by created_at DESC
     * @throws DAOException if query fails
     */
    List<BehavioralIncident> findByType(IncidentType type) throws DAOException;
    
    /**
     * Count total incidents for a student
     * @param studentId The student ID
     * @return Total count of incidents
     * @throws DAOException if query fails
     */
    int countByStudentId(int studentId) throws DAOException;
    
    /**
     * Find incidents for a student within a date range
     * @param studentId The student ID
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return List of incidents ordered by created_at DESC
     * @throws DAOException if query fails
     */
    List<BehavioralIncident> findByStudentAndDateRange(int studentId, LocalDate start, LocalDate end) throws DAOException;
    
    /**
     * Find the most recent N incidents for a student
     * @param studentId The student ID
     * @param limit Maximum number of incidents to return
     * @return List of recent incidents ordered by created_at DESC
     * @throws DAOException if query fails
     */
    List<BehavioralIncident> findRecentByStudent(int studentId, int limit) throws DAOException;
}