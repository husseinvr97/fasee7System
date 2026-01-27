package com.studenttracker.service;

import com.studenttracker.dao.NotificationDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.dao.UpdateRequestDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.model.Notification;
import com.studenttracker.model.Student;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.event.NotificationSentEvent;
import com.studenttracker.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 * Tests all notification creation, delivery, and management operations.
 */
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationDAO notificationDAO;
    
    @Mock
    private UserDAO userDAO;
    
    @Mock
    private StudentDAO studentDAO;
    
    @Mock
    private UpdateRequestDAO updateRequestDAO;
    
    @Mock
    private EventBusService eventBusService;
    
    private NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
            notificationDAO, 
            userDAO, 
            studentDAO, 
            updateRequestDAO,
            eventBusService
        );
    }
    
    
    // ========== Test 15.1: Send Notification - Basic ==========
    
    @Test
    void testSendNotification_Basic() {
        // Given
        Integer userId = 1;
        String notificationType = "WARNING";
        String message = "New warning generated for Student Ahmed";
        Integer expectedNotificationId = 100;
        
        when(notificationDAO.insert(any(Notification.class))).thenReturn(expectedNotificationId);
        
        // When
        Integer result = notificationService.sendNotification(userId, notificationType, message);
        
        // Then
        assertEquals(expectedNotificationId, result);
        
        // Verify Notification object created correctly
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO).insert(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(userId, capturedNotification.getUserId());
        assertEquals(notificationType, capturedNotification.getNotificationType());
        assertEquals(message, capturedNotification.getMessage());
        assertFalse(capturedNotification.isRead());
        assertNotNull(capturedNotification.getCreatedAt());
        
        // Verify NotificationSentEvent was published
        ArgumentCaptor<NotificationSentEvent> eventCaptor = ArgumentCaptor.forClass(NotificationSentEvent.class);
        verify(eventBusService).publish(eventCaptor.capture());
        
        NotificationSentEvent event = eventCaptor.getValue();
        assertEquals(expectedNotificationId, event.getNotificationId());
        assertEquals(userId, event.getUserId());
        assertEquals(notificationType, event.getNotificationType());
        assertEquals(message, event.getMessage());
    }
    
    
    // ========== Test 15.2: Notify Update Request - To Admin ==========
    
    @Test
    void testNotifyUpdateRequest_ToAdmin() {
        // Given
        Integer adminId = 1;
        Integer requestId = 107;
        String requestType = "UPDATE_ATTENDANCE";
        Integer requestedBy = 5;
        
        User assistant = new User();
        assistant.setUserId(5);
        assistant.setFullName("Sara");
        
        when(userDAO.findById(5)).thenReturn(assistant);
        when(notificationDAO.insert(any(Notification.class))).thenReturn(200);
        
        // When
        notificationService.notifyUpdateRequest(adminId, requestId, requestType, requestedBy);
        
        // Then
        verify(userDAO).findById(5);
        
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO).insert(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(adminId, capturedNotification.getUserId());
        assertEquals("UPDATE_REQUEST", capturedNotification.getNotificationType());
        assertTrue(capturedNotification.getMessage().contains("Sara"));
        assertTrue(capturedNotification.getMessage().contains("107"));
        
        // Verify event published
        verify(eventBusService).publish(any(NotificationSentEvent.class));
    }
    
    
    // ========== Test 15.3: Notify Request Approved - To Assistant ==========
    
    @Test
    void testNotifyRequestApproved_ToAssistant() {
        // Given
        Integer assistantId = 5;
        Integer requestId = 107;
        
        when(notificationDAO.insert(any(Notification.class))).thenReturn(201);
        
        // When
        notificationService.notifyRequestApproved(assistantId, requestId);
        
        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO).insert(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(assistantId, capturedNotification.getUserId());
        assertEquals("REQUEST_APPROVED", capturedNotification.getNotificationType());
        assertEquals("Your request #107 was approved", capturedNotification.getMessage());
        
        // Verify event published
        verify(eventBusService).publish(any(NotificationSentEvent.class));
    }
    
    
    // ========== Test 15.4: Notify Request Rejected - To Assistant ==========
    
    @Test
    void testNotifyRequestRejected_ToAssistant() {
        // Given
        Integer assistantId = 5;
        Integer requestId = 108;
        String reason = "Duplicate request";
        
        when(notificationDAO.insert(any(Notification.class))).thenReturn(202);
        
        // When
        notificationService.notifyRequestRejected(assistantId, requestId, reason);
        
        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO).insert(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(assistantId, capturedNotification.getUserId());
        assertEquals("REQUEST_REJECTED", capturedNotification.getNotificationType());
        assertEquals("Your request #108 was rejected: Duplicate request", 
                     capturedNotification.getMessage());
        
        // Verify event published
        verify(eventBusService).publish(any(NotificationSentEvent.class));
    }
    
    
    // ========== Test 15.5: Notify Mission Assigned - To User ==========
    
    @Test
    void testNotifyMissionAssigned_ToUser() {
        // Given
        Integer userId = 5;
        Integer missionId = 10;
        Integer lessonId = 50;
        String missionType = "ATTENDANCE_HOMEWORK";
        
        when(notificationDAO.insert(any(Notification.class))).thenReturn(203);
        
        // When
        notificationService.notifyMissionAssigned(userId, missionId, lessonId, missionType);
        
        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO).insert(notificationCaptor.capture());
        
        Notification capturedNotification = notificationCaptor.getValue();
        assertEquals(userId, capturedNotification.getUserId());
        assertEquals("MISSION_ASSIGNED", capturedNotification.getNotificationType());
        assertTrue(capturedNotification.getMessage().contains("ATTENDANCE_HOMEWORK"));
        assertTrue(capturedNotification.getMessage().contains("50"));
        
        // Verify event published
        verify(eventBusService).publish(any(NotificationSentEvent.class));
    }
    
    
    // ========== Test 15.6: Notify Warning Generated - To All Admins ==========
    
    @Test
    void testNotifyWarningGenerated_ToAllAdmins() {
        // Given
        Integer adminId = 1;
        Integer studentId = 1;
        String warningType = "CONSECUTIVE_ABSENCE";
        
        Student student = new Student();
        student.setStudentId(1);
        student.setFullName("محمد أحمد");
        
        User admin1 = new User();
        admin1.setUserId(1);
        admin1.setRole(UserRole.ADMIN);
        admin1.setActive(true);
        
        User admin2 = new User();
        admin2.setUserId(2);
        admin2.setRole(UserRole.ADMIN);
        admin2.setActive(true);
        
        when(studentDAO.findById(1)).thenReturn(student);
        when(userDAO.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(admin1, admin2));
        when(notificationDAO.insert(any(Notification.class))).thenReturn(204, 205, 206);
        
        // When
        notificationService.notifyWarningGenerated(adminId, studentId, warningType);
        
        // Then
        // Then
verify(studentDAO, times(1)).findById(1); // Only called once to get student name
verify(userDAO).findByRole(UserRole.ADMIN);
verify(notificationDAO, times(3)).insert(any(Notification.class)); // 1 for specific admin + 2 for all admins // 1 for specific admin + 2 for all admins
        
        // Verify 3 events published
        verify(eventBusService, times(3)).publish(any(NotificationSentEvent.class));
    }
    
    
    // ========== Test 15.7: Notify All Admins ==========
    
    @Test
    void testNotifyAllAdmins() {
        // Given
        String notificationType = "SYSTEM_ALERT";
        String message = "System maintenance scheduled";
        
        User admin1 = new User();
        admin1.setUserId(1);
        admin1.setActive(true);
        
        User admin2 = new User();
        admin2.setUserId(2);
        admin2.setActive(true);
        
        User admin3 = new User();
        admin3.setUserId(3);
        admin3.setActive(true);
        
        when(userDAO.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(admin1, admin2, admin3));
        when(notificationDAO.insert(any(Notification.class))).thenReturn(206, 207, 208);
        
        // When
        notificationService.notifyAllAdmins(notificationType, message);
        
        // Then
        verify(userDAO).findByRole(UserRole.ADMIN);
        verify(notificationDAO, times(3)).insert(any(Notification.class));
        
        // Verify each admin received a notification
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO, times(3)).insert(notificationCaptor.capture());
        
        List<Notification> notifications = notificationCaptor.getAllValues();
        assertEquals(3, notifications.size());
        assertEquals(notificationType, notifications.get(0).getNotificationType());
        assertEquals(message, notifications.get(0).getMessage());
        
        // Verify 3 events published
        verify(eventBusService, times(3)).publish(any(NotificationSentEvent.class));
    }
    
    
    // ========== Test 15.8: Mark Notification as Read ==========
    
    @Test
    void testMarkAsRead() {
        // Given
        Integer notificationId = 50;
        
        when(notificationDAO.markAsRead(50)).thenReturn(true);
        
        // When
        boolean result = notificationService.markAsRead(notificationId);
        
        // Then
        assertTrue(result);
        verify(notificationDAO).markAsRead(50);
    }
    
    
    // ========== Test 15.9: Mark All as Read for User ==========
    
    @Test
    void testMarkAllAsRead() {
        // Given
        Integer userId = 5;
        
        when(notificationDAO.markAllAsReadForUser(5)).thenReturn(true);
        
        // When
        boolean result = notificationService.markAllAsRead(userId);
        
        // Then
        assertTrue(result);
        verify(notificationDAO).markAllAsReadForUser(5);
    }
    
    
    // ========== Test 15.10: Get Unread Notifications ==========
    
    @Test
    void testGetUnreadNotifications() {
        // Given
        Integer userId = 5;
        
        Notification notif1 = new Notification();
        notif1.setNotificationId(1);
        notif1.setUserId(5);
        notif1.setRead(false);
        
        Notification notif2 = new Notification();
        notif2.setNotificationId(2);
        notif2.setUserId(5);
        notif2.setRead(false);
        
        Notification notif3 = new Notification();
        notif3.setNotificationId(3);
        notif3.setUserId(5);
        notif3.setRead(false);
        
        List<Notification> unreadNotifications = Arrays.asList(notif1, notif2, notif3);
        
        when(notificationDAO.findUnreadByUser(5)).thenReturn(unreadNotifications);
        
        // When
        List<Notification> result = notificationService.getUnreadNotifications(userId);
        
        // Then
        assertEquals(3, result.size());
        verify(notificationDAO).findUnreadByUser(5);
        assertFalse(result.get(0).isRead());
        assertFalse(result.get(1).isRead());
        assertFalse(result.get(2).isRead());
    }
    
    
    // ========== Test 15.11: Get Unread Count ==========
    
    @Test
    void testGetUnreadCount() {
        // Given
        Integer userId = 5;
        
        when(notificationDAO.countUnreadByUser(5)).thenReturn(3);
        
        // When
        int result = notificationService.getUnreadCount(userId);
        
        // Then
        assertEquals(3, result);
        verify(notificationDAO).countUnreadByUser(5);
    }
    
    
    // ========== Test 15.12: Delete Old Notifications ==========
    
    @Test
    void testDeleteOldNotifications() {
        // Given
        int daysOld = 30;
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        when(notificationDAO.deleteOldNotifications(any(LocalDateTime.class))).thenReturn(true);
        
        // When
        notificationService.deleteOldNotifications(daysOld);
        
        // Then
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(notificationDAO).deleteOldNotifications(dateCaptor.capture());
        
        LocalDateTime capturedDate = dateCaptor.getValue();
        // Verify the cutoff date is approximately 30 days ago (within 1 second tolerance)
        assertTrue(Math.abs(java.time.Duration.between(cutoffDate, capturedDate).toSeconds()) < 1);
    }
    
    
    // ========== Additional Edge Cases ==========
    
    @Test
    void testNotifyAllAdmins_SkipsInactiveAdmins() {
        // Given
        User activeAdmin = new User();
        activeAdmin.setUserId(1);
        activeAdmin.setActive(true);
        
        User inactiveAdmin = new User();
        inactiveAdmin.setUserId(2);
        inactiveAdmin.setActive(false);
        
        when(userDAO.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList(activeAdmin, inactiveAdmin));
        when(notificationDAO.insert(any(Notification.class))).thenReturn(300);
        
        // When
        notificationService.notifyAllAdmins("ALERT", "Test");
        
        // Then
        verify(notificationDAO, times(1)).insert(any(Notification.class)); // Only 1 notification for active admin
        verify(eventBusService, times(1)).publish(any(NotificationSentEvent.class)); // Only 1 event
    }
    
    @Test
    void testNotifyUpdateRequest_WithNullAssistant() {
        // Given
        when(userDAO.findById(999)).thenReturn(null);
        when(notificationDAO.insert(any(Notification.class))).thenReturn(301);
        
        // When
        notificationService.notifyUpdateRequest(1, 107, "UPDATE", 999);
        
        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO).insert(notificationCaptor.capture());
        
        assertTrue(notificationCaptor.getValue().getMessage().contains("Unknown"));
        verify(eventBusService).publish(any(NotificationSentEvent.class));
    }
    
    @Test
    void testNotifyWarningGenerated_WithNullStudent() {
        // Given
        when(studentDAO.findById(999)).thenReturn(null);
        when(userDAO.findByRole(UserRole.ADMIN)).thenReturn(Arrays.asList());
        when(notificationDAO.insert(any(Notification.class))).thenReturn(302);
        
        // When
        notificationService.notifyWarningGenerated(1, 999, "WARNING");
        
        // Then
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationDAO, atLeastOnce()).insert(notificationCaptor.capture());
        
        assertTrue(notificationCaptor.getValue().getMessage().contains("Unknown Student"));
        verify(eventBusService, atLeastOnce()).publish(any(NotificationSentEvent.class));
    }
}