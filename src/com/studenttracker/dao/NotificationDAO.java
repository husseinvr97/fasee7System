package com.studenttracker.dao;

import com.studenttracker.model.Notification;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationDAO {
    
    // Standard CRUD methods
    Integer insert(Notification notification);
    boolean update(Notification notification);
    boolean delete(int notificationId);
    Notification findById(int notificationId);
    List<Notification> findAll();
    
    // Custom methods
    List<Notification> findByUserId(int userId);
    List<Notification> findUnreadByUser(int userId);
    int countUnreadByUser(int userId);
    boolean markAsRead(int notificationId);
    boolean markAllAsReadForUser(int userId);
    List<Notification> findByUserAndType(int userId, String notificationType);
    boolean deleteOldNotifications(LocalDateTime before);
}