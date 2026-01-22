package com.studenttracker.exception;

/**
 * Custom runtime exception for DAO layer errors.
 * Wraps SQLExceptions and other database-related errors.
 */
public class DAOException extends RuntimeException {
    
    public DAOException(String message) {
        super(message);
    }
    
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DAOException(Throwable cause) {
        super(cause);
    }
}