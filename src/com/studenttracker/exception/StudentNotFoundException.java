package com.studenttracker.exception;

/**
 * Thrown when a student cannot be found by ID.
 * Examples:
 * - Invalid student ID
 * - Student deleted from database
 */
public class StudentNotFoundException extends EntityException {
    
    public StudentNotFoundException(Integer studentId) {
        super(studentId, "Student not found with ID: " + studentId);
    }
    
    public StudentNotFoundException(Integer studentId, String message) {
        super(studentId, message);
    }
}