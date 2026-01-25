package com.studenttracker.service;

import com.studenttracker.model.Notification;
import java.util.List;

/**
 * Service interface for notification operations.
 * Handles sending notifications to users based on system events.
 */
public interface NotificationService {
    
    // ========== Send Notifications ==========
    
    /**
     * Sends a notification to a specific user.
     * 
     * @param userId User to receive the notification
     * @param notificationType Type of notification (e.g., "UPDATE_REQUEST", "WARNING")
     * @param message Notification message content
     * @return ID of the created notification
     */
    Integer sendNotification(Integer userId, String notificationType, String message);
    
    /**
     * Notifies admins about a new update request.
     * 
     * @param adminId Admin to notify
     * @param requestId Update request ID
     * @param requestType Type of update request
     * @param requestedBy User who submitted the request
     */
    void notifyUpdateRequest(Integer adminId, Integer requestId, 
                            String requestType, Integer requestedBy);
    
    /**
     * Notifies an assistant that their request was approved.
     * 
     * @param assistantId Assistant to notify
     * @param requestId Approved request ID
     */
    void notifyRequestApproved(Integer assistantId, Integer requestId);
    
    /**
     * Notifies an assistant that their request was rejected.
     * 
     * @param assistantId Assistant to notify
     * @param requestId Rejected request ID
     * @param reason Reason for rejection
     */
    void notifyRequestRejected(Integer assistantId, Integer requestId, String reason);
    
    /**
     * Notifies a user about a mission assignment.
     * 
     * @param userId User assigned the mission
     * @param missionId Mission ID
     * @param lessonId Lesson ID related to the mission
     * @param missionType Type of mission (e.g., "ATTENDANCE_HOMEWORK")
     */
    void notifyMissionAssigned(Integer userId, Integer missionId, 
                              Integer lessonId, String missionType);
    
    /**
     * Notifies admins about a warning generated for a student.
     * 
     * @param adminId Admin to notify
     * @param studentId Student who received the warning
     * @param warningType Type of warning
     */
    void notifyWarningGenerated(Integer adminId, Integer studentId, String warningType);
    
    /**
     * Sends a notification to all active admins.
     * 
     * @param notificationType Type of notification
     * @param message Notification message
     */
    void notifyAllAdmins(String notificationType, String message);
    
    
    // ========== Mark as Read ==========
    
    /**
     * Marks a single notification as read.
     * 
     * @param notificationId Notification to mark as read
     * @return true if successful, false otherwise
     */
    boolean markAsRead(Integer notificationId);
    
    /**
     * Marks all notifications for a user as read.
     * 
     * @param userId User whose notifications to mark as read
     * @return true if successful, false otherwise
     */
    boolean markAllAsRead(Integer userId);
    
    
    // ========== Retrieval ==========
    
    /**
     * Gets a notification by ID.
     * 
     * @param notificationId Notification ID
     * @return Notification object or null if not found
     */
    Notification getNotificationById(Integer notificationId);
    
    /**
     * Gets all notifications for a user, ordered by creation date (newest first).
     * 
     * @param userId User ID
     * @return List of notifications
     */
    List<Notification> getUserNotifications(Integer userId);
    
    /**
     * Gets all unread notifications for a user.
     * 
     * @param userId User ID
     * @return List of unread notifications
     */
    List<Notification> getUnreadNotifications(Integer userId);
    
    /**
     * Gets the count of unread notifications for a user.
     * 
     * @param userId User ID
     * @return Number of unread notifications
     */
    int getUnreadCount(Integer userId);
    
    /**
     * Gets notifications filtered by type for a user.
     * 
     * @param userId User ID
     * @param type Notification type
     * @return List of notifications of the specified type
     */
    List<Notification> getNotificationsByType(Integer userId, String type);
    
    
    // ========== Cleanup ==========
    
    /**
     * Deletes notifications older than the specified number of days.
     * Typically used for maintenance tasks.
     * 
     * @param daysOld Number of days (e.g., 30 to delete notifications > 30 days old)
     */
    void deleteOldNotifications(int daysOld);
}