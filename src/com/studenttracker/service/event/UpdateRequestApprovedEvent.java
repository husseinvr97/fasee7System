package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an update request is approved and executed successfully.
 */
public class UpdateRequestApprovedEvent implements Event {
    private final Integer requestId;
    private final String requestType;
    private final String entityType;
    private final Integer entityId;
    private final Integer approvedBy;
    private final LocalDateTime approvedAt = LocalDateTime.now();
    
    public UpdateRequestApprovedEvent(Integer requestId, Integer approvedBy, 
                                      String entityType, Integer entityId , String requestType) {
        this.requestId = requestId;
        this.approvedBy = approvedBy;
        this.entityType = entityType;
        this.entityId = entityId;
        this.requestType = requestType;
    }
    
    public Integer getRequestId() { return requestId; }
    public Integer getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getRequestType() { return requestType; }
    public String getEntityType() { return entityType; }
    public Integer getEntityId() { return entityId; }
    
    @Override
    public String toString() {
        return "UpdateRequestApprovedEvent{" +
                "requestId=" + requestId +
                ", requestType='" + requestType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", approvedBy=" + approvedBy +
                ", approvedAt=" + approvedAt +
                '}';
    }
}