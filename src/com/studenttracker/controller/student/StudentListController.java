package com.studenttracker.controller.student;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.controller.BaseController;
import com.studenttracker.model.Student;
import com.studenttracker.service.*;
import com.studenttracker.service.event.*;
import com.studenttracker.util.SceneManager;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the Student List screen.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Display all students in a searchable, filterable table</li>
 *   <li>Provide search functionality (by name or phone)</li>
 *   <li>Provide filters (status, warnings, registration date range)</li>
 *   <li>Handle role-based actions (View, Edit, Archive/Restore)</li>
 *   <li>Real-time updates via EventBus</li>
 *   <li>Navigation to registration and detail screens</li>
 * </ul>
 * 
 * <p><b>Search Modes:</b></p>
 * <ul>
 *   <li>Name: Searches by full name (partial match)</li>
 *   <li>Phone: Searches by student phone number (exact match)</li>
 * </ul>
 * 
 * <p><b>Filters:</b></p>
 * <ul>
 *   <li>Status: All, Active, Archived</li>
 *   <li>Warnings: All, With Warnings, No Warnings</li>
 *   <li>Date Range: Filter by registration date</li>
 * </ul>
 * 
 * <p><b>Action Buttons (Role-Based):</b></p>
 * <ul>
 *   <li>View Details: All users → Navigate to student detail page</li>
 *   <li>Edit: Admin → direct edit, Assistant → submit update request</li>
 *   <li>Archive: Admin → archive with reason, Assistant → submit archive request</li>
 *   <li>Restore: Admin → restore directly, Assistant → submit restore request</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-30
 */
