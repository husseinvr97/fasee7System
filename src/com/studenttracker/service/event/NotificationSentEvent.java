package com.studenttracker.service.event;

import java.time.LocalDateTime;

/**
 * Event published when a notification is successfully sent to a user.
 * Published by: NotificationService
 */
public class NotificationSentEvent implements Event {
    private final Integer notificationId;
    private final Integer userId;
    private final String notificationType;
    private final String message;
    private final LocalDateTime sentAt;
    
    public NotificationSentEvent(Integer notificationId, Integer userId, 
                                String notificationType, String message) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }
    
    public Integer getNotificationId() { return notificationId; }
    public Integer getUserId() { return userId; }
    public String getNotificationType() { return notificationType; }
    public String getMessage() { return message; }
    public LocalDateTime getSentAt() { return sentAt; }
    
    @Override
    public String toString() {
        return "NotificationSentEvent{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", notificationType='" + notificationType + '\'' +
                ", message='" + message + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}