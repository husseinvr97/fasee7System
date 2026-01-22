package com.studenttracker.model;

import java.time.LocalDateTime;

public class UpdateRequest {
    public enum RequestStatus {
        PENDING,
        APPROVED,
        APPLIED,
        REJECTED,
        COMPLETED,
        FAILED,
        BLOCKED
    }

    private Integer requestId;
    private String requestType;
    private String entityType;
    private Integer entityId;
    private String requestedChanges;
    private Integer requestedBy;
    private LocalDateTime requestedAt;
    private RequestStatus status;
    private Integer reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNotes;

    public UpdateRequest() {
    }

    public UpdateRequest(String requestType, String entityType, Integer entityId, 
                         String requestedChanges, Integer requestedBy, 
                         LocalDateTime requestedAt, RequestStatus status, 
                         Integer reviewedBy, LocalDateTime reviewedAt, String reviewNotes) {
        this.requestType = requestType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.requestedChanges = requestedChanges;
        this.requestedBy = requestedBy;
        this.requestedAt = requestedAt;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.reviewedAt = reviewedAt;
        this.reviewNotes = reviewNotes;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getRequestedChanges() {
        return requestedChanges;
    }

    public void setRequestedChanges(String requestedChanges) {
        this.requestedChanges = requestedChanges;
    }

    public Integer getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Integer requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Integer getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Integer reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public void approve(Integer adminId) {
        this.status = RequestStatus.APPROVED;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
    }

    public void reject(Integer adminId, String reason) {
        this.status = RequestStatus.REJECTED;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.reviewNotes = reason;
    }

    public boolean isPending() {
        return this.status == RequestStatus.PENDING;
    }

    @Override
    public String toString() {
        return "UpdateRequest{" +
                "requestId=" + requestId +
                ", requestType='" + requestType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", requestedChanges='" + requestedChanges + '\'' +
                ", requestedBy=" + requestedBy +
                ", requestedAt=" + requestedAt +
                ", status=" + status +
                ", reviewedBy=" + reviewedBy +
                ", reviewedAt=" + reviewedAt +
                ", reviewNotes='" + reviewNotes + '\'' +
                '}';
    }
}