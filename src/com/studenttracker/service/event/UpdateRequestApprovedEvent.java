package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an update request is approved and executed successfully.
 */
public class UpdateRequestApprovedEvent implements Event {
    private final Integer requestId;
    private final Integer approvedBy;
    private final LocalDateTime approvedAt;
    
    public UpdateRequestApprovedEvent(Integer requestId, Integer approvedBy, 
                                     LocalDateTime approvedAt) {
        this.requestId = requestId;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
    }
    
    public Integer getRequestId() { return requestId; }
    public Integer getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    
    @Override
    public String toString() {
        return "UpdateRequestApprovedEvent{requestId=" + requestId + 
               ", approvedBy=" + approvedBy + "}";
    }
}