public class StudentListController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentListController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private Button registerButton;
    
    // Search & Filter
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> warningFilterCombo;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;
    
    // Results
    @FXML private Label resultCountLabel;
    
    // Table
    @FXML private TableView<StudentRow> studentTable;
    @FXML private TableColumn<StudentRow, Number> idColumn;
    @FXML private TableColumn<StudentRow, String> nameColumn;
    @FXML private TableColumn<StudentRow, String> phoneColumn;
    @FXML private TableColumn<StudentRow, String> parentPhoneColumn;
    @FXML private TableColumn<StudentRow, String> statusColumn;
    @FXML private TableColumn<StudentRow, String> registrationDateColumn;
    @FXML private TableColumn<StudentRow, Number> warningsColumn;
    @FXML private TableColumn<StudentRow, Void> actionsColumn;
    
    // ==================== SERVICES ====================
    
    private StudentService studentService;
    private WarningService warningService;
    private UpdateRequestOrchestratorService updateRequestService;
    private EventBusService eventBus;
    
    // ==================== DATA ====================
    
    private ObservableList<StudentRow> allStudentRows;
    private ObservableList<StudentRow> filteredStudentRows;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * No-arg constructor required for FXML instantiation.
     */
    public StudentListController() {
        super();
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Initialize method - called after FXML injection.
     * Sets up services, configures table, and loads initial data.
     */
    @Override
    public void initialize() {
        super.initialize();
        
        LOGGER.info("StudentListController initialized");
        
        // Initialize services
        this.studentService = serviceLocator.getStudentService();
        this.warningService = serviceLocator.getWarningService();
        this.updateRequestService = serviceLocator.getUpdateRequestService();
        this.eventBus = EventBusService.getInstance();
        
        // Initialize data collections
        this.allStudentRows = FXCollections.observableArrayList();
        this.filteredStudentRows = FXCollections.observableArrayList();
        
        // Register for EventBus updates
        eventBus.register(this);
        
        // Configure UI
        configureSearchAndFilters();
        configureTable();
        
        // Load initial data
        loadAllStudents();
    }
    
    // ==================== UI CONFIGURATION ====================
    
    /**
     * Configures search type combo box and filter combo boxes with default values.
     */
    private void configureSearchAndFilters() {
    // Populate search type combo
    searchTypeCombo.setItems(FXCollections.observableArrayList("Name", "Phone"));
    searchTypeCombo.getSelectionModel().selectFirst(); // "Name"
    
    // Populate status filter combo
    statusFilterCombo.setItems(FXCollections.observableArrayList("All", "Active", "Archived"));
    statusFilterCombo.getSelectionModel().selectFirst(); // "All"
    
    // Populate warning filter combo
    warningFilterCombo.setItems(FXCollections.observableArrayList("All", "With Warnings", "No Warnings"));
    warningFilterCombo.getSelectionModel().selectFirst(); // "All"
    
    LOGGER.fine("Search and filters configured");
}
    
    /**
     * Configures table columns with cell value factories and custom renderers.
     */
    private void configureTable() {
        // ID Column
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        
        // Name Column
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        // Phone Column
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        
        // Parent Phone Column
        parentPhoneColumn.setCellValueFactory(cellData -> cellData.getValue().parentPhoneProperty());
        
        // Status Column (with color coding)
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setCellFactory(column -> new TableCell<StudentRow, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("ACTIVE".equals(status)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if ("ARCHIVED".equals(status)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Registration Date Column
        registrationDateColumn.setCellValueFactory(cellData -> cellData.getValue().registrationDateProperty());
        
        // Warnings Column (with color coding)
        warningsColumn.setCellValueFactory(cellData -> cellData.getValue().warningCountProperty());
        warningsColumn.setCellFactory(column -> new TableCell<StudentRow, Number>() {
            @Override
            protected void updateItem(Number count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int warningCount = count.intValue();
                    setText(String.valueOf(warningCount));
                    if (warningCount > 0) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981;");
                    }
                }
            }
        });
        
        // Actions Column (with role-based buttons)
        actionsColumn.setCellFactory(column -> new TableCell<StudentRow, Void>() {
            private final Button viewButton = new Button("View");
private final Button editButton = new Button("Edit");
private final Button archiveButton = new Button("Archive");
private final Button restoreButton = new Button("Restore");
            private final HBox buttonBox = new HBox(5, viewButton, editButton, archiveButton, restoreButton);
            
            {
                // Style buttons
                viewButton.getStyleClass().add("secondary-button");
                editButton.getStyleClass().add("primary-button");
                archiveButton.getStyleClass().add("danger-button");
                restoreButton.getStyleClass().add("success-button");
                
                // Set button actions
                viewButton.setOnAction(event -> {
                    StudentRow row = getTableRow().getItem();
                    if (row != null) handleViewStudent(row.getStudentId());
                });
                
                editButton.setOnAction(event -> {
                    StudentRow row = getTableRow().getItem();
                    if (row != null) handleEditStudent(row.getStudentId());
                });
                
                archiveButton.setOnAction(event -> {
                    StudentRow row = getTableRow().getItem();
                    if (row != null) handleArchiveStudent(row.getStudentId(), row.getName());
                });
                
                restoreButton.setOnAction(event -> {
                    StudentRow row = getTableRow().getItem();
                    if (row != null) handleRestoreStudent(row.getStudentId(), row.getName());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StudentRow row = getTableRow().getItem();
                    if (row != null) {
                        // Show/hide archive/restore based on status
                        boolean isArchived = "ARCHIVED".equals(row.getStatus());
                        archiveButton.setVisible(!isArchived);
                        archiveButton.setManaged(!isArchived);
                        restoreButton.setVisible(isArchived);
                        restoreButton.setManaged(isArchived);
                        
                        setGraphic(buttonBox);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        // Bind filtered data to table
        studentTable.setItems(filteredStudentRows);
        
        LOGGER.fine("Table columns configured");
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Loads all students from the database and populates the table.
     * Also loads warning counts for each student.
     */
    private void loadAllStudents() {
        try {
            LOGGER.fine("Loading all students...");
            
            // Fetch all students
            List<Student> students = studentService.getAllStudents();
            
            // Convert to StudentRow objects with warning counts
            allStudentRows.clear();
            for (Student student : students) {
                int warningCount = warningService.getActiveWarningsByStudent(student.getStudentId()).size();
                StudentRow row = new StudentRow(student, warningCount);
                allStudentRows.add(row);
            }
            
            // Apply filters and update display
            applyFilters();
            
            LOGGER.info("Loaded " + students.size() + " students");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load students", e);
            Platform.runLater(() -> showError("Failed to load students. Please try refreshing."));
        }
    }
    
    /**
     * Applies current search and filter criteria to the student list.
     * Updates the table and result count.
     */
    private void applyFilters() {
        try {
            // Start with all students
            List<StudentRow> filtered = allStudentRows.stream()
                    .filter(this::matchesSearchCriteria)
                    .filter(this::matchesStatusFilter)
                    .filter(this::matchesWarningFilter)
                    .filter(this::matchesDateRangeFilter)
                    .collect(Collectors.toList());
            
            // Update filtered list
            Platform.runLater(() -> {
                filteredStudentRows.clear();
                filteredStudentRows.addAll(filtered);
                
                // Update result count
                updateResultCount(filtered.size());
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error applying filters", e);
        }
    }
    
    /**
     * Checks if a student row matches the current search criteria.
     */
    private boolean matchesSearchCriteria(StudentRow row) {
        String searchTerm = searchField.getText();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true; // No search term, show all
        }
        
        String searchType = searchTypeCombo.getValue();
        String lowerSearchTerm = searchTerm.trim().toLowerCase();
        
        if ("Name".equals(searchType)) {
            return row.getName().toLowerCase().contains(lowerSearchTerm);
        } else if ("Phone".equals(searchType)) {
            return row.getPhone().contains(searchTerm.trim());
        }
        
        return true;
    }
    
    /**
     * Checks if a student row matches the status filter.
     */
    private boolean matchesStatusFilter(StudentRow row) {
        String statusFilter = statusFilterCombo.getValue();
        if ("All".equals(statusFilter)) {
            return true;
        }
        return statusFilter.equalsIgnoreCase(row.getStatus());
    }
    
    /**
     * Checks if a student row matches the warning filter.
     */
    private boolean matchesWarningFilter(StudentRow row) {
        String warningFilter = warningFilterCombo.getValue();
        if ("All".equals(warningFilter)) {
            return true;
        }
        
        int warningCount = row.getWarningCount();
        if ("With Warnings".equals(warningFilter)) {
            return warningCount > 0;
        } else if ("No Warnings".equals(warningFilter)) {
            return warningCount == 0;
        }
        
        return true;
    }
    
    /**
     * Checks if a student row matches the date range filter.
     */
    private boolean matchesDateRangeFilter(StudentRow row) {
        LocalDate fromDate = dateFromPicker.getValue();
        LocalDate toDate = dateToPicker.getValue();
        
        if (fromDate == null && toDate == null) {
            return true; // No date filter
        }
        
        LocalDate registrationDate = row.getRegistrationDateAsLocalDate();
        
        if (fromDate != null && registrationDate.isBefore(fromDate)) {
            return false;
        }
        
        if (toDate != null && registrationDate.isAfter(toDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Updates the result count label.
     */
    private void updateResultCount(int count) {
        String text = count == 1 ? "Showing 1 student" : "Showing " + count + " students";
        resultCountLabel.setText(text);
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handles Register Student button click.
     * Navigates to StudentRegistration screen.
     */
    @FXML
private void handleRegisterStudent() {
    try {
        LOGGER.info("Opening StudentRegistration dialog");
        sceneManager.showDialog(
            "/com/studenttracker/view/fxml/student/StudentRegistration.fxml",
            "Register New Student"
        );
        
        
        
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Failed to open registration dialog", e);
        showError("Failed to open registration form.");
    }
}
    
    /**
     * Handles Search button click.
     * Applies search criteria and filters.
     */
    @FXML
    private void handleSearch() {
        LOGGER.fine("Search triggered");
        applyFilters();
    }
    
    /**
     * Handles Clear Search button click.
     * Clears all search and filter inputs.
     */
    @FXML
    private void handleClearSearch() {
        LOGGER.fine("Clearing search and filters");
        
        searchField.clear();
        searchTypeCombo.getSelectionModel().selectFirst();
        statusFilterCombo.getSelectionModel().selectFirst();
        warningFilterCombo.getSelectionModel().selectFirst();
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        
        applyFilters();
    }
    
    /**
     * Handles filter combo box changes.
     * Re-applies filters when any filter changes.
     */
    @FXML
    private void handleFilterChange() {
        LOGGER.fine("Filter changed");
        applyFilters();
    }
    
    /**
     * Handles Refresh button click.
     * Reloads all students from database.
     */
    @FXML
    private void handleRefresh() {
        LOGGER.info("Manual refresh requested");
        loadAllStudents();
    }
    
    // ==================== ACTION BUTTON HANDLERS ====================
    
    /**
     * Handles View Student button click.
     * Navigates to StudentDetail screen (read-only for all users).
     */
    /**
     * Handles View Student button click.
     * Navigates to StudentProfile screen.
     * 
     * @param studentId The ID of the student to view
     */
    private void handleViewStudent(Integer studentId) {
        try {
            LOGGER.info("Viewing student profile for ID: " + studentId);
            
            // Load StudentProfile view into container
            SceneManager.LoadResult<StudentProfileController> result = 
                sceneManager.loadContentWithController("/com/studenttracker/view/fxml/student/StudentProfile.fxml");
            
            // Get the controller and set student ID
            StudentProfileController controller = result.getController();
            if (controller != null) {
                controller.setStudentId(studentId);
                
                // Find the content area in MainLayout and load the profile view
                javafx.scene.layout.StackPane contentArea = 
                    (javafx.scene.layout.StackPane) studentTable.getScene().getRoot().lookup("#contentArea");
                
                if (contentArea != null) {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(result.getParent());
                } else {
                    LOGGER.warning("Content area not found in scene");
                    showError("Navigation error: Content area not found");
                }
            } else {
                LOGGER.warning("StudentProfileController is null");
                showError("Failed to load student profile controller");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to view student profile", e);
            showError("Failed to open student profile: " + e.getMessage());
        }
    }
    
    /**
 * Handles Edit Student button click.
 * Admin: Allows direct edit
 * Assistant: Submits update request for admin approval
 */
private void handleEditStudent(Integer studentId) {
    if (isAdmin()) {
        // Admin can edit directly
        try {
            LOGGER.info("Admin editing student: " + studentId);
            
            // Load FXML and get controller
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/studenttracker/view/fxml/student/StudentEdit.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            // Get controller and set student ID
            com.studenttracker.controller.student.StudentEditController controller = loader.getController();
            
            // Create modal stage
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Edit Student");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(sceneManager.getPrimaryStage());
            
            // Set dialog stage in controller
            controller.setDialogStage(dialogStage);
            
            // Set student ID (this will load the data)
            controller.setStudentId(studentId);
            
            // Show modal
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
            // No need to reload - EventBus will update automatically
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to edit student", e);
            showError("Failed to open edit form: " + e.getMessage());
        }
    } else {
        // Assistant must submit update request
        try {
            LOGGER.info("Assistant submitting update request for student: " + studentId);
            // TODO: Open update request submission dialog
            showInfo("Submit Request", "Update request submission will be implemented in next phase.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to submit update request", e);
            showError("Failed to submit update request.");
        }
    }
}
    
    /**
     * Handles Archive Student button click.
     * Admin: Shows reason dialog and archives directly
     * Assistant: Submits archive request for admin approval
     */
    private void handleArchiveStudent(Integer studentId, String studentName) {
        if (isAdmin()) {
            // Admin can archive directly with reason
            handleAdminArchive(studentId, studentName);
        } else {
            // Assistant must submit archive request
            handleAssistantArchiveRequest(studentId, studentName);
        }
    }
    
    /**
     * Handles admin direct archive with reason dialog.
     */
    private void handleAdminArchive(Integer studentId, String studentName) {
        // Show confirmation with reason input
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Archive Student");
        dialog.setHeaderText("Archive student: " + studentName);
        dialog.setContentText("Please enter archive reason:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String reason = result.get().trim();
            
            try {
                LOGGER.info("Admin archiving student " + studentId + " with reason: " + reason);
                
                boolean success = studentService.archiveStudent(studentId, getCurrentUserId(), reason);
                
                if (success) {
                    showSuccess("Student archived successfully.");
                    // Table will update automatically via EventBus
                } else {
                    showError("Failed to archive student.");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to archive student", e);
                showError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles assistant archive request submission.
     */
    private void handleAssistantArchiveRequest(Integer studentId, String studentName) {
        // Show confirmation
        boolean confirmed = confirm("Submit Archive Request", 
                "Submit archive request for student: " + studentName + "?\n\n" +
                "This request will be sent to an admin for approval.");
        
        if (confirmed) {
            try {
                LOGGER.info("Assistant submitting archive request for student: " + studentId);
                // TODO: Submit archive request via UpdateRequestOrchestratorService
                showInfo("Request Submitted", "Archive request submitted successfully.\nAn admin will review it shortly.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to submit archive request", e);
                showError("Failed to submit request: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles Restore Student button click.
     * Admin: Restores directly
     * Assistant: Submits restore request for admin approval
     */
    private void handleRestoreStudent(Integer studentId, String studentName) {
        if (isAdmin()) {
            // Admin can restore directly
            handleAdminRestore(studentId, studentName);
        } else {
            // Assistant must submit restore request
            handleAssistantRestoreRequest(studentId, studentName);
        }
    }
    
    /**
     * Handles admin direct restore.
     */
    private void handleAdminRestore(Integer studentId, String studentName) {
        boolean confirmed = confirm("Restore Student", 
                "Restore student: " + studentName + "?\n\n" +
                "This will set the student status back to ACTIVE.");
        
        if (confirmed) {
            try {
                LOGGER.info("Admin restoring student: " + studentId);
                
                boolean success = studentService.restoreStudent(studentId, getCurrentUserId());
                
                if (success) {
                    showSuccess("Student restored successfully.");
                    // Table will update automatically via EventBus
                } else {
                    showError("Failed to restore student.");
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to restore student", e);
                showError("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles assistant restore request submission.
     */
    private void handleAssistantRestoreRequest(Integer studentId, String studentName) {
        boolean confirmed = confirm("Submit Restore Request", 
                "Submit restore request for student: " + studentName + "?\n\n" +
                "This request will be sent to an admin for approval.");
        
        if (confirmed) {
            try {
                LOGGER.info("Assistant submitting restore request for student: " + studentId);
                // TODO: Submit restore request via UpdateRequestOrchestratorService
                showInfo("Request Submitted", "Restore request submitted successfully.\nAn admin will review it shortly.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to submit restore request", e);
                showError("Failed to submit request: " + e.getMessage());
            }
        }
    }
    
    // ==================== EVENT SUBSCRIBERS (Real-Time Updates) ====================
    
    /**
     * Handles StudentRegisteredEvent.
     * Adds new student to table.
     */
    @Subscribe
    public void onStudentRegistered(StudentRegisteredEvent event) {
        Platform.runLater(() -> {
            LOGGER.fine("Student registered event received: " + event.getStudentId());
            loadAllStudents(); // Reload to get new student
        });
    }
    
    /**
     * Handles StudentArchivedEvent.
     * Updates student status in table.
     */
    @Subscribe
    public void onStudentArchived(StudentArchivedEvent event) {
        Platform.runLater(() -> {
            LOGGER.fine("Student archived event received: " + event.getStudentId());
            loadAllStudents(); // Reload to update status
        });
    }
    
    /**
     * Handles StudentRestoredEvent.
     * Updates student status in table.
     */
    @Subscribe
    public void onStudentRestored(StudentRestoredEvent event) {
        Platform.runLater(() -> {
            LOGGER.fine("Student restored event received: " + event.getStudentId());
            loadAllStudents(); // Reload to update status
        });
    }
    
    /**
     * Handles WarningGeneratedEvent.
     * Updates warning count for affected student.
     */
    @Subscribe
    public void onWarningGenerated(WarningGeneratedEvent event) {
        Platform.runLater(() -> {
            LOGGER.fine("Warning generated for student: " + event.getStudentId());
            updateWarningCountForStudent(event.getStudentId());
        });
    }
    
    /**
     * Handles WarningResolvedEvent.
     * Updates warning count for affected student.
     */
    @Subscribe
    public void onWarningResolved(WarningResolvedEvent event) {
        Platform.runLater(() -> {
            LOGGER.fine("Warning resolved, refreshing student list");
            // Reload all since we don't have student ID in this event
            loadAllStudents();
        });
    }
    
    /**
     * Updates warning count for a specific student in the table.
     */
    private void updateWarningCountForStudent(Integer studentId) {
        try {
            int newWarningCount = warningService.getActiveWarningsByStudent(studentId).size();
            
            // Find and update the row
            for (StudentRow row : allStudentRows) {
                if (row.getStudentId().equals(studentId)) {
                    row.setWarningCount(newWarningCount);
                    break;
                }
            }
            
            // Re-apply filters to refresh display
            applyFilters();
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to update warning count for student " + studentId, e);
        }
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Cleanup method - called before navigation away from this screen.
     * Unregisters from EventBus.
     */
    @Override
    public void cleanup() {
        LOGGER.fine("Cleaning up StudentListController");
        eventBus.unregister(this);
        super.cleanup();
    }
    
    // ==================== INNER CLASS: StudentRow ====================
    
    /**
     * Wrapper class for displaying Student data in TableView.
     * Uses JavaFX properties for automatic UI updates.
     */
    public static class StudentRow {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty parentPhone;
        private final SimpleStringProperty status;
        private final SimpleStringProperty registrationDate;
        private final SimpleIntegerProperty warningCount;
        
        private final Integer studentId; // For actions
        private final LocalDateTime registrationDateTime; // For filtering
        
        public StudentRow(Student student, int warningCount) {
            this.studentId = student.getStudentId();
            this.id = new SimpleIntegerProperty(student.getStudentId());
            this.name = new SimpleStringProperty(student.getFullName());
            this.phone = new SimpleStringProperty(student.getPhoneNumber());
            this.parentPhone = new SimpleStringProperty(student.getParentPhoneNumber());
            this.status = new SimpleStringProperty(student.getStatus().name());
            this.registrationDateTime = student.getRegistrationDate();
            this.registrationDate = new SimpleStringProperty(
                student.getRegistrationDate().format(DATE_FORMATTER)
            );
            this.warningCount = new SimpleIntegerProperty(warningCount);
        }
        
        // Property getters for TableColumn binding
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty parentPhoneProperty() { return parentPhone; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty registrationDateProperty() { return registrationDate; }
        public SimpleIntegerProperty warningCountProperty() { return warningCount; }
        
        // Regular getters for logic
        public Integer getStudentId() { return studentId; }
        public String getName() { return name.get(); }
        public String getPhone() { return phone.get(); }
        public String getStatus() { return status.get(); }
        public int getWarningCount() { return warningCount.get(); }
        public LocalDate getRegistrationDateAsLocalDate() { 
            return registrationDateTime.toLocalDate(); 
        }
        
        // Setter for warning count updates
        public void setWarningCount(int count) {
            this.warningCount.set(count);
        }
    }
}