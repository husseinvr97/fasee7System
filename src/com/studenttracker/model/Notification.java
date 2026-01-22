package com.studenttracker.model;

import java.time.LocalDateTime;

public class Notification {
    private Integer notificationId;
    private Integer userId;
    private String notificationType;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(Integer userId, String notificationType, String message, 
                        boolean isRead, LocalDateTime createdAt) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Integer getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Integer notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", notificationType='" + notificationType + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}