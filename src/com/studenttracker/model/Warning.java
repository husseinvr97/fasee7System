package com.studenttracker.model;

import java.time.LocalDateTime;

public class Warning {
    public enum WarningType {
        CONSECUTIVE_ABSENCE,
        ARCHIVED,
        BEHAVIORAL
    }

    private Integer warningId;
    private Integer studentId;
    private WarningType warningType;
    private String warningReason;
    private LocalDateTime createdAt;
    private boolean isActive;
    private LocalDateTime resolvedAt;

    public Warning() {
    }

    public Warning(Integer studentId, WarningType warningType, String warningReason, 
                   LocalDateTime createdAt, boolean isActive, LocalDateTime resolvedAt) {
        this.studentId = studentId;
        this.warningType = warningType;
        this.warningReason = warningReason;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.resolvedAt = resolvedAt;
    }

    public Integer getWarningId() {
        return warningId;
    }

    public void setWarningId(Integer warningId) {
        this.warningId = warningId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public String getWarningReason() {
        return warningReason;
    }

    public void setWarningReason(String warningReason) {
        this.warningReason = warningReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void resolve() {
        this.isActive = false;
        this.resolvedAt = LocalDateTime.now();
    }

    public boolean isResolved() {
        return !isActive;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "warningId=" + warningId +
                ", studentId=" + studentId +
                ", warningType=" + warningType +
                ", warningReason='" + warningReason + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                ", resolvedAt=" + resolvedAt +
                '}';
    }
}