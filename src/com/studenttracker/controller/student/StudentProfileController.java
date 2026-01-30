package com.studenttracker.controller.student;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.controller.BaseController;
import com.studenttracker.model.*;
import com.studenttracker.service.*;
import com.studenttracker.service.event.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Student Profile screen.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Display comprehensive student profile with info cards</li>
 *   <li>Show attendance summary with consecutive tracking</li>
 *   <li>Display active warnings with visual indicators</li>
 *   <li>Show Fasee7 ranking with points breakdown</li>
 *   <li>Handle role-based edit functionality (Admin vs Assistant)</li>
 *   <li>Real-time updates via EventBus subscription</li>
 *   <li>Navigate between tabs (Lessons, Incidents)</li>
 * </ul>
 * 
 * <p><b>Design Patterns:</b></p>
 * <ul>
 *   <li>Template Method - extends BaseController</li>
 *   <li>Observer - EventBus subscriber for real-time updates</li>
 *   <li>Service Locator - accesses services via ServiceLocator</li>
 *   <li>MVC - separates view (FXML) from business logic</li>
 * </ul>
 * 
 * <p><b>EventBus Subscriptions:</b></p>
 * <ul>
 *   <li>AttendanceMarkedEvent - refreshes attendance summary</li>
 *   <li>WarningGeneratedEvent - refreshes warnings list</li>
 *   <li>WarningResolvedEvent - refreshes warnings list</li>
 *   <li>Fasee7PointsUpdatedEvent - refreshes ranking</li>
 *   <li>Fasee7RankingsChangedEvent - refreshes ranking</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-30
 */
