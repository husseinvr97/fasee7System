package com.studenttracker.service.impl;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.RecentActivityDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.model.RecentActivity;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.RecentActivityService;
import com.studenttracker.service.event.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of RecentActivityService.
 * Handles activity logging, event subscription, and cleanup operations.
 */
public class RecentActivityServiceImpl implements RecentActivityService {
    
    private static final Logger LOGGER = Logger.getLogger(RecentActivityServiceImpl.class.getName());
    
    private final RecentActivityDAO activityDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     * Automatically registers this service with the EventBus to listen for events.
     * 
     * @param activityDAO DAO for recent activity data access
     */
    public RecentActivityServiceImpl(RecentActivityDAO activityDAO) {
        this.activityDAO = activityDAO;
        this.eventBusService = EventBusService.getInstance();
        
        // Register to listen to all relevant events
        eventBusService.register(this);
    }
    
    
    // ========== Public Service Methods ==========
    
    @Override
    public List<RecentActivity> getRecentActivities(int limit) {
        try {
            return activityDAO.getRecent(limit);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch recent activities", e);
            return List.of(); // Return empty list on error
        }
    }
    
    @Override
    public void logActivity(String activityType, String description, 
                           String entityType, Integer entityId, Integer performedBy) {
        try {
            RecentActivity activity = new RecentActivity(
                activityType, 
                description, 
                entityType, 
                entityId, 
                performedBy
            );
            activityDAO.insert(activity);
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Failed to log activity: " + description, e);
            // Don't propagate exception - activity logging should not disrupt operations
        }
    }
    
    @Override
    public void cleanupOldActivities(int olderThanDays) {
        try {
            int deleted = activityDAO.deleteOlderThan(olderThanDays);
            LOGGER.info("Cleaned up " + deleted + " old activities");
        } catch (DAOException e) {
            LOGGER.log(Level.SEVERE, "Failed to cleanup old activities", e);
        }
    }
    
    
    // ==================== EVENT LISTENERS ====================
    
    /**
     * Listens for LessonCreatedEvent and logs the activity.
     * 
     * @param event The lesson created event
     */
    @Subscribe
    public void onLessonCreated(LessonCreatedEvent event) {
        logActivity(
            "LESSON_CREATED",
            "Lesson " + event.getLessonId() + " created",
            "LESSON",
            event.getLessonId(),
            event.getCreatedBy()
        );
    }
    
    /**
     * Listens for StudentRegisteredEvent and logs the activity.
     * 
     * @param event The student registered event
     */
    @Subscribe
    public void onStudentRegistered(StudentRegisteredEvent event) {
        logActivity(
            "STUDENT_REGISTERED",
            "Student " + event.getFullName() + " registered",
            "STUDENT",
            event.getStudentId(),
            event.getRegisteredBy()
        );
    }
    
    /**
     * Listens for StudentArchivedEvent and logs the activity.
     * 
     * @param event The student archived event
     */
    @Subscribe
    public void onStudentArchived(StudentArchivedEvent event) {
        logActivity(
            "STUDENT_ARCHIVED",
            "Student archived: " + event.getReason(),
            "STUDENT",
            event.getStudentId(),
            event.getArchivedBy()
        );
    }
    
    /**
     * Listens for WarningGeneratedEvent and logs the activity.
     * 
     * @param event The warning generated event
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        logActivity(
            "WARNING_GENERATED",
            "Warning generated for student: " + event.getWarningType(),
            "WARNING",
            event.getWarningId(),
            null // System generated
        );
    }
    
    /**
     * Listens for MissionCompletedEvent and logs the activity.
     * 
     * @param event The mission completed event
     */
    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent event) {
        logActivity(
            "MISSION_COMPLETED",
            event.getType().toString() + " completed for Lesson " + event.getLessonId(),
            "MISSION",
            event.getMissionId(),
            event.getCompletedBy()
        );
    }
    
    /**
     * Listens for QuizGradingCompletedEvent and logs the activity.
     * 
     * @param event The quiz grading completed event
     */
    @Subscribe
    public void onQuizGradingCompleted(QuizGradingCompletedEvent event) {
        logActivity(
            "QUIZ_GRADED",
            "Quiz grading completed for Lesson " + event.getLessonId() + 
            " (" + event.getTotalStudentsGraded() + " students graded)",
            "QUIZ",
            event.getQuizId(),
            event.getCompletedBy()
        );
    }
    
    // Additional event listeners can be added here as needed for future events
}