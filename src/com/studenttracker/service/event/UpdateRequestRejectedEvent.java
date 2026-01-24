package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an update request is rejected.
 */
public class UpdateRequestRejectedEvent implements Event {
    private final Integer requestId;
    private final Integer rejectedBy;
    private final String reason;
    private final LocalDateTime rejectedAt;
    
    public UpdateRequestRejectedEvent(Integer requestId, Integer rejectedBy, 
                                     String reason, LocalDateTime rejectedAt) {
        this.requestId = requestId;
        this.rejectedBy = rejectedBy;
        this.reason = reason;
        this.rejectedAt = rejectedAt;
    }
    
    public Integer getRequestId() { return requestId; }
    public Integer getRejectedBy() { return rejectedBy; }
    public String getReason() { return reason; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    
    @Override
    public String toString() {
        return "UpdateRequestRejectedEvent{requestId=" + requestId + 
               ", rejectedBy=" + rejectedBy + ", reason=" + reason + "}";
    }
}