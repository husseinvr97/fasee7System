package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an update request is rejected.
 */
public class UpdateRequestRejectedEvent implements Event {
    private final Integer requestId;
    private final String requestType;
    private final Integer rejectedBy;
    private final String reason;
    private final LocalDateTime rejectedAt = LocalDateTime.now();
    
    public UpdateRequestRejectedEvent(Integer requestId, Integer rejectedBy, 
                                     String reason, String requestType) {
        this.requestId = requestId;
        this.rejectedBy = rejectedBy;
        this.reason = reason;
        this.requestType = requestType;
    }
    
    public Integer getRequestId() { return requestId; }
    public Integer getRejectedBy() { return rejectedBy; }
    public String getReason() { return reason; }
    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public String getRequestType() { return requestType; }
    
    @Override
    public String toString() {
        return "UpdateRequestRejectedEvent{" +
                "requestId=" + requestId +
                ", requestType='" + requestType + '\'' +
                ", rejectedBy=" + rejectedBy +
                ", reason='" + reason + '\'' +
                ", rejectedAt=" + rejectedAt +
                '}';
    }
}