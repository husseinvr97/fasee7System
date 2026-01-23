package com.studenttracker.dao;

import com.studenttracker.exception.DAOException;
import com.studenttracker.model.Warning;
import com.studenttracker.model.Warning.WarningType;

import java.util.List;

/**
 * DAO interface for Warning operations
 * TODO: Implement this interface and its implementation class
 */
public interface WarningDAO {
    
    /**
     * Insert a new warning
     * TODO: Implement this method
     */
    Integer insert(Warning warning) throws DAOException;
    
    /**
     * Find all active warnings for a student
     * TODO: Implement this method
     */
    List<Warning> findActiveByStudentId(int studentId) throws DAOException;
    
    /**
     * Find all warnings for a student
     * TODO: Implement this method
     */
    List<Warning> findByStudentId(int studentId) throws DAOException;
    
    /**
     * Resolve a warning by ID
     * TODO: Implement this method
     */
    boolean resolve(int warningId) throws DAOException;
    
    /**
     * Find warnings by type
     * TODO: Implement this method
     */
    List<Warning> findByType(WarningType type) throws DAOException;
}