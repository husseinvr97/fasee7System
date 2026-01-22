package com.studenttracker.exception;

/**
 * Base exception for validation errors.
 * Should result in 400 Bad Request at API level.
 */
public abstract class ValidationException extends ServiceException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}