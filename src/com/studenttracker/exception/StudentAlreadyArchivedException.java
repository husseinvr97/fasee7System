package com.studenttracker.exception;

/**
 * Thrown when attempting to archive an already archived student.
 * Business rule violation.
 */
public class StudentAlreadyArchivedException extends EntityException {
    
    public StudentAlreadyArchivedException(Integer studentId) {
        super(studentId, "Student with ID " + studentId + " is already archived");
    }
    
    public StudentAlreadyArchivedException(Integer studentId, String message) {
        super(studentId, message);
    }
}