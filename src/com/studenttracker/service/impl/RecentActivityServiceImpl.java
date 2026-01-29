package com.studenttracker.service.impl;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.MissionDAO;
import com.studenttracker.dao.QuizDAO;
import com.studenttracker.dao.RecentActivityDAO;
import com.studenttracker.model.Mission;
import com.studenttracker.model.Quiz;
import com.studenttracker.model.RecentActivity;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.RecentActivityService;
import com.studenttracker.service.event.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of RecentActivityService with event-driven auto-logging.
 * 
 * <p><b>Design Pattern:</b> Observer Pattern (via EventBus)</p>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Listens to system events and automatically logs activities</li>
 *   <li>Graceful error handling (logs errors but doesn't crash)</li>
 *   <li>Registers with EventBus in constructor</li>
 *   <li>Thread-safe (EventBus handles concurrency)</li>
 * </ul>
 * 
 * <p><b>Architecture:</b></p>
 * <pre>
 * Service Layer Action → Publishes Event → EventBus
 *                                             ↓
 *                            RecentActivityService (@Subscribe)
 *                                             ↓
 *                                    Log to Database
 * </pre>
 * 
 * <p><b>Error Handling Strategy:</b></p>
 * <ul>
 *   <li>All database errors are caught and logged</li>
 *   <li>Errors don't propagate to event publishers (non-blocking)</li>
 *   <li>System continues to function even if activity logging fails</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class RecentActivityServiceImpl implements RecentActivityService {
    
    private static final Logger LOGGER = Logger.getLogger(RecentActivityServiceImpl.class.getName());
    
    // ==================== DEPENDENCIES ====================
    
    /**
     * DAO for database operations.
     */
    private final RecentActivityDAO activityDAO;
    
    /**
     * EventBus for subscribing to system events.
     */
    private final EventBusService eventBus;

    /**
     * DAO for database operations.
     */
    private final MissionDAO missionDAO;

    /**
     * DAO for database operations.
     */
    private final QuizDAO quizDAO;

    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor with dependency injection.
     * Automatically registers this service with EventBus to receive events.
     * 
     * @param activityDAO DAO for database operations
     * @param eventBus EventBus for event subscription
     */
    public RecentActivityServiceImpl(RecentActivityDAO activityDAO, MissionDAO missionDAO ,EventBusService eventBus , QuizDAO quizDAO) {
        this.activityDAO = activityDAO;
        this.eventBus = eventBus;
        this.missionDAO = missionDAO;
        this.quizDAO = quizDAO;
        
        
        // Register to listen to all relevant events
        eventBus.register(this);
        
        LOGGER.info("RecentActivityService initialized and registered with EventBus");
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<RecentActivity> getRecentActivities(int limit) {
        try {
            return activityDAO.getRecent(limit);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch recent activities", e);
            // Return empty list instead of crashing
            return List.of();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<RecentActivity> getActivitiesByType(String activityType, int limit) {
        try {
            return activityDAO.getByType(activityType, limit);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to fetch activities by type: " + activityType, e);
            return List.of();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void logActivity(String activityType, String description, 
                           String entityType, Integer entityId, Integer performedBy) {
        try {
            RecentActivity activity = new RecentActivity(
                activityType, description, entityType, entityId, performedBy
            );
            activityDAO.insert(activity);
            
            LOGGER.fine("Logged activity: " + activityType + " - " + description);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to log activity: " + description, e);
            // Don't throw - logging failures shouldn't break application flow
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanupOldActivities(int olderThanDays) {
        try {
            int deleted = activityDAO.deleteOlderThan(olderThanDays);
            LOGGER.info("Cleaned up " + deleted + " old activities (older than " + olderThanDays + " days)");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to cleanup old activities", e);
        }
    }
    
    // ==================== EVENT LISTENERS ====================
    
    /*
     * Design Pattern: Observer Pattern
     * 
     * Each @Subscribe method is an event listener.
     * When an event is published to the EventBus, the corresponding method is called.
     * 
     * Benefits:
     * - Loose coupling (services don't directly call this service)
     * - Easy to add new listeners without modifying existing code
     * - Centralized activity logging logic
     */
    
    /**
     * Listener for LessonCreatedEvent.
     * Logs when a new lesson is created.
     * 
     * @param event The lesson created event
     */
    @Subscribe
    public void onLessonCreated(LessonCreatedEvent event) {
        logActivity(
            "LESSON_CREATED",
            "Lesson " + event.getLessonId() + " created on " + event.getLessonDate(),
            "LESSON",
            event.getLessonId(),
            event.getCreatedBy()
        );
    }
    
    /**
     * Listener for StudentRegisteredEvent.
     * Logs when a new student is registered.
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
     * Listener for StudentArchivedEvent.
     * Logs when a student is archived.
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
     * Listener for StudentRestoredEvent.
     * Logs when an archived student is restored.
     * 
     * @param event The student restored event
     */
    @Subscribe
    public void onStudentRestored(StudentRestoredEvent event) {
        logActivity(
            "STUDENT_RESTORED",
            "Student restored from archive",
            "STUDENT",
            event.getStudentId(),
            event.getRestoredBy()
        );
    }
    
    /**
     * Listener for WarningGeneratedEvent.
     * Logs when a warning is generated for a student.
     * 
     * @param event The warning generated event
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        logActivity(
            "WARNING_GENERATED",
            "Warning generated: " + event.getWarningType() + " - " + event.getReason(),
            "WARNING",
            event.getWarningId(),
            null // System-generated
        );
    }
    
    /**
     * Listener for WarningResolvedEvent.
     * Logs when a warning is marked as resolved.
     * 
     * @param event The warning resolved event
     */
    @Subscribe
    public void onWarningResolved(WarningResolvedEvent event) {
        logActivity(
            "WARNING_RESOLVED",
            "Warning resolved: " + event.getResolvedReason(),
            "WARNING",
            event.getWarningId(),
            null // Could add resolvedBy if available in event
        );
    }
    
    /**
     * Listener for MissionAssignedEvent.
     * Logs when a mission is assigned to a user.
     * 
     * @param event The mission assigned event
     */
    @Subscribe
    public void onMissionAssigned(MissionAssignedEvent event) {
        Mission mission = missionDAO.findById(event.getMissionId());
        logActivity(
            "MISSION_ASSIGNED",
            mission.getMissionType() + " mission assigned for Lesson " + event.getLessonId(),
            "MISSION",
            event.getMissionId(),
            event.getAssignedBy()
        );
    }
    
    /**
     * Listener for MissionCompletedEvent.
     * Logs when a mission is marked as complete.
     * 
     * @param event The mission completed event
     */
    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent event) {
        logActivity(
            "MISSION_COMPLETED",
            event.getType() + " mission completed for Lesson " + event.getLessonId(),
            "MISSION",
            event.getMissionId(),
            event.getCompletedBy()
        );
    }
    
    /**
     * Listener for MissionReassignedEvent.
     * Logs when a mission is reassigned to a different user.
     * 
     * @param event The mission reassigned event
     */
    @Subscribe
    public void onMissionReassigned(MissionReassignedEvent event) {
        Mission mission = missionDAO.findById(event.getMissionId());
        logActivity(
            "MISSION_REASSIGNED",
            "Mission reassigned for Lesson " + mission.getLessonId(),
            "MISSION",
            event.getMissionId(),
            event.getReassignedBy()
        );
    }
    
    /**
     * Listener for QuizCreatedEvent.
     * Logs when a quiz is created for a lesson.
     * 
     * @param event The quiz created event
     */
    @Subscribe
    public void onQuizCreated(QuizCreatedEvent event) {
        Quiz quiz = quizDAO.findById(event.getQuizId());
        logActivity(
            "QUIZ_CREATED",
            "Quiz created for Lesson " + event.getLessonId(),
            "QUIZ",
            event.getQuizId(),
            quiz.getCreatedBy()
        );
    }
    
    /**
     * Listener for QuizGradingCompletedEvent.
     * Logs when quiz grading is completed for a lesson.
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
    
    /**
     * Listener for AttendanceBatchCompletedEvent.
     * Logs when attendance recording is completed for a lesson.
     * 
     * @param event The attendance batch completed event
     */
    @Subscribe
    public void onAttendanceBatchCompleted(AttendanceBatchCompletedEvent event) {
        logActivity(
            "ATTENDANCE_RECORDED",
            "Attendance recorded for Lesson " + event.getLessonId() + 
            " (" + event.getPresentCount() + " students)",
            "LESSON",
            event.getLessonId(),
            event.getCompletedBy()
        );
    }
    
    /**
     * Listener for AttendanceMarkedEvent.
     * Logs individual attendance marking (if needed for detailed tracking).
     * 
     * @param event The attendance marked event
     */
    @Subscribe
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        // Optional: Log individual attendance records
        // Usually batch completion is sufficient, so this might be too verbose
        // Uncomment if you want detailed logs:
        /*
        logActivity(
            "ATTENDANCE_MARKED",
            "Attendance marked for student: " + event.getStatus(),
            "ATTENDANCE",
            event.getAttendanceId(),
            event.getEnteredBy()
        );
        */
    }
    
    /**
     * Listener for HomeworkBatchCompletedEvent.
     * Logs when homework recording is completed for a lesson.
     * 
     * @param event The homework batch completed event
     */
    @Subscribe
    public void onHomeworkBatchCompleted(HomeworkBatchCompletedEvent event) {
        logActivity(
            "HOMEWORK_RECORDED",
            "Homework recorded for Lesson " + event.getLessonId() + 
            " (" + (event.getDoneCount() + event.getPartialCount()) + " students)",
            "LESSON",
            event.getLessonId(),
            event.getCompletedBy()
        );
    }
    
    /**
     * Listener for HomeworkRecordedEvent.
     * Logs individual homework recording (if needed for detailed tracking).
     * 
     * @param event The homework recorded event
     */
    @Subscribe
    public void onHomeworkRecorded(HomeworkRecordedEvent event) {
        // Optional: Log individual homework records
        // Usually batch completion is sufficient
        // Uncomment if needed:
        /*
        logActivity(
            "HOMEWORK_RECORDED",
            "Homework recorded: " + event.getStatus(),
            "HOMEWORK",
            event.getHomeworkId(),
            event.getEnteredBy()
        );
        */
    }
    
    /**
     * Listener for UpdateRequestSubmittedEvent.
     * Logs when an assistant submits an update request.
     * 
     * @param event The update request submitted event
     */
    @Subscribe
    public void onUpdateRequestSubmitted(UpdateRequestSubmittedEvent event) {
        logActivity(
            "UPDATE_REQUEST_SUBMITTED",
            "Update request submitted: " + event.getRequestType(),
            "UPDATE_REQUEST",
            event.getRequestId(),
            event.getRequestedBy()
        );
    }
    
    /**
     * Listener for UpdateRequestApprovedEvent.
     * Logs when an admin approves an update request.
     * 
     * @param event The update request approved event
     */
    @Subscribe
    public void onUpdateRequestApproved(UpdateRequestApprovedEvent event) {
        logActivity(
            "UPDATE_REQUEST_APPROVED",
            "Update request approved",
            "UPDATE_REQUEST",
            event.getRequestId(),
            event.getApprovedBy()
        );
    }
    
    /**
     * Listener for UpdateRequestRejectedEvent.
     * Logs when an admin rejects an update request.
     * 
     * @param event The update request rejected event
     */
    @Subscribe
    public void onUpdateRequestRejected(UpdateRequestRejectedEvent event) {
        logActivity(
            "UPDATE_REQUEST_REJECTED",
            "Update request rejected: " + event.getReason(),
            "UPDATE_REQUEST",
            event.getRequestId(),
            event.getRejectedBy()
        );
    }
    
    /**
     * Listener for MonthlyReportGeneratedEvent.
     * Logs when a monthly report is generated.
     * 
     * @param event The monthly report generated event
     */
    @Subscribe
    public void onMonthlyReportGenerated(MonthlyReportGeneratedEvent event) {
        logActivity(
            "REPORT_GENERATED",
            "Monthly report generated for " + event.getMonthGroup(),
            "REPORT",
            event.getReportId(),
            event.getGeneratedBy()
        );
    }
    
    /**
     * Listener for MonthlyReportDeletedEvent.
     * Logs when a monthly report is deleted.
     * 
     * @param event The monthly report deleted event
     */
    @Subscribe
    public void onMonthlyReportDeleted(MonthlyReportDeletedEvent event) {
        logActivity(
            "REPORT_DELETED",
            "Monthly report deleted for " + event.getMonthGroup(),
            "REPORT",
            event.getReportId(),
            event.getDeletedBy()
        );
    }
    
    /**
     * Listener for BehavioralIncidentAddedEvent.
     * Logs when a behavioral incident is recorded for a student.
     * 
     * @param event The behavioral incident added event
     */
    @Subscribe
    public void onBehavioralIncidentAdded(BehavioralIncidentAddedEvent event) {
        logActivity(
            "BEHAVIORAL_INCIDENT",
            "Behavioral incident recorded: " + event.getIncidentType(),
            "BEHAVIORAL_INCIDENT",
            event.getIncidentId(),
            event.getCreatedBy()
        );
    }
    
    /**
     * Listener for TargetCreatedEvent.
     * Logs when a new achievement target is created.
     * 
     * @param event The target created event
     */
    @Subscribe
    public void onTargetCreated(TargetCreatedEvent event) {
        logActivity(
            "TARGET_CREATED",
            "New achievement target created",
            "TARGET",
            event.getTargetId(),
            event.getStudentId()
        );
    }
    
    /**
     * Listener for TargetAchievedEvent.
     * Logs when a student achieves a target.
     * 
     * @param event The target achieved event
     */
    @Subscribe
    public void onTargetAchieved(TargetAchievedEvent event) {
        logActivity(
            "TARGET_ACHIEVED",
            "Student achieved target " + event.getTargetId(),
            "TARGET",
            event.getTargetId(),
            event.getStudentId()
        );
    }
    
    /**
     * Listener for Fasee7RankingsChangedEvent.
     * Logs when Fasee7 point rankings change significantly.
     * 
     * @param event The rankings changed event
     */
    @Subscribe
    public void onFasee7RankingsChanged(Fasee7RankingsChangedEvent event) {
        logActivity(
            "RANKINGS_UPDATED",
            "Fasee7 rankings updated",
            "FASEE7",
            null,
            null // System-generated
        );
    }
    
    // ==================== HELPER NOTES ====================
    
    /*
     * NOTE: Not all events need activity logs!
     * 
     * Events we DON'T log (too granular/internal):
     * - Fasee7PointsUpdatedEvent (happens too frequently)
     * - ConsecutivityUpdatedEvent (internal tracking)
     * - PerformanceIndicatorCalculatedEvent (internal calculation)
     * - NotificationSentEvent (would create duplicate logs)
     * 
     * If you need to log these, add @Subscribe methods following the pattern above.
     */
}