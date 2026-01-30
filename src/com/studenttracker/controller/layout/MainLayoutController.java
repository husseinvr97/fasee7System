package com.studenttracker.controller.layout;

import com.studenttracker.controller.BaseController;
import com.studenttracker.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Main Layout screen.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Display user information in header</li>
 *   <li>Configure navigation menu based on user role</li>
 *   <li>Handle navigation between views</li>
 *   <li>Highlight active navigation button</li>
 *   <li>Load views into content area dynamically</li>
 *   <li>Handle logout functionality</li>
 * </ul>
 * 
 * <p><b>Navigation Pattern:</b></p>
 * <ul>
 *   <li>Each nav button calls a show*() method</li>
 *   <li>show*() methods call loadView(viewName, button)</li>
 *   <li>loadView() uses SceneManager to load into contentArea</li>
 *   <li>Active button is highlighted</li>
 * </ul>
 * 
 * <p><b>Role-Based Access:</b></p>
 * <ul>
 *   <li>Admin: All menu items visible</li>
 *   <li>Assistant: "Update Requests" button hidden</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class MainLayoutController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());
    
    // ==================== FXML COMPONENTS ====================
    
    // Header
    @FXML private Label userInfoLabel;
    @FXML private Button logoutButton;
    
    // Sidebar Navigation
    @FXML private Button dashboardBtn;
    @FXML private Button studentsBtn;
    @FXML private Button lessonsBtn;
    @FXML private Button missionsBtn;
    @FXML private Button fasee7Btn;
    @FXML private Button warningsBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button reportsBtn;
    @FXML private Button updateRequestsBtn;
    @FXML private Button settingsBtn;
    
    // Content Area
    @FXML private StackPane contentArea;
    
    // ==================== STATE ====================
    
    /**
     * Currently active navigation button (for highlighting).
     */
    private Button currentActiveButton;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * No-arg constructor required for FXML instantiation.
     */
    public MainLayoutController() {
        super();
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Initialize method - called after FXML injection.
     * Configures UI based on logged-in user and loads default screen.
     */
    @Override
    public void initialize() {
        super.initialize(); // Initialize utilities
        
        LOGGER.info("MainLayout initialized");
        
        // Configure UI based on user role
        configureForUser();
        
        // Load default screen (Dashboard) after a short delay
        // This allows the layout to fully render first
        Platform.runLater(this::showDashboard);
    }
    
    // ==================== USER CONFIGURATION ====================
    
    /**
     * Configures UI based on the logged-in user's role.
     * Updates header info and shows/hides menu items.
     */
    private void configureForUser() {
        User currentUser = getCurrentUser();
        
        if (currentUser == null) {
            LOGGER.severe("No user logged in - should not reach MainLayout");
            handleLogout();
            return;
        }
        
        // Update header with user info
        String roleDisplay = currentUser.getRole().toString();
        userInfoLabel.setText(roleDisplay + ": " + currentUser.getFullName());
        
        LOGGER.info("Configured MainLayout for user: " + currentUser.getUsername() + 
                   " (Role: " + roleDisplay + ")");
        
        // Configure menu visibility based on role
        if (isAssistant()) {
            // Hide admin-only features for assistants
            updateRequestsBtn.setVisible(false);
            updateRequestsBtn.setManaged(false);
            LOGGER.fine("Hidden admin-only menu items for assistant");
        }
    }
    
    // ==================== LOGOUT ====================
    
    /**
     * Handles logout button click.
     * Clears session and returns to login screen.
     */
    @FXML
    private void handleLogout() {
        LOGGER.info("User logging out");
        
        // Optional: Confirm logout
        // boolean confirmed = confirm("Logout", "Are you sure you want to logout?");
        // if (!confirmed) return;
        
        try {
            // Clean up current view
            cleanup();
            
            // Clear session
            sessionManager.clearSession();
            
            // Return to login
            sceneManager.switchToLogin();
            
            LOGGER.info("Logout successful");
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Logout failed", e);
            showError("Failed to logout. Please try again.");
        }
    }
    
    // ==================== NAVIGATION METHODS ====================
    
    /**
     * Loads Dashboard view.
     */
    @FXML
    private void showDashboard() {
        loadView("Dashboard", dashboardBtn);
    }
    
    /**
     * Loads Student List view (placeholder for now).
     */
    @FXML
    private void showStudents() {
        loadView("StudentList", studentsBtn);
    }
    
    /**
     * Loads Lesson List view (placeholder for now).
     */
    @FXML
    private void showLessons() {
        showPlaceholder("Lessons", "Lesson List screen will be available soon");
        setActiveButton(lessonsBtn);
    }
    
    /**
     * Loads Mission List view (placeholder for now).
     */
    @FXML
    private void showMissions() {
        showPlaceholder("Missions", "Mission List screen will be available soon");
        setActiveButton(missionsBtn);
    }
    
    /**
     * Loads Fasee7 Table view (placeholder for now).
     */
    @FXML
    private void showFasee7() {
        showPlaceholder("Fasee7 Table", "Fasee7 Table screen will be available soon");
        setActiveButton(fasee7Btn);
    }
    
    /**
     * Loads Warning List view (placeholder for now).
     */
    @FXML
    private void showWarnings() {
        showPlaceholder("Warnings", "Warning List screen will be available soon");
        setActiveButton(warningsBtn);
    }
    
    /**
     * Loads Notification Center view (placeholder for now).
     */
    @FXML
    private void showNotifications() {
        showPlaceholder("Notifications", "Notification Center will be available soon");
        setActiveButton(notificationsBtn);
    }
    
    /**
     * Loads Monthly Reports view (placeholder for now).
     */
    @FXML
    private void showReports() {
        showPlaceholder("Reports", "Monthly Reports screen will be available soon");
        setActiveButton(reportsBtn);
    }
    
    /**
     * Loads Update Request Queue view (placeholder for now).
     * Admin only.
     */
    @FXML
    private void showUpdateRequests() {
        showPlaceholder("Update Requests", "Update Request Queue will be available soon");
        setActiveButton(updateRequestsBtn);
    }
    
    /**
     * Loads Settings view (placeholder for now).
     */
    @FXML
    private void showSettings() {
        showPlaceholder("Settings", "Settings screen will be available soon");
        setActiveButton(settingsBtn);
    }
    
    // ==================== VIEW LOADING ====================
    
    /**
     * Generic method to load a view into content area.
     * Cleans up previous view and highlights active button.
     * 
     * @param viewName Logical name of view (e.g., "Dashboard")
     * @param navButton Navigation button that was clicked
     */
    private void loadView(String viewName, Button navButton) {
        try {
            LOGGER.info("Loading view: " + viewName);
            
            // Load view into content area
            sceneManager.loadViewIntoContainer(viewName, contentArea);
            
            // Update active button styling
            setActiveButton(navButton);
            
            LOGGER.fine("View loaded successfully: " + viewName);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + viewName, e);
            showError("Failed to load " + viewName + ". Please try again.");
        } catch (IllegalArgumentException e) {
            // View doesn't exist in SceneManager mapping
            LOGGER.log(Level.WARNING, "View not found: " + viewName, e);
            showPlaceholder(viewName, viewName + " screen is not yet implemented");
            setActiveButton(navButton);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error loading view: " + viewName, e);
            showError("An unexpected error occurred");
        }
    }
    
    /**
     * Shows a placeholder alert for screens that don't exist yet.
     * 
     * @param screenName Name of the screen
     * @param message Message to display
     */
    private void showPlaceholder(String screenName, String message) {
        showInfo("Coming Soon", message);
    }
    
    /**
     * Highlights the active navigation button.
     * Removes highlight from previous button.
     * 
     * @param button Button to highlight (null to clear all)
     */
    private void setActiveButton(Button button) {
        // Remove active class from previous button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        
        // Add active class to new button
        if (button != null) {
            if (!button.getStyleClass().contains("nav-button-active")) {
                button.getStyleClass().add("nav-button-active");
            }
            currentActiveButton = button;
        }
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Cleanup method - called before navigation away from main layout.
     */
    @Override
    public void cleanup() {
        LOGGER.fine("Cleaning up MainLayout");
        // No event subscriptions to unregister for this controller
        super.cleanup();
    }
}