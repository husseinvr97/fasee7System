package com.studenttracker.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Fasee7Snapshot {
    private Integer snapshotId;
    private LocalDate snapshotDate;
    private String snapshotData;
    private LocalDateTime createdAt;

    public Fasee7Snapshot() {
    }

    public Fasee7Snapshot(LocalDate snapshotDate, String snapshotData, 
                          LocalDateTime createdAt) {
        this.snapshotDate = snapshotDate;
        this.snapshotData = snapshotData;
        this.createdAt = createdAt;
    }

    public Integer getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Integer snapshotId) {
        this.snapshotId = snapshotId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public String getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Fasee7Snapshot{" +
                "snapshotId=" + snapshotId +
                ", snapshotDate=" + snapshotDate +
                ", snapshotData='" + snapshotData + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}