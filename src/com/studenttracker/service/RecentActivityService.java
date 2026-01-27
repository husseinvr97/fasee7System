package com.studenttracker.service;

import com.studenttracker.model.RecentActivity;
import java.util.List;

/**
 * Service interface for Recent Activity operations.
 * Handles activity logging, retrieval, and cleanup.
 */
public interface RecentActivityService {
    
    /**
     * Get recent activities for dashboard display.
     * 
     * @param limit Maximum number of activities to retrieve
     * @return List of recent activities ordered by created_at DESC
     */
    List<RecentActivity> getRecentActivities(int limit);
    
    /**
     * Manually log an activity (for non-event-based actions).
     * 
     * @param activityType Type of activity (e.g., "LESSON_CREATED")
     * @param description Human-readable description
     * @param entityType Type of entity (e.g., "LESSON", "STUDENT")
     * @param entityId ID of the entity
     * @param performedBy User ID who performed the action (null for system-generated)
     */
    void logActivity(String activityType, String description, 
                    String entityType, Integer entityId, Integer performedBy);
    
    /**
     * Clean up old activities.
     * Deletes activities older than specified number of days.
     * 
     * @param olderThanDays Delete activities older than this many days
     */
    void cleanupOldActivities(int olderThanDays);
}