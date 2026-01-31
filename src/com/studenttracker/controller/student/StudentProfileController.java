package com.studenttracker.controller.student;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.controller.BaseController;
import com.studenttracker.controller.student.tabs.StudentIncidentsTabController;
import com.studenttracker.controller.student.tabs.StudentLessonsTabController;
import com.studenttracker.model.Fasee7Points;
import com.studenttracker.model.Student;
import com.studenttracker.model.User;
import com.studenttracker.model.Warning;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.service.AttendanceService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.Fasee7TableService;
import com.studenttracker.service.StudentService;
import com.studenttracker.service.WarningService;
import com.studenttracker.service.event.AttendanceMarkedEvent;
import com.studenttracker.service.event.Fasee7PointsUpdatedEvent;
import com.studenttracker.service.event.StudentArchivedEvent;
import com.studenttracker.service.event.WarningGeneratedEvent;
import com.studenttracker.util.AlertHelper;
import com.studenttracker.util.ServiceLocator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Student Profile screen.
 * Displays comprehensive student information across multiple info cards and tabs.
 * 
 * <p><b>Design Patterns Used:</b></p>
 * <ul>
 *   <li>Template Method - Extends BaseController lifecycle</li>
 *   <li>Observer - EventBus subscriptions for real-time updates</li>
 *   <li>Facade - Aggregates data from multiple services</li>
 *   <li>Lazy Initialization - Tab controllers loaded on demand</li>
 * </ul>
 * 
 * <p><b>SOLID Principles:</b></p>
 * <ul>
 *   <li>SRP - Single responsibility: manage student profile UI only</li>
 *   <li>DIP - Depends on service interfaces via ServiceLocator</li>
 *   <li>OCP - Open for extension via events, closed for modification</li>
 * </ul>
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>Personal info card (ID, phones, status, registration date)</li>
 *   <li>Attendance summary card (total, attended, absent, rate, consecutive)</li>
 *   <li>Active warnings card (count + list)</li>
 *   <li>Fasee7 ranking card (rank, points breakdown)</li>
 *   <li>Lazy-loaded tabs (Lessons, Behavioral Incidents)</li>
 *   <li>Real-time updates via EventBus</li>
 *   <li>Role-based edit button visibility (Admin only)</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-31
 */