public class StudentProfileController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentProfileController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    // ==================== FXML COMPONENTS ====================
    
    // Header
    @FXML private Label studentNameLabel;
    @FXML private Button editButton;
    
    // Personal Info Card
    @FXML private Label studentIdLabel;
    @FXML private Label phoneLabel;
    @FXML private Label parentPhoneLabel;
    @FXML private Label statusLabel;
    @FXML private Label registrationDateLabel;
    
    // Attendance Summary Card
    @FXML private Label totalLessonsLabel;
    @FXML private Label attendedLabel;
    @FXML private Label absentLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label consecutiveAbsencesLabel;
    
    // Warnings Card
    @FXML private Label warningCountBadge;
    @FXML private ListView<WarningRow> warningsListView;
    
    // Fasee7 Ranking Card
    @FXML private Label rankLabel;
    @FXML private Label totalPointsLabel;
    @FXML private Label quizPointsLabel;
    @FXML private Label attendancePointsLabel;
    @FXML private Label homeworkPointsLabel;
    @FXML private Label targetPointsLabel;
    
    // Tabs
    @FXML private TabPane tabPane;
    
    // ==================== SERVICES ====================
    
    private StudentService studentService;
    private AttendanceService attendanceService;
    private WarningService warningService;
    private Fasee7TableService fasee7Service;
    private ConsecutivityTrackingService consecutivityService;
    private EventBusService eventBus;
    
    // ==================== DATA ====================
    
    private Integer currentStudentId;
    private Student currentStudent;
    private ObservableList<WarningRow> warningsList;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * No-arg constructor required for FXML instantiation.
     * Initializes services via ServiceLocator.
     */
    public StudentProfileController() {
        super();
        LOGGER.fine("StudentProfileController constructor called");
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Initialize method - called after FXML injection.
     * Sets up services, configures UI, registers EventBus, and loads profile data.
     */
    @Override
    public void initialize() {
        super.initialize();
        
        LOGGER.info("StudentProfileController initializing");
        
        try {
            // Initialize services
            this.studentService = serviceLocator.getStudentService();
            this.attendanceService = serviceLocator.getAttendanceService();
            this.warningService = serviceLocator.getWarningService();
            this.fasee7Service = serviceLocator.getFasee7TableService();
            this.consecutivityService = serviceLocator.getConsecutivityTrackingService();
            this.eventBus = EventBusService.getInstance();
            
            // Initialize data collections
            this.warningsList = FXCollections.observableArrayList();
            
            // Register for EventBus updates
            eventBus.register(this);
            
            // Configure UI components
            configureEditButton();
            configureWarningsList();
            
            // Load student profile
            // Note: currentStudentId should be set by navigation logic before initialize()
            // For now, we'll handle null case gracefully
            if (currentStudentId != null) {
                loadStudentProfile(currentStudentId);
            } else {
                LOGGER.warning("No student ID provided to profile screen");
                showError("No student selected. Returning to student list.");
                handleBack();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize StudentProfileController", e);
            showError("Failed to load student profile. Please try again.");
        }
    }
    
    /**
     * Cleanup method - called before navigation away from this screen.
     * Unregisters from EventBus to prevent memory leaks.
     */
    @Override
    public void cleanup() {
        LOGGER.fine("Cleaning up StudentProfileController");
        
        if (eventBus != null) {
            eventBus.unregister(this);
        }
        
        super.cleanup();
    }
    
    // ==================== SETTER FOR NAVIGATION ====================
    
    /**
     * Sets the student ID to display.
     * Should be called by navigation logic before the screen is displayed.
     * 
     * @param studentId the ID of the student to display
     */
    public void setStudentId(Integer studentId) {
        this.currentStudentId = studentId;
        LOGGER.info("Student ID set to: " + studentId);
    }
    
    // ==================== UI CONFIGURATION ====================
    
    /**
     * Configures the Edit button based on user role.
     * Admin: Shows "✏ Edit" button for direct editing
     * Assistant: Shows "📝 Request Update" button for submitting update requests
     */
    private void configureEditButton() {
        if (editButton == null) {
            LOGGER.warning("Edit button not injected from FXML");
            return;
        }
        
        if (isAdmin()) {
            editButton.setVisible(true);
            editButton.setManaged(true);
            editButton.setText("✏ Edit");
            LOGGER.fine("Edit button configured for Admin");
        } else if (isAssistant()) {
            editButton.setVisible(true);
            editButton.setManaged(true);
            editButton.setText("📝 Request Update");
            LOGGER.fine("Edit button configured for Assistant");
        } else {
            editButton.setVisible(false);
            editButton.setManaged(false);
            LOGGER.fine("Edit button hidden (no edit permission)");
        }
    }
    
    /**
     * Configures the warnings ListView with custom cell factory.
     * Each warning is displayed with an icon and formatted text.
     */
    private void configureWarningsList() {
        if (warningsListView == null) {
            LOGGER.warning("Warnings ListView not injected from FXML");
            return;
        }
        
        warningsListView.setItems(warningsList);
        
        // Custom cell factory for warning display
        warningsListView.setCellFactory(listView -> new ListCell<WarningRow>() {
            @Override
            protected void updateItem(WarningRow item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.getDisplayText());
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });
        
        LOGGER.fine("Warnings ListView configured");
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Loads complete student profile from multiple services.
     * Coordinates data from StudentService, AttendanceService, WarningService,
     * Fasee7TableService, and ConsecutivityTrackingService.
     * 
     * @param studentId the ID of the student to load
     */
    private void loadStudentProfile(Integer studentId) {
        try {
            LOGGER.info("Loading profile for student ID: " + studentId);
            
            // Load student data
            currentStudent = studentService.getStudentById(studentId);
            
            if (currentStudent == null) {
                LOGGER.warning("Student not found: " + studentId);
                showError("Student not found. Returning to student list.");
                handleBack();
                return;
            }
            
            // Populate all sections
            populatePersonalInfo(currentStudent);
            populateAttendanceSummary(studentId);
            populateWarnings(studentId);
            populateFasee7Ranking(studentId);
            
            LOGGER.info("Profile loaded successfully for: " + currentStudent.getFullName());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load student profile", e);
            showError("Failed to load student profile: " + e.getMessage());
        }
    }
    
    /**
     * Populates the personal info card with student data.
     * 
     * @param student the student object containing personal information
     */
    private void populatePersonalInfo(Student student) {
        if (student == null) return;
        
        try {
            studentNameLabel.setText("Student: " + student.getFullName());
            studentIdLabel.setText(String.valueOf(student.getStudentId()));
            phoneLabel.setText(student.getPhoneNumber() != null ? student.getPhoneNumber() : "N/A");
            parentPhoneLabel.setText(student.getParentPhoneNumber() != null ? student.getParentPhoneNumber() : "N/A");
            
            // Status with color coding
            String status = student.getStatus().name();
            statusLabel.setText(status);
            if (student.getStatus() == Student.StudentStatus.ACTIVE) {
                statusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
            
            // Registration date formatting
            if (student.getRegistrationDate() != null) {
                registrationDateLabel.setText(student.getRegistrationDate().format(DATE_FORMATTER));
            } else {
                registrationDateLabel.setText("N/A");
            }
            
            LOGGER.fine("Personal info populated");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error populating personal info", e);
        }
    }
    
    /**
     * Populates the attendance summary card with calculated statistics.
     * Queries AttendanceService for records and ConsecutivityTrackingService for consecutive count.
     * 
     * @param studentId the student ID
     */
    private void populateAttendanceSummary(Integer studentId) {
        try {
            // Get all attendance records for student
            List<Attendance> attendanceRecords = attendanceService.getAttendanceByStudent(studentId);
            
            // Calculate statistics
            int totalLessons = attendanceRecords.size();
            int attended = (int) attendanceRecords.stream()
                    .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT)
                    .count();
            int absent = totalLessons - attended;
            double attendanceRate = totalLessons > 0 ? (attended * 100.0 / totalLessons) : 0.0;
            
            // Get consecutive absence count
            int consecutiveAbsences = consecutivityService.getConsecutiveAbsenceCount(studentId);
            
            // Update UI
            totalLessonsLabel.setText(String.valueOf(totalLessons));
            attendedLabel.setText(String.valueOf(attended));
            absentLabel.setText(String.valueOf(absent));
            attendanceRateLabel.setText(String.format("%.2f%%", attendanceRate));
            consecutiveAbsencesLabel.setText(String.valueOf(consecutiveAbsences));
            
            // Color coding for consecutive absences
            if (consecutiveAbsences >= 2) {
                consecutiveAbsencesLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            } else {
                consecutiveAbsencesLabel.setStyle("-fx-text-fill: #10b981;");
            }
            
            LOGGER.fine("Attendance summary populated: " + totalLessons + " lessons, " + attended + " attended");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error populating attendance summary", e);
            // Set default values on error
            totalLessonsLabel.setText("0");
            attendedLabel.setText("0");
            absentLabel.setText("0");
            attendanceRateLabel.setText("0.00%");
            consecutiveAbsencesLabel.setText("0");
        }
    }
    
    /**
     * Populates the warnings card with active warnings for the student.
     * Each warning is wrapped in a WarningRow for display with icon and formatted text.
     * 
     * @param studentId the student ID
     */
    private void populateWarnings(Integer studentId) {
        try {
            // Get active warnings
            List<Warning> activeWarnings = warningService.getActiveWarningsByStudent(studentId);
            
            // Update badge count
            int warningCount = activeWarnings.size();
            warningCountBadge.setText(String.valueOf(warningCount));
            
            if (warningCount > 0) {
                warningCountBadge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-padding: 2 8 2 8; -fx-font-weight: bold;");
            } else {
                warningCountBadge.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-padding: 2 8 2 8; -fx-font-weight: bold;");
            }
            
            // Clear and populate warnings list
            warningsList.clear();
            for (Warning warning : activeWarnings) {
                warningsList.add(new WarningRow(warning));
            }
            
            LOGGER.fine("Warnings populated: " + warningCount + " active warnings");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error populating warnings", e);
            warningCountBadge.setText("0");
            warningsList.clear();
        }
    }
    
    /**
     * Populates the Fasee7 ranking card with points breakdown and rank.
     * Queries Fasee7TableService for points and rank calculation.
     * 
     * @param studentId the student ID
     */
    private void populateFasee7Ranking(Integer studentId) {
        try {
            // Get points
            Fasee7Points points = fasee7Service.getStudentPoints(studentId);
            
            if (points == null) {
                LOGGER.warning("No Fasee7 points found for student: " + studentId);
                setDefaultRankingValues();
                return;
            }
            
            // Get rank
            int rank = fasee7Service.getStudentRank(studentId);
            
            // Update UI
            rankLabel.setText(formatRank(rank));
            totalPointsLabel.setText(String.format("%.0f", points.getTotalPoints()));
            quizPointsLabel.setText(String.format("%.0f", points.getQuizPoints()));
            attendancePointsLabel.setText(String.valueOf(points.getAttendancePoints()));
            homeworkPointsLabel.setText(String.valueOf(points.getHomeworkPoints()));
            targetPointsLabel.setText(String.valueOf(points.getTargetPoints()));
            
            LOGGER.fine("Fasee7 ranking populated: Rank " + rank + ", Total " + points.getTotalPoints());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error populating Fasee7 ranking", e);
            setDefaultRankingValues();
        }
    }
    
    /**
     * Sets default values for ranking card when data is unavailable.
     */
    private void setDefaultRankingValues() {
        rankLabel.setText("N/A");
        totalPointsLabel.setText("0");
        quizPointsLabel.setText("0");
        attendancePointsLabel.setText("0");
        homeworkPointsLabel.setText("0");
        targetPointsLabel.setText("0");
    }
    
    /**
     * Formats a rank number with appropriate suffix (1st, 2nd, 3rd, 4th, etc.).
     * 
     * @param rank the rank number
     * @return formatted rank string (e.g., "1st", "2nd", "3rd", "8th")
     */
    private String formatRank(int rank) {
        if (rank < 0) {
            return "N/A";
        }
        
        // Handle special cases: 11th, 12th, 13th
        if (rank % 100 >= 11 && rank % 100 <= 13) {
            return rank + "th";
        }
        
        // Handle normal cases
        switch (rank % 10) {
            case 1: return rank + "st";
            case 2: return rank + "nd";
            case 3: return rank + "rd";
            default: return rank + "th";
        }
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handles Back button click.
     * Performs cleanup and navigates back to student list.
     */
    @FXML
    private void handleBack() {
        try {
            LOGGER.info("Back button clicked, returning to student list");
            navigateTo("/com/studenttracker/view/fxml/student/StudentList.fxml");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate back", e);
            showError("Navigation error. Please try again.");
        }
    }
    
    /**
     * Handles Edit button click.
     * Admin: Opens edit dialog for direct editing
     * Assistant: Opens update request dialog for submitting requests
     */
    @FXML
    private void handleEdit() {
        if (currentStudent == null) {
            showError("No student loaded.");
            return;
        }
        
        try {
            if (isAdmin()) {
                handleAdminEdit();
            } else if (isAssistant()) {
                handleAssistantUpdateRequest();
            } else {
                showError("You don't have permission to edit students.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling edit action", e);
            showError("Failed to open edit dialog: " + e.getMessage());
        }
    }
    
    /**
     * Handles admin direct edit flow.
     * Opens edit dialog where admin can make immediate changes.
     */
    private void handleAdminEdit() {
        LOGGER.info("Admin editing student: " + currentStudentId);
        
        // TODO: Implement in future phase
        // sceneManager.showDialog("/com/studenttracker/view/fxml/student/StudentEdit.fxml", "Edit Student");
        // Pass currentStudentId to dialog controller
        
        showInfo("Admin Edit", 
                "Student edit functionality will be implemented in the next phase.\n\n" +
                "For now, you can edit students through the student list screen.");
    }
    
    /**
     * Handles assistant update request flow.
     * Opens request dialog where assistant can submit changes for admin approval.
     */
    private void handleAssistantUpdateRequest() {
        LOGGER.info("Assistant submitting update request for student: " + currentStudentId);
        
        // TODO: Implement in future phase
        // sceneManager.showDialog("/com/studenttracker/view/fxml/student/UpdateRequest.fxml", "Submit Update Request");
        // Pass currentStudentId to dialog controller
        
        showInfo("Submit Update Request", 
                "Update request functionality will be implemented in the next phase.\n\n" +
                "Your request will be sent to an admin for approval.");
    }
    
    // ==================== EVENTBUS SUBSCRIBERS ====================
    
    /**
     * Handles AttendanceMarkedEvent from EventBus.
     * Refreshes attendance summary if the event is for the current student.
     * 
     * @param event the attendance marked event
     */
    @Subscribe
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        if (event == null || event.getStudentId() == null) return;
        
        // Only refresh if event is for current student
        if (event.getStudentId().equals(currentStudentId)) {
            Platform.runLater(() -> {
                LOGGER.fine("Attendance marked event received for current student, refreshing");
                populateAttendanceSummary(currentStudentId);
            });
        }
    }
    
    /**
     * Handles WarningGeneratedEvent from EventBus.
     * Refreshes warnings list if the event is for the current student.
     * 
     * @param event the warning generated event
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        if (event == null || event.getStudentId() == null) return;
        
        // Only refresh if event is for current student
        if (event.getStudentId().equals(currentStudentId)) {
            Platform.runLater(() -> {
                LOGGER.fine("Warning generated event received for current student, refreshing");
                populateWarnings(currentStudentId);
            });
        }
    }
    
    /**
     * Handles WarningResolvedEvent from EventBus.
     * Refreshes warnings list.
     * Note: This event doesn't contain studentId, so we always refresh.
     * 
     * @param event the warning resolved event
     */
    @Subscribe
    public void onWarningResolved(WarningResolvedEvent event) {
        if (event == null) return;
        
        Platform.runLater(() -> {
            LOGGER.fine("Warning resolved event received, refreshing warnings");
            if (currentStudentId != null) {
                populateWarnings(currentStudentId);
            }
        });
    }
    
    /**
     * Handles Fasee7PointsUpdatedEvent from EventBus.
     * Refreshes Fasee7 ranking if the event is for the current student.
     * 
     * @param event the Fasee7 points updated event
     */
    @Subscribe
    public void onFasee7PointsUpdated(Fasee7PointsUpdatedEvent event) {
        if (event == null || event.getStudentId() == null) return;
        
        // Only refresh if event is for current student
        if (event.getStudentId().equals(currentStudentId)) {
            Platform.runLater(() -> {
                LOGGER.fine("Fasee7 points updated event received for current student, refreshing");
                populateFasee7Ranking(currentStudentId);
            });
        }
    }
    
    /**
     * Handles Fasee7RankingsChangedEvent from EventBus.
     * Refreshes Fasee7 ranking as rankings may have shifted globally.
     * This is a global event - any student's rank may have changed.
     * 
     * @param event the Fasee7 rankings changed event
     */
    @Subscribe
    public void onFasee7RankingsChanged(Fasee7RankingsChangedEvent event) {
        if (event == null) return;
        
        Platform.runLater(() -> {
            LOGGER.fine("Fasee7 rankings changed event received, refreshing ranking");
            if (currentStudentId != null) {
                populateFasee7Ranking(currentStudentId);
            }
        });
    }
    
    // ==================== INNER CLASS: WarningRow ====================
    
    /**
     * Wrapper class for displaying Warning objects in ListView.
     * Provides formatted display text with icon based on warning type.
     * 
     * <p><b>Display Format:</b></p>
     * <ul>
     *   <li>CONSECUTIVE_ABSENCE: 🚫 [reason] - [date]</li>
     *   <li>ARCHIVED: 📦 [reason] - [date]</li>
     *   <li>BEHAVIORAL: ⚠️ [reason] - [date]</li>
     * </ul>
     */
    public static class WarningRow {
        private final Warning warning;
        private final String typeIcon;
        private final String displayText;
        
        /**
         * Creates a WarningRow from a Warning object.
         * 
         * @param warning the warning to wrap
         */
        public WarningRow(Warning warning) {
            this.warning = warning;
            this.typeIcon = getTypeIcon(warning.getWarningType());
            this.displayText = formatDisplayText();
        }
        
        /**
         * Gets the icon for a warning type.
         * 
         * @param type the warning type
         * @return emoji icon representing the type
         */
        private String getTypeIcon(Warning.WarningType type) {
            switch (type) {
                case CONSECUTIVE_ABSENCE:
                    return "🚫";
                case ARCHIVED:
                    return "📦";
                case BEHAVIORAL:
                    return "⚠️";
                default:
                    return "❗";
            }
        }
        
        /**
         * Formats the display text for the warning.
         * 
         * @return formatted string with icon, reason, and date
         */
        private String formatDisplayText() {
            StringBuilder sb = new StringBuilder();
            
            // Icon
            sb.append(typeIcon).append(" ");
            
            // Reason
            String reason = warning.getWarningReason();
            if (reason != null && !reason.isEmpty()) {
                sb.append(reason);
            } else {
                sb.append(warning.getWarningType().toString().replace("_", " "));
            }
            
            // Date
            if (warning.getCreatedAt() != null) {
                sb.append(" - ");
                sb.append(warning.getCreatedAt().format(DATE_FORMATTER));
            }
            
            return sb.toString();
        }
        
        /**
         * Gets the warning object.
         * 
         * @return the wrapped warning
         */
        public Warning getWarning() {
            return warning;
        }
        
        /**
         * Gets the type icon.
         * 
         * @return emoji icon for the warning type
         */
        public String getTypeIcon() {
            return typeIcon;
        }
        
        /**
         * Gets the formatted display text.
         * 
         * @return formatted text for display
         */
        public String getDisplayText() {
            return displayText;
        }
    }
}