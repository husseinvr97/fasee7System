package com.studenttracker.controller.layout;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.controller.BaseController;
import com.studenttracker.model.RecentActivity;
import com.studenttracker.service.*;
import com.studenttracker.service.event.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Dashboard screen.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Load and display key system metrics</li>
 *   <li>Display recent activities</li>
 *   <li>Listen to events for real-time updates</li>
 *   <li>Refresh data on demand</li>
 *   <li>Hide admin-only metrics for assistants</li>
 * </ul>
 * 
 * <p><b>Metrics Displayed:</b></p>
 * <ul>
 *   <li>Active Students</li>
 *   <li>Archived Students</li>
 *   <li>Total Lessons</li>
 *   <li>Pending Missions</li>
 *   <li>Active Warnings</li>
 *   <li>Pending Requests (Admin only)</li>
 * </ul>
 * 
 * <p><b>Real-Time Updates:</b></p>
 * <p>Subscribes to EventBus to receive notifications when data changes,
 * automatically refreshing affected metrics.</p>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class DashboardController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private Button refreshButton;
    
    // Metric Labels
    @FXML private Label activeStudentsLabel;
    @FXML private Label archivedStudentsLabel;
    @FXML private Label totalLessonsLabel;
    @FXML private Label pendingMissionsLabel;
    @FXML private Label activeWarningsLabel;
    @FXML private Label pendingRequestsLabel;
    
    // Admin-only card
    @FXML private VBox pendingRequestsCard;
    
    // Recent Activities
    @FXML private ListView<String> activitiesListView;
    
    // ==================== SERVICES ====================
    
    private StudentService studentService;
    private LessonService lessonService;
    private MissionService missionService;
    private WarningService warningService;
    private UpdateRequestOrchestratorService updateRequestService;
    private RecentActivityService activityService;
    private EventBusService eventBus;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * No-arg constructor required for FXML instantiation.
     */
    public DashboardController() {
        super();
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Initialize method - called after FXML injection.
     * Initializes services, configures UI, and loads data.
     */
    @Override
    public void initialize() {
        super.initialize(); // Initialize utilities
        
        LOGGER.info("Dashboard initialized");
        
        // Initialize services
        this.studentService = serviceLocator.getStudentService();
        this.lessonService = serviceLocator.getLessonService();
        this.missionService = serviceLocator.getMissionService();
        this.warningService = serviceLocator.getWarningService();
        this.updateRequestService = serviceLocator.getUpdateRequestService();
        this.activityService = serviceLocator.getRecentActivityService();
        this.eventBus = EventBusService.getInstance();
        
        // Register for event updates
        eventBus.register(this);
        
        // Configure UI for user role
        configureForUserRole();
        
        // Load data
        loadAllMetrics();
        loadRecentActivities();
    }
    
    // ==================== UI CONFIGURATION ====================
    
    /**
     * Configures UI based on user role.
     * Hides admin-only cards for assistants.
     */
    private void configureForUserRole() {
        if (isAssistant()) {
            // Hide "Pending Requests" card for assistants
            pendingRequestsCard.setVisible(false);
            pendingRequestsCard.setManaged(false);
            LOGGER.fine("Hidden admin-only metrics for assistant");
        }
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Loads all metrics from services.
     * Called on initialization and manual refresh.
     */
    private void loadAllMetrics() {
        try {
            LOGGER.fine("Loading dashboard metrics...");
            
            // Load student counts
            int activeStudents = studentService.getActiveStudentCount();
            int archivedStudents = studentService.getArchivedStudentCount();
            
            // Load lesson count
            int totalLessons = lessonService.getTotalLessonCount();
            
            // Load mission count
            int pendingMissions = missionService.getPendingMissionCount();
            
            // Load warning count
            int activeWarnings = warningService.getActiveWarningCount();
            
            // Load request count (admin only)
            int pendingRequests = 0;
            if (isAdmin()) {
                pendingRequests = updateRequestService.getPendingRequestCount();
            }
            
            // Update UI (must be on JavaFX thread)
            final int finalRequests = pendingRequests;
            Platform.runLater(() -> {
                activeStudentsLabel.setText(String.valueOf(activeStudents));
                archivedStudentsLabel.setText(String.valueOf(archivedStudents));
                totalLessonsLabel.setText(String.valueOf(totalLessons));
                pendingMissionsLabel.setText(String.valueOf(pendingMissions));
                activeWarningsLabel.setText(String.valueOf(activeWarnings));
                
                if (isAdmin()) {
                    pendingRequestsLabel.setText(String.valueOf(finalRequests));
                }
            });
            
            LOGGER.fine("Dashboard metrics loaded successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load dashboard metrics", e);
            Platform.runLater(() -> showError("Failed to load dashboard data. Please try refreshing."));
        }
    }
    
    /**
     * Loads recent 20 activities from database.
     */
    private void loadRecentActivities() {
        try {
            LOGGER.fine("Loading recent activities...");
            
            // Get recent 20 activities
            List<RecentActivity> activities = activityService.getRecentActivities(20);
            
            // Update UI (must be on JavaFX thread)
            Platform.runLater(() -> {
                activitiesListView.getItems().clear();
                
                if (activities.isEmpty()) {
                    activitiesListView.getItems().add("No recent activity");
                } else {
                    for (RecentActivity activity : activities) {
                        String displayText = formatActivityForDisplay(activity);
                        activitiesListView.getItems().add(displayText);
                    }
                }
            });
            
            LOGGER.fine("Recent activities loaded successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load recent activities", e);
            Platform.runLater(() -> activitiesListView.getItems().add("Error loading activities"));
        }
    }
    
    /**
     * Formats activity for display in ListView.
     * 
     * @param activity Activity to format
     * @return Formatted string for display
     */
    private String formatActivityForDisplay(RecentActivity activity) {
        // Format: "Description (Time)"
        return activity.getActivityDescription() + " (" + activity.getFormattedTime() + ")";
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handles refresh button click.
     * Reloads all metrics and activities.
     */
    @FXML
    private void handleRefresh() {
        LOGGER.info("Manual refresh requested");
        loadAllMetrics();
        loadRecentActivities();
    }
    
    // ==================== EVENT SUBSCRIBERS (Real-Time Updates) ====================
    
    /**
     * Handles StudentRegisteredEvent.
     * Updates active student count.
     */
    @Subscribe
    public void onStudentRegistered(StudentRegisteredEvent event) {
        Platform.runLater(() -> {
            int count = studentService.getActiveStudentCount();
            activeStudentsLabel.setText(String.valueOf(count));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles StudentArchivedEvent.
     * Updates both active and archived student counts.
     */
    @Subscribe
    public void onStudentArchived(StudentArchivedEvent event) {
        Platform.runLater(() -> {
            int active = studentService.getActiveStudentCount();
            int archived = studentService.getArchivedStudentCount();
            activeStudentsLabel.setText(String.valueOf(active));
            archivedStudentsLabel.setText(String.valueOf(archived));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles StudentRestoredEvent.
     * Updates both active and archived student counts.
     */
    @Subscribe
    public void onStudentRestored(StudentRestoredEvent event) {
        Platform.runLater(() -> {
            int active = studentService.getActiveStudentCount();
            int archived = studentService.getArchivedStudentCount();
            activeStudentsLabel.setText(String.valueOf(active));
            archivedStudentsLabel.setText(String.valueOf(archived));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles LessonCreatedEvent.
     * Updates total lesson count.
     */
    @Subscribe
    public void onLessonCreated(LessonCreatedEvent event) {
        Platform.runLater(() -> {
            int count = lessonService.getTotalLessonCount();
            totalLessonsLabel.setText(String.valueOf(count));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles MissionAssignedEvent.
     * Updates pending mission count.
     */
    @Subscribe
    public void onMissionAssigned(MissionAssignedEvent event) {
        Platform.runLater(() -> {
            int count = missionService.getPendingMissionCount();
            pendingMissionsLabel.setText(String.valueOf(count));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles MissionCompletedEvent.
     * Updates pending mission count.
     */
    @Subscribe
    public void onMissionCompleted(MissionCompletedEvent event) {
        Platform.runLater(() -> {
            int count = missionService.getPendingMissionCount();
            pendingMissionsLabel.setText(String.valueOf(count));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles WarningGeneratedEvent.
     * Updates active warning count.
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        Platform.runLater(() -> {
            int count = warningService.getActiveWarningCount();
            activeWarningsLabel.setText(String.valueOf(count));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles WarningResolvedEvent.
     * Updates active warning count.
     */
    @Subscribe
    public void onWarningResolved(WarningResolvedEvent event) {
        Platform.runLater(() -> {
            int count = warningService.getActiveWarningCount();
            activeWarningsLabel.setText(String.valueOf(count));
        });
        refreshRecentActivities();
    }
    
    /**
     * Handles UpdateRequestSubmittedEvent.
     * Updates pending request count (admin only).
     */
    @Subscribe
    public void onUpdateRequestSubmitted(UpdateRequestSubmittedEvent event) {
        if (isAdmin()) {
            Platform.runLater(() -> {
                int count = updateRequestService.getPendingRequestCount();
                pendingRequestsLabel.setText(String.valueOf(count));
            });
        }
        refreshRecentActivities();
    }
    
    /**
     * Handles UpdateRequestApprovedEvent.
     * Updates pending request count (admin only).
     */
    @Subscribe
    public void onUpdateRequestApproved(UpdateRequestApprovedEvent event) {
        if (isAdmin()) {
            Platform.runLater(() -> {
                int count = updateRequestService.getPendingRequestCount();
                pendingRequestsLabel.setText(String.valueOf(count));
            });
        }
        refreshRecentActivities();
    }
    
    /**
     * Handles UpdateRequestRejectedEvent.
     * Updates pending request count (admin only).
     */
    @Subscribe
    public void onUpdateRequestRejected(UpdateRequestRejectedEvent event) {
        if (isAdmin()) {
            Platform.runLater(() -> {
                int count = updateRequestService.getPendingRequestCount();
                pendingRequestsLabel.setText(String.valueOf(count));
            });
        }
        refreshRecentActivities();
    }
    
    /**
     * Generic event handler for other events that trigger activity logs.
     * Refreshes the recent activities list.
     */
    private void refreshRecentActivities() {
        // Small delay to allow activity to be logged first
        new Thread(() -> {
            try {
                Thread.sleep(100); // 100ms delay
                Platform.runLater(this::loadRecentActivities);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Cleanup method - called before navigation away from dashboard.
     * Unregisters from EventBus.
     */
    @Override
    public void cleanup() {
        LOGGER.fine("Cleaning up Dashboard");
        eventBus.unregister(this);
        super.cleanup();
    }
}