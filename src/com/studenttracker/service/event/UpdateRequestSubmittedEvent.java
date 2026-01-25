package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an update request is submitted.
 */
public class UpdateRequestSubmittedEvent implements Event {
    private final Integer requestId;
    private final String requestType;
    private final String entityType;
    private final Integer entityId;
    private final Integer requestedBy;
    private final LocalDateTime submittedAt = LocalDateTime.now();
    
    public UpdateRequestSubmittedEvent(Integer requestId, String requestType, 
                                      Integer requestedBy, String entityType, Integer entityId) {
        this.requestId = requestId;
        this.requestType = requestType;
        this.requestedBy = requestedBy;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    public Integer getRequestId() { return requestId; }
    public String getRequestType() { return requestType; }
    public Integer getRequestedBy() { return requestedBy; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getEntityType() { return entityType; }
    public Integer getEntityId() { return entityId; }

@Override
public String toString() {
    return "UpdateRequestSubmittedEvent{" +
            "requestId=" + requestId +
            ", requestType='" + requestType + '\'' +
            ", entityType='" + entityType + '\'' +
            ", entityId=" + entityId +
            ", requestedBy=" + requestedBy +
            ", submittedAt=" + submittedAt +
            '}';
}
}