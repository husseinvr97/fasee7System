package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when an update request is submitted.
 */
public class UpdateRequestSubmittedEvent implements Event {
    private final Integer requestId;
    private final String requestType;
    private final Integer requestedBy;
    private final LocalDateTime submittedAt;
    
    public UpdateRequestSubmittedEvent(Integer requestId, String requestType, 
                                      Integer requestedBy, LocalDateTime submittedAt) {
        this.requestId = requestId;
        this.requestType = requestType;
        this.requestedBy = requestedBy;
        this.submittedAt = submittedAt;
    }
    
    public Integer getRequestId() { return requestId; }
    public String getRequestType() { return requestType; }
    public Integer getRequestedBy() { return requestedBy; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

@Override
public String toString() {
    return "UpdateRequestSubmittedEvent{requestId=" + requestId + 
           ", type=" + requestType + ", by=" + requestedBy + "}";
}
}