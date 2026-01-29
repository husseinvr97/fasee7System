package com.studenttracker.service;

import com.studenttracker.model.RecentActivity;

import java.util.List;

/**
 * Service interface for managing recent activities in the system.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Retrieve recent activities for dashboard display</li>
 *   <li>Manually log activities when needed</li>
 *   <li>Listen to system events and auto-log activities</li>
 *   <li>Cleanup old activities (data retention)</li>
 * </ul>
 * 
 * <p><b>Event-Driven Architecture:</b></p>
 * <p>This service subscribes to system events (via EventBus) and automatically
 * creates activity logs when important actions occur. For example:</p>
 * <ul>
 *   <li>LessonCreatedEvent → "Lesson 48 created by Admin Ahmed"</li>
 *   <li>StudentRegisteredEvent → "Student Ali Ahmed registered"</li>
 *   <li>WarningGeneratedEvent → "Warning generated: 3 consecutive absences"</li>
 * </ul>
 * 
 * <p><b>Usage Example - Dashboard:</b></p>
 * <pre>
 * RecentActivityService activityService = serviceLocator.getRecentActivityService();
 * 
 * // Get recent 20 activities for dashboard
 * List<RecentActivity> activities = activityService.getRecentActivities(20);
 * 
 * // Display in ListView
 * activities.forEach(activity -> {
 *     System.out.println(activity.getActivityDescription());
 * });
 * </pre>
 * 
 * <p><b>Usage Example - Manual Logging:</b></p>
 * <pre>
 * // Sometimes you need to log without an event
 * activityService.logActivity(
 *     "SETTINGS_CHANGED",
 *     "System settings updated",
 *     null,  // no specific entity
 *     null,  // no entity ID
 *     getCurrentUserId()
 * );
 * </pre>
 * 
 * <p><b>Usage Example - Cleanup:</b></p>
 * <pre>
 * // Run this periodically (e.g., on app start or scheduled job)
 * activityService.cleanupOldActivities(30); // Delete activities older than 30 days
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public interface RecentActivityService {
    
    // ==================== RETRIEVE ACTIVITIES ====================
    
    /**
     * Gets the most recent N activities from the database.
     * Used primarily for dashboard display.
     * 
     * <p><b>Typical Usage:</b> Dashboard shows recent 20 activities</p>
     * 
     * <p><b>Ordering:</b> Most recent first (created_at DESC)</p>
     * 
     * @param limit Maximum number of activities to retrieve (e.g., 20)
     * @return List of recent activities (may be empty, never null)
     */
    List<RecentActivity> getRecentActivities(int limit);
    
    /**
     * Gets activities of a specific type.
     * Useful for filtering activities by category.
     * 
     * <p><b>Example Types:</b></p>
     * <ul>
     *   <li>LESSON_CREATED</li>
     *   <li>STUDENT_REGISTERED</li>
     *   <li>WARNING_GENERATED</li>
     *   <li>MISSION_COMPLETED</li>
     * </ul>
     * 
     * @param activityType The type of activity to filter by
     * @param limit Maximum number of activities to retrieve
     * @return List of matching activities (may be empty, never null)
     */
    List<RecentActivity> getActivitiesByType(String activityType, int limit);
    
    // ==================== MANUAL LOGGING ====================
    
    /**
     * Manually logs an activity to the database.
     * Use this for actions that don't have associated events.
     * 
     * <p><b>When to Use:</b></p>
     * <ul>
     *   <li>System-generated activities (no user action)</li>
     *   <li>Actions without corresponding events</li>
     *   <li>Batch operations that should be logged once</li>
     * </ul>
     * 
     * <p><b>Example:</b></p>
     * <pre>
     * // Log a settings change
     * logActivity(
     *     "SETTINGS_CHANGED",
     *     "Dashboard refresh interval updated to 5 minutes",
     *     "SETTINGS",
     *     null,
     *     adminUserId
     * );
     * </pre>
     * 
     * <p><b>Note:</b> For most activities, event listeners handle logging automatically.
     * Only use this method for special cases.</p>
     * 
     * @param activityType Type of activity (e.g., "LESSON_CREATED")
     * @param description Human-readable description
     * @param entityType Type of entity involved (can be null)
     * @param entityId ID of entity involved (can be null)
     * @param performedBy User ID who performed action (can be null for system)
     */
    void logActivity(String activityType, String description, 
                    String entityType, Integer entityId, Integer performedBy);
    
    // ==================== CLEANUP ====================
    
    /**
     * Deletes activities older than the specified number of days.
     * This is used for data retention and cleanup.
     * 
     * <p><b>Recommended Retention:</b></p>
     * <ul>
     *   <li>Development: 7-14 days (testing)</li>
     *   <li>Production: 30-90 days (depends on requirements)</li>
     * </ul>
     * 
     * <p><b>When to Run:</b></p>
     * <ul>
     *   <li>On application startup (lightweight cleanup)</li>
     *   <li>Scheduled job (e.g., daily at midnight)</li>
     *   <li>Manual trigger from admin settings</li>
     * </ul>
     * 
     * <p><b>Example:</b></p>
     * <pre>
     * // Run on app startup
     * int deleted = activityService.cleanupOldActivities(30);
     * logger.info("Cleaned up " + deleted + " old activities");
     * </pre>
     * 
     * @param olderThanDays Number of days to keep (activities older than this are deleted)
     */
    void cleanupOldActivities(int olderThanDays);
    
    // ==================== EVENT LISTENERS ====================
    
    /*
     * NOTE: Event listener methods are not part of this interface.
     * They are implemented in the service implementation with @Subscribe annotations.
     * 
     * Event Listeners (in implementation):
     * - onLessonCreated(LessonCreatedEvent)
     * - onStudentRegistered(StudentRegisteredEvent)
     * - onStudentArchived(StudentArchivedEvent)
     * - onStudentRestored(StudentRestoredEvent)
     * - onWarningGenerated(WarningGeneratedEvent)
     * - onWarningResolved(WarningResolvedEvent)
     * - onMissionAssigned(MissionAssignedEvent)
     * - onMissionCompleted(MissionCompletedEvent)
     * - onQuizGradingCompleted(QuizGradingCompletedEvent)
     * - onAttendanceBatchCompleted(AttendanceBatchCompletedEvent)
     * - onHomeworkBatchCompleted(HomeworkBatchCompletedEvent)
     * - onUpdateRequestSubmitted(UpdateRequestSubmittedEvent)
     * - onUpdateRequestApproved(UpdateRequestApprovedEvent)
     * - onUpdateRequestRejected(UpdateRequestRejectedEvent)
     */
}