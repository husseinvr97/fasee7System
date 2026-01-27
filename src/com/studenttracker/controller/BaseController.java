package com.studenttracker.controller;

import com.studenttracker.model.User;
import com.studenttracker.util.SessionManager;
import com.studenttracker.util.SceneManager;
import com.studenttracker.util.AlertHelper;

/**
 * BaseController - Abstract parent class for all screen controllers.
 * 
 * <p>Provides common functionality that every controller needs:</p>
 * <ul>
 *   <li>Access to SessionManager for user information and permissions</li>
 *   <li>Access to SceneManager for navigation</li>
 *   <li>Convenient methods for alerts and dialogs</li>
 *   <li>Permission checking methods</li>
 * </ul>
 * 
 * <p><b>Usage:</b> All screen controllers must extend this class:</p>
 * <pre>
 * public class StudentListController extends BaseController {
 *     @FXML
 *     public void initialize() {
 *         User user = getCurrentUser();
 *         if (isAdmin()) {
 *             // Enable admin features
 *         }
 *     }
 * }
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-27
 */
public abstract class BaseController {
    
    /**
     * SessionManager instance - accessible by all child controllers.
     * Provides access to current user and permission checking.
     */
    protected SessionManager sessionManager;
    
    /**
     * SceneManager instance - accessible by all child controllers.
     * Provides navigation and dialog management.
     */
    protected SceneManager sceneManager;
    
    /**
     * Constructor - automatically initializes utility instances.
     * Called by child controller constructors.
     */
    public BaseController() {
        this.sessionManager = SessionManager.getInstance();
        this.sceneManager = SceneManager.getInstance();
    }
    
    // ==================== User Session Methods ====================
    
    /**
     * Get the currently logged-in user.
     * 
     * @return the current User object, or null if no user is logged in
     */
    protected User getCurrentUser() {
        return sessionManager.getCurrentUser();
    }
    
    /**
     * Check if the current user has ADMIN role.
     * 
     * @return true if current user is an admin, false otherwise
     */
    protected boolean isAdmin() {
        return sessionManager.isAdmin();
    }
    
    /**
     * Check if the current user has ASSISTANT role.
     * 
     * @return true if current user is an assistant, false otherwise
     */
    protected boolean isAssistant() {
        return sessionManager.isAssistant();
    }
    
    // ==================== Alert Methods ====================
    
    /**
     * Show an error alert to the user.
     * Use for validation errors, operation failures, and exceptions.
     * 
     * @param message the error message to display
     */
    protected void showError(String message) {
        AlertHelper.showError(message);
    }
    
    /**
     * Show a success alert to the user.
     * Use to confirm successful operations.
     * 
     * @param message the success message to display
     */
    protected void showSuccess(String message) {
        AlertHelper.showSuccess(message);
    }
    
    /**
     * Show a warning alert to the user.
     * Use for non-critical issues that user should be aware of.
     * 
     * @param message the warning message to display
     */
    protected void showWarning(String message) {
        AlertHelper.showWarning(message);
    }
    
    /**
     * Show a confirmation dialog to the user.
     * Use before performing destructive or important operations.
     * 
     * @param title the title of the confirmation dialog
     * @param message the confirmation question to display
     * @return true if user clicked OK/Yes, false if user clicked Cancel/No
     */
    protected boolean confirm(String title, String message) {
        return AlertHelper.showConfirmation(title, message);
    }
    
    // ==================== Navigation Methods ====================
    
    /**
     * Navigate to another screen by switching the entire scene.
     * 
     * @param fxmlPath the path to the FXML file (e.g., "/view/students.fxml")
     */
    protected void navigateTo(String fxmlPath) {
        sceneManager.switchScene(fxmlPath);
    }
    
    /**
     * Show a modal dialog window.
     * The main window will be blocked until the dialog is closed.
     * 
     * @param fxmlPath the path to the dialog FXML file
     * @param title the title for the dialog window
     */
    protected void showDialog(String fxmlPath, String title) {
        sceneManager.showDialog(fxmlPath, title);
    }
}