package com.studenttracker.exception;

/**
 * Thrown when phone number validation fails.
 * Examples:
 * - Not 11 digits
 * - Doesn't start with 01
 * - Contains non-numeric characters
 */
public class InvalidPhoneNumberException extends ValidationException {
    
    private final String phoneNumber;
    
    public InvalidPhoneNumberException(String message) {
        super(message);
        this.phoneNumber = null;
    }
    
    public InvalidPhoneNumberException(String phoneNumber, String message) {
        super(message);
        this.phoneNumber = phoneNumber;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
}