package com.studenttracker.exception;

/**
 * Thrown when full name validation fails.
 * Examples:
 * - Less than 4 name parts (الاسم الرباعي)
 * - Empty or null
 * - Only whitespace
 */
public class InvalidFullNameException extends ValidationException {
    
    private final String fullName;
    
    public InvalidFullNameException(String message) {
        super(message);
        this.fullName = null;
    }
    
    public InvalidFullNameException(String fullName, String message) {
        super(message);
        this.fullName = fullName;
    }
    
    public String getFullName() {
        return fullName;
    }
}