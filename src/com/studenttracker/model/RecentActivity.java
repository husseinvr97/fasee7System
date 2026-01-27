package com.studenttracker.model;

import java.time.LocalDateTime;

public class RecentActivity {
    private int activityId;
    private String activityType;
    private String activityDescription;
    private String entityType;
    private Integer entityId;
    private Integer performedBy;
    private LocalDateTime createdAt;
    
    // Constructors
    public RecentActivity() {}
    
    public RecentActivity(String activityType, String activityDescription, 
                         String entityType, Integer entityId, Integer performedBy) {
        this.activityType = activityType;
        this.activityDescription = activityDescription;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
    }
    
    // Full constructor (for DAO when reading from DB)
    public RecentActivity(int activityId, String activityType, 
                         String activityDescription, String entityType, 
                         Integer entityId, Integer performedBy, 
                         LocalDateTime createdAt) {
        this.activityId = activityId;
        this.activityType = activityType;
        this.activityDescription = activityDescription;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }
    
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    
    public String getActivityDescription() { return activityDescription; }
    public void setActivityDescription(String description) { this.activityDescription = description; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
    
    public Integer getPerformedBy() { return performedBy; }
    public void setPerformedBy(Integer performedBy) { this.performedBy = performedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "RecentActivity{" +
                "activityId=" + activityId +
                ", activityType='" + activityType + '\'' +
                ", description='" + activityDescription + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}