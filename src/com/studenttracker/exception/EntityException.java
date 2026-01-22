package com.studenttracker.exception;

/**
 * Base exception for entity operation errors.
 * Contains entity ID for better error reporting.
 */
public abstract class EntityException extends ServiceException {
    
    private final Integer entityId;
    
    public EntityException(Integer entityId, String message) {
        super(message);
        this.entityId = entityId;
    }
    
    public EntityException(Integer entityId, String message, Throwable cause) {
        super(message, cause);
        this.entityId = entityId;
    }
    
    public Integer getEntityId() {
        return entityId;
    }
}