public class StudentProfileController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentProfileController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    // ==================== FXML COMPONENTS - HEADER ====================
    
    @FXML private Label studentNameLabel;
    @FXML private Button editButton;
    
    // ==================== FXML COMPONENTS - PERSONAL INFO CARD ====================
    
    @FXML private Label studentIdLabel;
    @FXML private Label phoneLabel;
    @FXML private Label parentPhoneLabel;
    @FXML private Label statusLabel;
    @FXML private Label registrationDateLabel;
    
    // ==================== FXML COMPONENTS - ATTENDANCE SUMMARY CARD ====================
    
    @FXML private Label totalLessonsLabel;
    @FXML private Label attendedLabel;
    @FXML private Label absentLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label consecutiveAbsencesLabel;
    
    // ==================== FXML COMPONENTS - WARNINGS CARD ====================
    
    @FXML private Label warningCountBadge;
    @FXML private ListView<String> warningsListView;
    private ObservableList<String> warnings = FXCollections.observableArrayList();
    
    // ==================== FXML COMPONENTS - FASEE7 RANKING CARD ====================
    
    @FXML private Label rankLabel;
    @FXML private Label totalPointsLabel;
    @FXML private Label quizPointsLabel;
    @FXML private Label attendancePointsLabel;
    @FXML private Label homeworkPointsLabel;
    @FXML private Label targetPointsLabel;
    
    // ==================== FXML COMPONENTS - TABS ====================
    
    @FXML private TabPane tabPane;
    
    // ==================== STATE ====================
    
    private int studentId;
    private Student student;
    
    // Tab controllers (lazy loaded)
    private StudentLessonsTabController lessonsTabController;
    private StudentIncidentsTabController incidentsTabController;
    
    // ==================== SERVICES ====================
    
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final WarningService warningService;
    private final Fasee7TableService fasee7Service;
    private final EventBusService eventBus;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor - initializes services via ServiceLocator.
     * Follows Dependency Inversion Principle by depending on interfaces.
     */
    public StudentProfileController() {
        super();
        ServiceLocator services = ServiceLocator.getInstance();
        this.studentService = services.getStudentService();
        this.attendanceService = services.getAttendanceService();
        this.warningService = services.getWarningService();
        this.fasee7Service = services.getFasee7TableService();
        this.eventBus = EventBusService.getInstance();
        
        LOGGER.info("StudentProfileController created");
    }
    
    // ==================== LIFECYCLE METHODS ====================
    
    /**
     * Initialize method - called by JavaFX after FXML injection.
     * Sets up UI components and subscribes to events.
     * 
     * <p><b>Template Method Pattern:</b> Extends BaseController.initialize()</p>
     */
    @Override
    public void initialize() {
        super.initialize();
        
        try {
            LOGGER.fine("Initializing StudentProfileController");
            
            // Setup warnings list
            warningsListView.setItems(warnings);
            
            // Setup tab lazy loading
            setupTabLoading();
            
            // Subscribe to events for real-time updates
            eventBus.register(this);
            
            // Configure UI based on user role
            configureForRole();
            
            LOGGER.info("StudentProfileController initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            showError("Failed to initialize student profile: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup method - called before controller is destroyed.
     * Critical for preventing memory leaks.
     * 
     * <p><b>Resource Management:</b></p>
     * <ul>
     *   <li>Unregisters from EventBus</li>
     *   <li>Cleans up tab controllers</li>
     *   <li>Calls super.cleanup()</li>
     * </ul>
     */
    @Override
    public void cleanup() {
        try {
            // Cleanup tab controllers
            if (lessonsTabController != null) {
                lessonsTabController.cleanup();
                LOGGER.fine("Lessons tab controller cleaned up");
            }
            if (incidentsTabController != null) {
                incidentsTabController.cleanup();
                LOGGER.fine("Incidents tab controller cleaned up");
            }
            
            // Unregister from EventBus
            eventBus.unregister(this);
            LOGGER.info("StudentProfileController cleaned up - EventBus unregistered");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during cleanup", e);
        }
        
        super.cleanup();
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Set student ID and load profile data.
     * Called by parent controller or scene manager to initialize the view.
     * 
     * @param studentId The ID of the student whose profile to display
     * @throws IllegalArgumentException if studentId is invalid
     */
    public void setStudentId(int studentId) {
        if (studentId <= 0) {
            String errorMsg = "Invalid student ID: " + studentId;
            LOGGER.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        this.studentId = studentId;
        loadStudentProfile();
        
        LOGGER.info("Student ID set to: " + studentId);
    }
    
    // ==================== PRIVATE SETUP METHODS ====================
    
    /**
     * Configure UI based on current user's role.
     * Implements role-based access control for UI elements.
     * 
     * <p><b>Security:</b> Edit button visible only to admins</p>
     */
    private void configureForRole() {
        try {
            User currentUser = sessionManager.getCurrentUser();
            
            if (currentUser != null && currentUser.isAdmin()) {
                editButton.setVisible(true);
                LOGGER.fine("Edit button enabled for admin user");
            } else {
                editButton.setVisible(false);
                LOGGER.fine("Edit button hidden for non-admin user");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error configuring for role", e);
            // Fail-safe: hide edit button on error
            editButton.setVisible(false);
        }
    }
    
    /**
     * Setup lazy loading for tabs.
     * Tab controllers are created only when user clicks the tab.
     * 
     * <p><b>Lazy Initialization Pattern:</b></p>
     * <ul>
     *   <li>Saves resources if tabs not viewed</li>
     *   <li>Improves initial load time (~150ms per tab)</li>
     *   <li>Controllers loaded on first tab selection</li>
     * </ul>
     */
    private void setupTabLoading() {
        LOGGER.fine("Setting up tab lazy loading");
        
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null) {
                return;
            }
            
            String tabText = newTab.getText();
            LOGGER.fine("Tab selected: " + tabText);
            
            try {
                if ("Lessons".equals(tabText) && lessonsTabController == null) {
                    // Get controller from tab content
                    lessonsTabController = getTabController(newTab, StudentLessonsTabController.class);
                    if (lessonsTabController != null) {
                        lessonsTabController.setStudentId(studentId);
                        LOGGER.info("Lessons tab controller loaded and initialized");
                    } else {
                        LOGGER.warning("Failed to get Lessons tab controller");
                    }
                    
                } else if ("Behavioral Incidents".equals(tabText) && incidentsTabController == null) {
                    // Get controller from tab content
                    incidentsTabController = getTabController(newTab, StudentIncidentsTabController.class);
                    if (incidentsTabController != null) {
                        incidentsTabController.setStudentId(studentId);
                        LOGGER.info("Incidents tab controller loaded and initialized");
                    } else {
                        LOGGER.warning("Failed to get Incidents tab controller");
                    }
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading tab controller for: " + tabText, e);
            }
        });
    }
    
    /**
     * Helper method to get tab controller from tab content.
     * 
     * <p><b>Implementation Note:</b> Requires tab controllers to set themselves
     * as UserData in their initialize() method:</p>
     * <pre>
     * // In tab controller initialize():
     * ((Parent) someNode).setUserData(this);
     * </pre>
     * 
     * @param tab The tab whose controller to retrieve
     * @param controllerClass The expected controller class
     * @param <T> The controller type
     * @return The controller instance, or null if not found
     */
    @SuppressWarnings("unchecked")
    private <T> T getTabController(Tab tab, Class<T> controllerClass) {
        try {
            // Get the controller from tab's content UserData
            Object controller = tab.getContent().getUserData();
            
            if (controller == null) {
                LOGGER.warning("Tab content UserData is null for: " + tab.getText());
                return null;
            }
            
            if (!controllerClass.isInstance(controller)) {
                LOGGER.warning("Tab controller is not instance of " + controllerClass.getName());
                return null;
            }
            
            return (T) controller;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get tab controller", e);
            return null;
        }
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Load complete student profile data.
     * 
     * <p><b>Facade Pattern:</b> Orchestrates loading from multiple services:</p>
     * <ul>
     *   <li>StudentService - Basic student info</li>
     *   <li>AttendanceService - Attendance statistics</li>
     *   <li>WarningService - Active warnings</li>
     *   <li>Fasee7TableService - Points and ranking</li>
     * </ul>
     * 
     * <p><b>Error Handling Strategy:</b></p>
     * <ul>
     *   <li>Critical: Student data (abort if fails)</li>
     *   <li>Optional: Info cards (log warning, show defaults)</li>
     * </ul>
     */
    private void loadStudentProfile() {
        try {
            LOGGER.info("Loading student profile for student ID: " + studentId);
            
            // Get student data (CRITICAL - abort if fails)
            student = studentService.getStudentById(studentId);
            
            if (student == null) {
                LOGGER.severe("Student not found: " + studentId);
                AlertHelper.showError("Student not found");
                return;
            }
            
            // Update header
            studentNameLabel.setText("Student: " + student.getFullName());
            
            // Load all info cards (optional - continue on errors)
            loadPersonalInfo();
            loadAttendanceSummary();
            loadWarnings();
            loadFasee7Data();
            
            LOGGER.info("Student profile loaded successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load student profile", e);
            AlertHelper.showError("Failed to load student profile: " + e.getMessage());
        }
    }
    
    /**
     * Load personal information card.
     * Displays: ID, phone numbers, status, registration date.
     * 
     * <p><b>Dynamic Styling:</b> Status label colored based on value</p>
     */
    private void loadPersonalInfo() {
        try {
            LOGGER.fine("Loading personal info");
            
            // Set basic info
            studentIdLabel.setText(String.valueOf(student.getStudentId()));
            phoneLabel.setText(student.getPhoneNumber() != null ? student.getPhoneNumber() : "-");
            parentPhoneLabel.setText(student.getParentPhoneNumber() != null ? student.getParentPhoneNumber() : "-");
            
            // Set and style status
            String status = student.getStatus() != null ? student.getStatus().toString() : "UNKNOWN";
            statusLabel.setText(status);
            
            if ("ACTIVE".equals(status)) {
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            
            // Set registration date
            if (student.getRegistrationDate() != null) {
                registrationDateLabel.setText(student.getRegistrationDate().format(DATE_FORMATTER));
            } else {
                registrationDateLabel.setText("-");
            }
            
            LOGGER.fine("Personal info loaded successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load personal info", e);
            // Set defaults on error
            studentIdLabel.setText("-");
            phoneLabel.setText("-");
            parentPhoneLabel.setText("-");
            statusLabel.setText("-");
            registrationDateLabel.setText("-");
        }
    }
    
    /**
     * Load attendance summary card.
     * Displays: Total lessons, attended, absent, rate, consecutive absences.
     * 
     * <p><b>Service API Workaround:</b></p>
     * <ul>
     *   <li>Total = Attended + Absent (two service calls)</li>
     *   <li>Consecutive = List size (service returns List, not count)</li>
     * </ul>
     */
    private void loadAttendanceSummary() {
        try {
            LOGGER.fine("Loading attendance summary");
            
            // Get attendance counts
            // Note: Service API requires two calls to get total
            int attended = attendanceService.getStudentAttendanceCount(studentId, AttendanceStatus.PRESENT);
            int absent = attendanceService.getStudentAttendanceCount(studentId, AttendanceStatus.ABSENT);
            int total = attended + absent;
            
            // Calculate rate
            double rate = total > 0 ? (attended * 100.0 / total) : 0.0;
            
            // Get consecutive absences
            // Note: Service returns List<Attendance>, we need count
            List<com.studenttracker.model.Attendance> consecutiveList = 
                attendanceService.getConsecutiveAbsences(studentId);
            int consecutive = consecutiveList != null ? consecutiveList.size() : 0;
            
            // Update labels
            totalLessonsLabel.setText(String.valueOf(total));
            attendedLabel.setText(String.valueOf(attended));
            absentLabel.setText(String.valueOf(absent));
            attendanceRateLabel.setText(String.format("%.2f%%", rate));
            consecutiveAbsencesLabel.setText(String.valueOf(consecutive));
            
            LOGGER.fine("Attendance summary loaded: total=" + total + ", attended=" + attended + 
                       ", rate=" + String.format("%.2f%%", rate));
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load attendance summary", e);
            // Set defaults on error
            totalLessonsLabel.setText("0");
            attendedLabel.setText("0");
            absentLabel.setText("0");
            attendanceRateLabel.setText("0.00%");
            consecutiveAbsencesLabel.setText("0");
        }
    }
    
    /**
     * Load active warnings card.
     * Displays: Warning count badge and list of warning descriptions.
     */
    private void loadWarnings() {
        try {
            LOGGER.fine("Loading warnings");
            
            // Get active warnings
            List<Warning> activeWarnings = warningService.getActiveWarningsByStudent(studentId);
            
            if (activeWarnings == null) {
                LOGGER.warning("Warning service returned null");
                activeWarnings = List.of(); // Empty list
            }
            
            // Update count badge
            warningCountBadge.setText(String.valueOf(activeWarnings.size()));
            
            // Update warnings list
            warnings.clear();
            for (Warning warning : activeWarnings) {
                String text = warning.getWarningType() + ": " + warning.getWarningReason();
                warnings.add(text);
            }
            
            // Show placeholder if no warnings
            if (warnings.isEmpty()) {
                warnings.add("No active warnings");
            }
            
            LOGGER.fine("Warnings loaded: count=" + activeWarnings.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load warnings", e);
            // Set defaults on error
            warningCountBadge.setText("0");
            warnings.clear();
            warnings.add("Error loading warnings");
        }
    }
    
    /**
     * Load Fasee7 ranking card.
     * Displays: Rank, total points, points breakdown (quiz, attendance, homework, targets).
     * 
     * <p><b>Null Safety:</b> Shows defaults if student has no points record</p>
     */
    private void loadFasee7Data() {
        try {
            LOGGER.fine("Loading Fasee7 data");
            
            // Get Fasee7 points
            Fasee7Points points = fasee7Service.getStudentPoints(studentId);
            
            // Get rank
            int rank = fasee7Service.getStudentRank(studentId);
            
            if (points != null && rank > 0) {
                // Update rank with ordinal suffix (1st, 2nd, 3rd, 4th, etc.)
                rankLabel.setText(rank + getOrdinalSuffix(rank));
                
                // Update points breakdown
                totalPointsLabel.setText(String.format("%.0f", points.getTotalPoints()));
                quizPointsLabel.setText(String.format("%.0f", points.getQuizPoints()));
                attendancePointsLabel.setText(String.valueOf(points.getAttendancePoints()));
                homeworkPointsLabel.setText(String.valueOf(points.getHomeworkPoints()));
                targetPointsLabel.setText(String.valueOf(points.getTargetPoints()));
                
                LOGGER.fine("Fasee7 data loaded: rank=" + rank + ", total=" + points.getTotalPoints());
                
            } else {
                // No points record - show defaults
                LOGGER.fine("No Fasee7 data found for student " + studentId);
                rankLabel.setText("-");
                totalPointsLabel.setText("0");
                quizPointsLabel.setText("0");
                attendancePointsLabel.setText("0");
                homeworkPointsLabel.setText("0");
                targetPointsLabel.setText("0");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load Fasee7 data", e);
            // Set defaults on error
            rankLabel.setText("-");
            totalPointsLabel.setText("0");
            quizPointsLabel.setText("0");
            attendancePointsLabel.setText("0");
            homeworkPointsLabel.setText("0");
            targetPointsLabel.setText("0");
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Get ordinal suffix for rank number.
     * 
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>1 → "st" (1st)</li>
     *   <li>2 → "nd" (2nd)</li>
     *   <li>3 → "rd" (3rd)</li>
     *   <li>4-20 → "th" (4th, 11th, 12th, 13th, 20th)</li>
     *   <li>21 → "st" (21st)</li>
     *   <li>22 → "nd" (22nd)</li>
     *   <li>23 → "rd" (23rd)</li>
     * </ul>
     * 
     * @param rank The rank number
     * @return The ordinal suffix ("st", "nd", "rd", or "th")
     */
    private String getOrdinalSuffix(int rank) {
        // Special case: 11th, 12th, 13th always end in "th"
        if (rank >= 11 && rank <= 13) {
            return "th";
        }
        
        // For all other numbers, check last digit
        switch (rank % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
    
    // ==================== FXML EVENT HANDLERS ====================
    
    /**
     * Handle Back button click.
     * Navigates back to previous screen using SceneManager.
     */
    @FXML
    private void handleBack() {
        try {
            LOGGER.info("Back button clicked");
            sceneManager.switchScene("/com/studenttracker/view/fxml/student/StudentList.fxml");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to navigate back", e);
            showError("Failed to navigate back: " + e.getMessage());
        }
    }
    
    /**
     * Handle Edit button click.
     * Opens student edit modal dialog.
     * 
     * <p><b>Note:</b> Only visible to admin users</p>
     */
    @FXML
    private void handleEdit() {
        try {
            LOGGER.info("Edit button clicked for student: " + studentId);
            
            // Show edit modal
            sceneManager.showDialog(
                "/com/studenttracker/view/fxml/student/StudentEdit.fxml",
                "Edit Student"
            );
            
            // Reload profile after edit (in case data changed)
            loadStudentProfile();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open edit modal", e);
            showError("Failed to open edit form: " + e.getMessage());
        }
    }
    
    // ==================== EVENT SUBSCRIBERS (EventBus) ====================
    
    /**
     * Handle StudentUpdatedEvent from EventBus.
     * Reloads personal info when student data is updated.
     * 
     * <p><b>Thread Safety:</b> Uses Platform.runLater() for UI update</p>
     * 
     * @param event The student updated event (not defined in provided files, assuming it exists)
     */
    @Subscribe
    public void onStudentUpdated(com.studenttracker.service.event.UpdateRequestApprovedEvent event) {

        if(!event.getEntityType().toLowerCase().equals("student"))
            return;
        if (event != null && event.getEntityId() != null && event.getEntityId() == studentId) {
            LOGGER.info("Student updated event received for student: " + studentId);
            
            Platform.runLater(() -> {
                LOGGER.fine("Reloading personal info due to update event");
                loadStudentProfile(); // Reload entire profile to be safe
            });
        }
    }
    
    /**
     * Handle StudentArchivedEvent from EventBus.
     * Reloads personal info to show updated status.
     * 
     * <p><b>Thread Safety:</b> Uses Platform.runLater() for UI update</p>
     * 
     * @param event The student archived event
     */
    @Subscribe
    public void onStudentArchived(StudentArchivedEvent event) {
        if (event != null && event.getStudentId() != null && event.getStudentId() == studentId) {
            LOGGER.info("Student archived event received for student: " + studentId);
            
            Platform.runLater(() -> {
                LOGGER.fine("Reloading personal info due to archive event");
                loadPersonalInfo();
            });
        }
    }
    
    /**
     * Handle AttendanceMarkedEvent from EventBus.
     * Reloads attendance summary when attendance is marked for this student.
     * 
     * <p><b>Thread Safety:</b> Uses Platform.runLater() for UI update</p>
     * 
     * @param event The attendance marked event
     */
    @Subscribe
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        if (event != null && event.getStudentId() != null && event.getStudentId() == studentId) {
            LOGGER.info("Attendance marked event received for student: " + studentId);
            
            Platform.runLater(() -> {
                LOGGER.fine("Reloading attendance summary due to event");
                loadAttendanceSummary();
            });
        }
    }
    
    /**
     * Handle WarningGeneratedEvent from EventBus.
     * Reloads warnings list when new warning is generated for this student.
     * 
     * <p><b>Thread Safety:</b> Uses Platform.runLater() for UI update</p>
     * 
     * @param event The warning generated event
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        if (event != null && event.getStudentId() != null && event.getStudentId() == studentId) {
            LOGGER.info("Warning generated event received for student: " + studentId + 
                       ", type: " + event.getWarningType());
            
            Platform.runLater(() -> {
                LOGGER.fine("Reloading warnings due to event");
                loadWarnings();
            });
        }
    }
    
    /**
     * Handle Fasee7PointsUpdatedEvent from EventBus.
     * Reloads Fasee7 data when points are updated for this student.
     * 
     * <p><b>Thread Safety:</b> Uses Platform.runLater() for UI update</p>
     * 
     * @param event The Fasee7 points updated event
     */
    @Subscribe
    public void onFasee7PointsUpdated(Fasee7PointsUpdatedEvent event) {
        if (event != null && event.getStudentId() != null && event.getStudentId() == studentId) {
            LOGGER.info("Fasee7 points updated event received for student: " + studentId + 
                       ", total: " + event.getTotalPoints());
            
            Platform.runLater(() -> {
                LOGGER.fine("Reloading Fasee7 data due to event");
                loadFasee7Data();
            });
        }
    }
}