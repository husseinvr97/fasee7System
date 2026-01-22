package com.studenttracker.exception;

/**
 * Thrown when attempting to register a phone number that already exists.
 * Prevents duplicate student registrations.
 */
public class DuplicatePhoneNumberException extends ValidationException {
    
    private final String phoneNumber;
    
    public DuplicatePhoneNumberException(String phoneNumber) {
        super("Phone number already registered: " + phoneNumber);
        this.phoneNumber = phoneNumber;
    }
    
    public DuplicatePhoneNumberException(String phoneNumber, String message) {
        super(message);
        this.phoneNumber = phoneNumber;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
}