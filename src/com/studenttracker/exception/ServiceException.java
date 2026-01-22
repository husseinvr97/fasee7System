package com.studenttracker.exception;

/**
 * Base exception for all service layer errors.
 * Can be caught at controller level for generic error handling.
 */
public abstract class ServiceException extends RuntimeException {
    
    public ServiceException(String message) {
        super(message);
    }
    
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}