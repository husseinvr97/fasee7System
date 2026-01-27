package com.studenttracker.dao;

import com.studenttracker.model.RecentActivity;
import java.util.List;

/**
 * Data Access Object interface for RecentActivity entity operations.
 */
public interface RecentActivityDAO {
    
    /**
     * Insert new activity into database
     * @param activity The activity to insert
     * @return Generated activity ID
     */
    Integer insert(RecentActivity activity);
    
    /**
     * Get recent activities (limit to most recent N)
     * @param limit Maximum number of activities to retrieve
     * @return List of recent activities ordered by created_at DESC
     */
    List<RecentActivity> getRecent(int limit);
    
    /**
     * Get activities by type
     * @param activityType The type of activity to filter by
     * @param limit Maximum number of activities to retrieve
     * @return List of activities matching the type
     */
    List<RecentActivity> getByType(String activityType, int limit);
    
    /**
     * Delete old activities (older than specified days)
     * @param days Number of days to keep activities
     * @return Number of deleted records
     */
    int deleteOlderThan(int days);
}