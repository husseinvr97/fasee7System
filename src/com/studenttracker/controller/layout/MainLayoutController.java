package com.studenttracker.controller.layout;

import com.studenttracker.model.User;
import com.studenttracker.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controller for the main application layout.
 * Manages navigation between different views and user session display.
 * 
 * TODO: Implement all navigation methods when respective FXML views are created
 * TODO: Integrate with SceneManager for proper scene transitions
 * TODO: Add proper error handling and logging
 * TODO: Implement role-based UI adjustments (Admin vs Teacher)
 */
public class MainLayoutController {

    @FXML private Label userInfoLabel;
    @FXML private Button logoutButton;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    
    // Navigation Buttons
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
    
    private Button currentActiveButton;
    private SessionManager sessionManager;

    /**
     * Initialize the controller after FXML loading.
     * Sets up user session display and loads default view.
     */
    @FXML
    public void initialize() {
        loadUserSession();
        applyRoleBasedUI();
        sessionManager = SessionManager.getInstance();
        
        // TODO: Load Dashboard view by default
        // showDashboard();
    }

    /**
     * Loads current user session and displays user info in header.
     */
    private void loadUserSession() {
        // TODO: Integrate with SessionManager
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            String displayText = String.format("User: %s (%s)", 
                currentUser.getFullName(), 
                currentUser.getRole());
            userInfoLabel.setText(displayText);
        } else {
            userInfoLabel.setText("User: Unknown");
            // TODO: Handle case where no user is logged in
        }
    }

    /**
     * Applies role-based UI adjustments.
     * Shows/hides elements based on user role (Admin vs Teacher).
     */
    private void applyRoleBasedUI() {
        // TODO: Integrate with SessionManager to check user role
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole().name());
            
            // Admin-only elements
            updateRequestsBtn.setVisible(isAdmin);
            updateRequestsBtn.setManaged(isAdmin);
        }
    }

    /**
     * Handles logout action.
     * Clears session and returns to login screen.
     */
    @FXML
    private void handleLogout() {
        // TODO: Integrate with SessionManager.logout()
        // TODO: Integrate with SceneManager to navigate to Login.fxml
        System.out.println("Logout clicked - TODO: Implement logout logic");
        
        // SessionManager.logout();
        // SceneManager.switchScene("view/fxml/auth/Login.fxml");
    }

    // ==================== NAVIGATION METHODS ====================
    
    /**
     * Loads Dashboard view into content area.
     */
    @FXML
    private void showDashboard() {
        // TODO: Implement when Dashboard.fxml is created
        loadView("view/fxml/dashboard/Dashboard.fxml", dashboardBtn);
    }

    /**
     * Loads Student List view into content area.
     */
    @FXML
    private void showStudents() {
        // TODO: Implement when StudentList.fxml is created
        loadView("view/fxml/student/StudentList.fxml", studentsBtn);
    }

    /**
     * Loads Lesson List view into content area.
     */
    @FXML
    private void showLessons() {
        // TODO: Implement when LessonList.fxml is created
        loadView("view/fxml/lesson/LessonList.fxml", lessonsBtn);
    }

    /**
     * Loads Mission List view into content area.
     */
    @FXML
    private void showMissions() {
        // TODO: Implement when MissionList.fxml is created
        loadView("view/fxml/mission/MissionList.fxml", missionsBtn);
    }

    /**
     * Loads Fasee7 Table view into content area.
     */
    @FXML
    private void showFasee7() {
        // TODO: Implement when Fasee7Table.fxml is created
        loadView("view/fxml/fasee7/Fasee7Table.fxml", fasee7Btn);
    }

    /**
     * Loads Warning List view into content area.
     */
    @FXML
    private void showWarnings() {
        // TODO: Implement when WarningList.fxml is created
        loadView("view/fxml/warning/WarningList.fxml", warningsBtn);
    }

    /**
     * Loads Notification Center view into content area.
     */
    @FXML
    private void showNotifications() {
        // TODO: Implement when NotificationCenter.fxml is created
        loadView("view/fxml/notification/NotificationCenter.fxml", notificationsBtn);
    }

    /**
     * Loads Monthly Reports view into content area.
     */
    @FXML
    private void showReports() {
        // TODO: Implement when MonthlyReports.fxml is created
        loadView("view/fxml/report/MonthlyReports.fxml", reportsBtn);
    }

    /**
     * Loads Update Request Queue view into content area (Admin only).
     */
    @FXML
    private void showUpdateRequests() {
        // TODO: Implement when UpdateRequestQueue.fxml is created
        loadView("view/fxml/updaterequest/UpdateRequestQueue.fxml", updateRequestsBtn);
    }

    /**
     * Loads Settings view into content area.
     */
    @FXML
    private void showSettings() {
        // TODO: Implement when Settings.fxml is created (placeholder for now)
        loadView("view/fxml/settings/Settings.fxml", settingsBtn);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generic method to load FXML views into the content area.
     * 
     * @param fxmlPath Path to the FXML file (relative to resources root)
     * @param activeButton The navigation button that triggered this view
     */
    private void loadView(String fxmlPath, Button activeButton) {
        try {
            // TODO: Implement proper FXML loading logic
            System.out.println("Loading view: " + fxmlPath);
            
            // FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
            // Parent view = loader.load();
            // contentArea.getChildren().clear();
            // contentArea.getChildren().add(view);
            
            setActiveButton(activeButton);
            
        } catch (Exception e) {
            // TODO: Add proper error handling and user feedback
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Sets the active navigation button and removes active style from others.
     * 
     * @param activeButton The button to mark as active
     */
    private void setActiveButton(Button activeButton) {
        // Remove active class from previous button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
        }
        
        // Add active class to new button
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
        
        currentActiveButton = activeButton;
    }
}