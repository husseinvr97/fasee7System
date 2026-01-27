package com.studenttracker.service.impl;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.NotificationDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.dao.UpdateRequestDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.model.Notification;
import com.studenttracker.model.Student;
import com.studenttracker.model.UpdateRequest;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.NotificationService;
import com.studenttracker.service.event.*;
import com.studenttracker.service.impl.helpers.NotificationServiceImplHelpers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of NotificationService.
 * Handles notification creation, delivery, and management.
 * Subscribes to system events to send relevant notifications.
 */
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationDAO notificationDAO;
    private final UserDAO userDAO;
    private final StudentDAO studentDAO;
    private final UpdateRequestDAO updateRequestDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     * Registers this service as an event subscriber.
     * 
     * @param notificationDAO DAO for notification data access
     * @param userDAO DAO for user data access
     * @param studentDAO DAO for student data access
     * @param updateRequestDAO DAO for update request data access
     */
    public NotificationServiceImpl(NotificationDAO notificationDAO, UserDAO userDAO,
                                  StudentDAO studentDAO, UpdateRequestDAO updateRequestDAO , EventBusService eventBusService) {
        this.notificationDAO = notificationDAO;
        this.userDAO = userDAO;
        this.studentDAO = studentDAO;
        this.updateRequestDAO = updateRequestDAO;
        this.eventBusService = eventBusService;
        
        // Register as event subscriber
        this.eventBusService.register(this);
    }
    
    
    // ========== Send Notifications ==========
    
    @Override
    public Integer sendNotification(Integer userId, String notificationType, String message) {
        // Step 1: Create Notification object
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotificationType(notificationType);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        // Step 2: Persist to database
        Integer notificationId = notificationDAO.insert(notification);
        
        // Step 3: Publish NotificationSentEvent
        NotificationSentEvent event = new NotificationSentEvent(
            notificationId,
            userId,
            notificationType,
            message
        );
        eventBusService.publish(event);
        
        // Step 4: Return notification ID
        return notificationId;
    }
    
    @Override
    public void notifyUpdateRequest(Integer adminId, Integer requestId, 
                                    String requestType, Integer requestedBy) {
        // Get assistant name
        User assistant = userDAO.findById(requestedBy);
        String assistantName = assistant != null ? assistant.getFullName() : "Unknown";
        
        // Build message
        String message = NotificationServiceImplHelpers.buildUpdateRequestMessage(
            assistantName, requestId, requestType
        );
        
        // Send notification
        sendNotification(adminId, "UPDATE_REQUEST", message);
    }
    
    @Override
    public void notifyRequestApproved(Integer assistantId, Integer requestId) {
        // Build message
        String message = NotificationServiceImplHelpers.buildRequestApprovedMessage(requestId);
        
        // Send notification
        sendNotification(assistantId, "REQUEST_APPROVED", message);
    }
    
    @Override
    public void notifyRequestRejected(Integer assistantId, Integer requestId, String reason) {
        // Build message
        String message = NotificationServiceImplHelpers.buildRequestRejectedMessage(
            requestId, reason
        );
        
        // Send notification
        sendNotification(assistantId, "REQUEST_REJECTED", message);
    }
    
    @Override
    public void notifyMissionAssigned(Integer userId, Integer missionId, 
                                     Integer lessonId, String missionType) {
        // Build message
        String message = NotificationServiceImplHelpers.buildMissionAssignedMessage(
            missionType, lessonId
        );
        
        // Send notification
        sendNotification(userId, "MISSION_ASSIGNED", message);
    }
    
    @Override
    public void notifyWarningGenerated(Integer adminId, Integer studentId, String warningType) {
        // Get student name
        Student student = studentDAO.findById(studentId);
        String studentName = student != null ? student.getFullName() : "Unknown Student";
        
        // Build message
        String message = NotificationServiceImplHelpers.buildWarningGeneratedMessage(
            studentName, warningType
        );
        
        // Send notification to specific admin
        sendNotification(adminId, "WARNING", message);
        
        // Also notify all admins
        notifyAllAdmins("WARNING", message);
    }
    
    @Override
    public void notifyAllAdmins(String notificationType, String message) {
        // Get all active admins
        List<User> admins = userDAO.findByRole(UserRole.ADMIN);
        
        // Send notification to each admin
        for (User admin : admins) {
            if (admin.isActive()) {
                sendNotification(admin.getUserId(), notificationType, message);
            }
        }
    }
    
    
    // ========== Mark as Read ==========
    
    @Override
    public boolean markAsRead(Integer notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }
    
    @Override
    public boolean markAllAsRead(Integer userId) {
        return notificationDAO.markAllAsReadForUser(userId);
    }
    
    
    // ========== Retrieval ==========
    
    @Override
    public Notification getNotificationById(Integer notificationId) {
        return notificationDAO.findById(notificationId);
    }
    
    @Override
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationDAO.findByUserId(userId);
    }
    
    @Override
    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationDAO.findUnreadByUser(userId);
    }
    
    @Override
    public int getUnreadCount(Integer userId) {
        return notificationDAO.countUnreadByUser(userId);
    }
    
    @Override
    public List<Notification> getNotificationsByType(Integer userId, String type) {
        return notificationDAO.findByUserAndType(userId, type);
    }
    
    
    // ========== Cleanup ==========
    
    @Override
    public void deleteOldNotifications(int daysOld) {
        // Calculate cutoff date
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        
        // Delete old notifications
        notificationDAO.deleteOldNotifications(cutoffDate);
    }
    
    
    // ========== Event Subscribers ==========
    
    /**
     * Handles UpdateRequestSubmittedEvent.
     * Notifies all admins about the new update request.
     */
    @Subscribe
    public void onUpdateRequestSubmitted(UpdateRequestSubmittedEvent event) {
        // Get all admins
        List<User> admins = userDAO.findByRole(UserRole.ADMIN);
        
        // Notify each admin
        for (User admin : admins) {
            if (admin.isActive()) {
                notifyUpdateRequest(
                    admin.getUserId(),
                    event.getRequestId(),
                    event.getRequestType(),
                    event.getRequestedBy()
                );
            }
        }
    }
    
    /**
     * Handles UpdateRequestApprovedEvent.
     * Notifies the assistant who submitted the request.
     */
    @Subscribe
    public void onUpdateRequestApproved(UpdateRequestApprovedEvent event) {
        // Get the original request to find who submitted it
        UpdateRequest request = updateRequestDAO.findById(event.getRequestId());
        
        if (request != null) {
            notifyRequestApproved(request.getRequestedBy(), event.getRequestId());
        }
    }
    
    /**
     * Handles UpdateRequestRejectedEvent.
     * Notifies the assistant who submitted the request.
     */
    @Subscribe
    public void onUpdateRequestRejected(UpdateRequestRejectedEvent event) {
        // Get the original request to find who submitted it
        UpdateRequest request = updateRequestDAO.findById(event.getRequestId());
        
        if (request != null) {
            notifyRequestRejected(
                request.getRequestedBy(),
                event.getRequestId(),
                event.getReason()
            );
        }
    }
    
    /**
     * Handles MissionAssignedEvent.
     * Notifies the user who was assigned the mission.
     */
    @Subscribe
    public void onMissionAssigned(MissionAssignedEvent event) {
        notifyMissionAssigned(
            event.getAssignedTo(),
            event.getMissionId(),
            event.getLessonId(),
            event.getType().toString()
        );
    }
    
    /**
     * Handles WarningGeneratedEvent.
     * Notifies all admins about the warning.
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        // Get student name
        Student student = studentDAO.findById(event.getStudentId());
        String studentName = student != null ? student.getFullName() : "Unknown Student";
        
        // Build message
        String message = NotificationServiceImplHelpers.buildWarningGeneratedMessage(
            studentName,
            event.getWarningType().toString()
        );
        
        // Notify all admins
        notifyAllAdmins("WARNING", message);
    }
}