package com.studenttracker.exception;

/**
 * Thrown when attempting to restore an already active student.
 * Business rule violation.
 */
public class StudentAlreadyActiveException extends EntityException {
    
    public StudentAlreadyActiveException(Integer studentId) {
        super(studentId, "Student with ID " + studentId + " is already active");
    }
    
    public StudentAlreadyActiveException(Integer studentId, String message) {
        super(studentId, message);
    }
}