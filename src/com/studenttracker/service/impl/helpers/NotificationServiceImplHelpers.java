package com.studenttracker.service.impl.helpers;

/**
 * Helper class for NotificationServiceImpl.
 * Contains utility methods for building notification messages.
 */
public class NotificationServiceImplHelpers {
    
    private NotificationServiceImplHelpers() {}
    
    /**
     * Builds a message for update request notification.
     * 
     * @param assistantName Name of the assistant who submitted the request
     * @param requestId Request ID
     * @param requestType Type of request
     * @return Formatted notification message
     */
    public static String buildUpdateRequestMessage(String assistantName, 
                                                   Integer requestId, 
                                                   String requestType) {
        return String.format("New update request from %s (Request #%d, Type: %s)", 
                           assistantName, requestId, requestType);
    }
    
    /**
     * Builds a message for request approved notification.
     * 
     * @param requestId Request ID
     * @return Formatted notification message
     */
    public static String buildRequestApprovedMessage(Integer requestId) {
        return String.format("Your request #%d was approved", requestId);
    }
    
    /**
     * Builds a message for request rejected notification.
     * 
     * @param requestId Request ID
     * @param reason Rejection reason
     * @return Formatted notification message
     */
    public static String buildRequestRejectedMessage(Integer requestId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return String.format("Your request #%d was rejected", requestId);
        }
        return String.format("Your request #%d was rejected: %s", requestId, reason);
    }
    
    /**
     * Builds a message for mission assigned notification.
     * 
     * @param missionType Type of mission
     * @param lessonId Lesson ID
     * @return Formatted notification message
     */
    public static String buildMissionAssignedMessage(String missionType, Integer lessonId) {
        return String.format("You've been assigned: %s for Lesson #%d", 
                           missionType, lessonId);
    }
    
    /**
     * Builds a message for warning generated notification.
     * 
     * @param studentName Name of the student
     * @param warningType Type of warning
     * @return Formatted notification message
     */
    public static String buildWarningGeneratedMessage(String studentName, String warningType) {
        return String.format("Warning generated for %s: %s", studentName, warningType);
    }
}