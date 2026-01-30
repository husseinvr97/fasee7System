package com.studenttracker.controller.student.tabs;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.controller.BaseController;
import com.studenttracker.model.BehavioralIncident;
import com.studenttracker.service.BehavioralIncidentService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.BehavioralIncidentAddedEvent;
import com.studenttracker.util.AlertHelper;
import com.studenttracker.util.ServiceLocator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Student Behavioral Incidents Tab.
 * Displays all behavioral incidents for a specific student with real-time updates.
 * 
 * <p><b>Design Patterns Used:</b></p>
 * <ul>
 *   <li>Observer Pattern - EventBus subscriptions for real-time updates</li>
 *   <li>Template Method - Extends BaseController lifecycle</li>
 *   <li>Dependency Inversion - Depends on service interfaces</li>
 * </ul>
 * 
 * <p><b>SOLID Principles:</b></p>
 * <ul>
 *   <li>SRP - Single responsibility: manage incidents tab UI only</li>
 *   <li>DIP - Depends on BehavioralIncidentService interface, not implementation</li>
 *   <li>OCP - Open for extension via events, closed for modification</li>
 * </ul>
 * 
 * <p><b>Thread Safety:</b> All UI updates use Platform.runLater() when triggered by events</p>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-30
 */
public class StudentIncidentsTabController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentIncidentsTabController.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // ==================== UI COMPONENTS ====================
    
    @FXML private Label totalIncidentsLabel;
    @FXML private Button refreshButton;
    @FXML private TableView<BehavioralIncident> incidentsTable;
    @FXML private TableColumn<BehavioralIncident, String> dateColumn;
    @FXML private TableColumn<BehavioralIncident, String> lessonColumn;
    @FXML private TableColumn<BehavioralIncident, String> typeColumn;
    @FXML private TableColumn<BehavioralIncident, String> notesColumn;
    @FXML private TableColumn<BehavioralIncident, String> createdByColumn;
    
    // ==================== DATA COLLECTIONS ====================
    
    private int studentId;
    private ObservableList<BehavioralIncident> incidents = FXCollections.observableArrayList();
    
    // ==================== SERVICES ====================
    
    private final BehavioralIncidentService incidentService;
    private final EventBusService eventBus;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor - initializes services via ServiceLocator.
     * Follows Dependency Inversion Principle by depending on interfaces.
     */
    public StudentIncidentsTabController() {
        super();
        ServiceLocator services = ServiceLocator.getInstance();
        this.incidentService = services.getBehavioralIncidentService();
        this.eventBus = EventBusService.getInstance();
        
        LOGGER.info("StudentIncidentsTabController created");
    }
    
    // ==================== LIFECYCLE METHODS ====================
    
    /**
     * Initialize method - called by JavaFX after FXML injection.
     * Sets up table columns, bindings, and event subscriptions.
     * 
     * <p><b>Template Method Pattern:</b> Extends BaseController.initialize()</p>
     */
    @Override
    public void initialize() {
        super.initialize();
        
        try {
            LOGGER.fine("Initializing StudentIncidentsTabController");
            
            // Setup table columns with cell value factories
            setupTableColumns();
            
            // Bind table to observable list
            incidentsTable.setItems(incidents);
            
            // Subscribe to events for real-time updates
            eventBus.register(this);
            
            LOGGER.info("StudentIncidentsTabController initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during initialization", e);
            showError("Failed to initialize incidents tab: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup method - called before controller is destroyed.
     * Unregisters from EventBus to prevent memory leaks.
     * 
     * <p><b>Resource Management:</b> Critical for preventing memory leaks</p>
     */
    @Override
    public void cleanup() {
        try {
            eventBus.unregister(this);
            LOGGER.info("StudentIncidentsTabController cleaned up - EventBus unregistered");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during cleanup", e);
        }
        super.cleanup();
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Set the student ID and load incidents.
     * Called by parent controller to initialize tab with student data.
     * 
     * <p><b>Validation:</b> Ensures valid student ID before loading data</p>
     * <p><b>Optimization:</b> Skips reload if student ID hasn't changed</p>
     * 
     * @param studentId The ID of the student whose incidents to display
     * @throws IllegalArgumentException if studentId is null or <= 0
     */
    public void setStudentId(int studentId) {
        if (studentId <= 0) {
            String errorMsg = "Invalid student ID: " + studentId;
            LOGGER.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        // Optimization: Skip reload if student ID hasn't changed
        if (this.studentId == studentId) {
            LOGGER.fine("Student ID unchanged (" + studentId + "), skipping reload");
            return;
        }
        
        this.studentId = studentId;
        loadIncidents();
        
        LOGGER.info("Student ID set to: " + studentId);
    }
    
    // ==================== PRIVATE SETUP METHODS ====================
    
    /**
     * Setup table columns with cell value factories.
     * Implements presentation logic for displaying incident data.
     * 
     * <p><b>Design Note:</b> Separates data transformation from data retrieval</p>
     */
    private void setupTableColumns() {
        LOGGER.fine("Setting up table columns");
        
        // Date column - format LocalDateTime to readable string
        dateColumn.setCellValueFactory(cellData -> {
            BehavioralIncident incident = cellData.getValue();
            if (incident.getCreatedAt() != null) {
                return new SimpleStringProperty(incident.getCreatedAt().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("-");
        });
        
        // Lesson column - display lesson ID
        lessonColumn.setCellValueFactory(cellData -> {
            BehavioralIncident incident = cellData.getValue();
            if (incident.getLessonId() != null) {
                return new SimpleStringProperty("Lesson " + incident.getLessonId());
            }
            return new SimpleStringProperty("-");
        });
        
        // Type column - display incident type with formatting
        typeColumn.setCellValueFactory(cellData -> {
            BehavioralIncident incident = cellData.getValue();
            if (incident.getIncidentType() != null) {
                // Format enum name: DISRESPECTFUL -> Disrespectful
                String type = incident.getIncidentType().toString();
                String formatted = type.charAt(0) + type.substring(1).toLowerCase().replace('_', ' ');
                return new SimpleStringProperty(formatted);
            }
            return new SimpleStringProperty("-");
        });
        
        // Apply custom cell factory for type column to add color coding
        typeColumn.setCellFactory(column -> new TableCell<BehavioralIncident, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    // Color code by severity
                    if (type.contains("Disrespectful")) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else if (type.contains("Late") || type.contains("Left early")) {
                        setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6c757d;");
                    }
                }
            }
        });
        
        // Notes column - display notes or placeholder
        notesColumn.setCellValueFactory(cellData -> {
            BehavioralIncident incident = cellData.getValue();
            String notes = incident.getNotes();
            return new SimpleStringProperty(notes != null && !notes.trim().isEmpty() ? notes : "-");
        });
        
        // Created by column - display user ID
        // Note: Could be enhanced to show username by joining with User table
        createdByColumn.setCellValueFactory(cellData -> {
            BehavioralIncident incident = cellData.getValue();
            if (incident.getCreatedBy() != null) {
                return new SimpleStringProperty("User #" + incident.getCreatedBy());
            }
            return new SimpleStringProperty("-");
        });
        
        LOGGER.fine("Table columns configured successfully");
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Load all incidents for the current student from the service layer.
     * 
     * <p><b>Error Handling:</b> Comprehensive try-catch with user feedback</p>
     * <p><b>Data Flow:</b> Service → ObservableList → TableView</p>
     */
    private void loadIncidents() {
        try {
            LOGGER.info("Loading incidents for student: " + studentId);
            
            // Disable refresh button during load
            if (refreshButton != null) {
                refreshButton.setDisable(true);
            }
            
            // Fetch incidents from service layer
            List<BehavioralIncident> incidentList = incidentService.getIncidentsByStudent(studentId);
            
            // Handle null or empty results
            if (incidentList == null) {
                LOGGER.warning("Service returned null for student " + studentId);
                incidents.clear();
                updateIncidentCount(0);
                return;
            }
            
            // Update observable list (automatically updates table)
            incidents.setAll(incidentList);
            
            // Update count label
            updateIncidentCount(incidentList.size());
            
            LOGGER.info("Successfully loaded " + incidentList.size() + " incidents for student " + studentId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load incidents for student " + studentId, e);
            
            // Clear data on error (defensive programming)
            incidents.clear();
            updateIncidentCount(0);
            
            // Notify user
            AlertHelper.showError("Loading Error : Failed to load behavioral incidents. Please try again.\n\nError: " + e.getMessage());
            
        } finally {
            // Re-enable refresh button
            if (refreshButton != null) {
                refreshButton.setDisable(false);
            }
        }
    }
    
    /**
     * Update the total incidents count label.
     * 
     * @param count The number of incidents to display
     */
    private void updateIncidentCount(int count) {
        if (totalIncidentsLabel != null) {
            totalIncidentsLabel.setText(String.valueOf(count));
            LOGGER.fine("Updated incident count to: " + count);
        }
    }
    
    // ==================== EVENT HANDLERS (FXML) ====================
    
    /**
     * Handle refresh button click.
     * Reloads all incidents from the database.
     * 
     * <p><b>User Action:</b> Manual refresh requested</p>
     */
    @FXML
    private void handleRefresh() {
        LOGGER.fine("Refresh button clicked by user");
        loadIncidents();
    }
    
    // ==================== EVENT SUBSCRIBERS (EventBus) ====================
    
    /**
     * Handle BehavioralIncidentAddedEvent from EventBus.
     * Automatically reloads incidents when a new incident is added for this student.
     * 
     * <p><b>Observer Pattern:</b> Subscribes to domain events</p>
     * <p><b>Thread Safety:</b> Uses Platform.runLater() for UI updates</p>
     * <p><b>Filtering:</b> Only reloads if incident belongs to current student</p>
     * 
     * @param event The behavioral incident added event
     */
    @Subscribe
    public void onBehavioralIncidentAdded(BehavioralIncidentAddedEvent event) {
        if (event == null) {
            LOGGER.warning("Received null BehavioralIncidentAddedEvent");
            return;
        }
        
        // Guard: Ensure student ID is set
        if (studentId <= 0) {
            LOGGER.fine("Student ID not set, ignoring event");
            return;
        }
        
        // Filter: Only reload if incident is for current student
        if (event.getStudentId() != null && event.getStudentId().equals(studentId)) {
            LOGGER.info("Received BehavioralIncidentAddedEvent for student " + studentId + 
                       " (incident ID: " + event.getIncidentId() + ", type: " + event.getIncidentType() + ")");
            
            // Thread safety: Ensure UI update happens on JavaFX Application Thread
            Platform.runLater(() -> {
                LOGGER.fine("Reloading incidents due to event");
                loadIncidents();
            });
        } else {
            LOGGER.finer("Ignored BehavioralIncidentAddedEvent for student " + event.getStudentId() + 
                        " (current student: " + studentId + ")");
        }
    }
    
    // ==================== ADDITIONAL HELPER METHODS ====================
    
    /**
     * Get the current incident count.
     * Useful for parent controllers that need to display summary information.
     * 
     * @return The number of incidents currently displayed
     */
    public int getIncidentCount() {
        return incidents.size();
    }
    
    /**
     * Check if there are any incidents for the current student.
     * 
     * @return true if student has incidents, false otherwise
     */
    public boolean hasIncidents() {
        return !incidents.isEmpty();
    }
    
    /**
     * Get the most recent incident for the current student.
     * 
     * @return The most recent incident, or null if no incidents exist
     */
    public BehavioralIncident getMostRecentIncident() {
        if (incidents.isEmpty()) {
            return null;
        }
        
        // Incidents are typically sorted by createdAt descending from service
        return incidents.get(0);
    }
}