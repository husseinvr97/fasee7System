package com.studenttracker.controller;

import com.studenttracker.model.User;
import com.studenttracker.util.AlertHelper;
import com.studenttracker.util.SceneManager;
import com.studenttracker.util.ServiceLocator;
import com.studenttracker.util.SessionManager;

/**
 * BaseController - Abstract parent class for all screen controllers.
 * 
 * <p><b>Design Pattern:</b> Template Method Pattern</p>
 * 
 * <p>Provides common functionality that every controller needs:</p>
 * <ul>
 *   <li>Access to SessionManager for user information and permissions</li>
 *   <li>Access to SceneManager for navigation</li>
 *   <li>Access to ServiceLocator for business logic services</li>
 *   <li>Lifecycle management (initialize/cleanup hooks)</li>
 *   <li>Convenient methods for alerts and dialogs</li>
 *   <li>Permission checking methods</li>
 * </ul>
 * 
 * <p><b>Lifecycle:</b></p>
 * <ol>
 *   <li>Constructor called - utilities initialized</li>
 *   <li>FXML injection occurs (JavaFX)</li>
 *   <li>initialize() called - setup UI and load data</li>
 *   <li>Controller active - user interactions</li>
 *   <li>cleanup() called - release resources before navigation</li>
 * </ol>
 * 
 * <p><b>Usage Example:</b></p>
 * <pre>
 * public class StudentListController extends BaseController {
 *     private StudentService studentService;
 *     
 *     public StudentListController() {
 *         super();
 *         this.studentService = serviceLocator.getStudentService();
 *     }
 *     
 *     @Override
 *     public void initialize() {
 *         super.initialize(); // Always call super first!
 *         
 *         if (isAdmin()) {
 *             // Enable admin features
 *         }
 *         loadStudentData();
 *     }
 *     
 *     @Override
 *     public void cleanup() {
 *         // Unregister from EventBus, cancel timers, etc.
 *         super.cleanup(); // Always call super last!
 *     }
 * }
 * </pre>
 * 
 * @author fasee7System
 * @version 2.0.0
 * @since 2026-01-28
 */
public abstract class BaseController {
    
    // ==================== PROTECTED UTILITIES ====================
    
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
     * ServiceLocator instance - accessible by all child controllers.
     * Provides access to all business logic services.
     */
    protected ServiceLocator serviceLocator;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor - automatically initializes utility instances.
     * Called by child controller constructors.
     * 
     * <p>Child classes should call super() first, then initialize their services:</p>
     * <pre>
     * public MyController() {
     *     super();
     *     this.myService = serviceLocator.getMyService();
     * }
     * </pre>
     */
    public BaseController() {
        sessionManager = SessionManager.getInstance();
        sceneManager = SceneManager.getInstance();
        serviceLocator = ServiceLocator.getInstance();
    }

    /**
     * Ensures utilities are initialized.
     * Called automatically by getter methods.
     * This handles the fact that FXML controllers are created via reflection.
     */
    private void ensureInitialized() {
        if (sessionManager == null) {
            sessionManager = SessionManager.getInstance();
        }
        if (sceneManager == null) {
            sceneManager = SceneManager.getInstance();
        }
        if (serviceLocator == null) {
            serviceLocator = ServiceLocator.getInstance();
        }
    }
    
    // ==================== LIFECYCLE METHODS (Template Method Pattern) ====================
    
    /**
     * Initialize method - called by JavaFX after FXML injection.
     * 
     * <p><b>Template Method Pattern:</b> This method can be overridden by subclasses
     * to provide custom initialization logic.</p>
     * 
     * <p><b>Override Pattern:</b></p>
     * <pre>
     * @Override
     * public void initialize() {
     *     super.initialize(); // Call super first!
     *     
     *     // Your custom initialization here
     *     loadData();
     *     setupEventListeners();
     * }
     * </pre>
     * 
     * <p><b>Default Behavior:</b> Does nothing - safe to override without calling super
     * if no base initialization is needed.</p>
     */
    public void initialize() {
        // Default: No base initialization needed
        // Child classes override to add their own initialization
        ensureInitialized();
    }
    
    /**
     * Cleanup method - called before controller is destroyed or replaced.
     * 
     * <p><b>Use Cases:</b></p>
     * <ul>
     *   <li>Unregister from EventBus</li>
     *   <li>Cancel scheduled timers</li>
     *   <li>Close file handles</li>
     *   <li>Release other resources</li>
     * </ul>
     * 
     * <p><b>Override Pattern:</b></p>
     * <pre>
     * @Override
     * public void cleanup() {
     *     // Your cleanup here
     *     eventBus.unregister(this);
     *     timer.cancel();
     *     
     *     super.cleanup(); // Call super last!
     * }
     * </pre>
     * 
     * <p><b>Important:</b> SceneManager should call this before switching views.</p>
     */
    public void cleanup() {
        // Default: No base cleanup needed
        // Child classes override to add their own cleanup
    }
    
    /**
     * Lifecycle hook called after view is loaded but before shown.
     * 
     * <p>This is useful for:</p>
     * <ul>
     *   <li>Final UI adjustments</li>
     *   <li>Triggering animations</li>
     *   <li>Setting initial focus</li>
     * </ul>
     * 
     * <p><b>Note:</b> This is not automatically called by JavaFX.
     * SceneManager must explicitly invoke it if needed.</p>
     */
    protected void onViewLoaded() {
        // Optional hook - child classes can override
    }
    
    // ==================== USER SESSION METHODS ====================
    
    /**
     * Get the currently logged-in user.
     * 
     * @return the current User object, or null if no user is logged in
     */
    protected User getCurrentUser() {
        ensureInitialized();
        return sessionManager.getCurrentUser();
    }
    
    /**
     * Get the ID of the currently logged-in user.
     * Convenience method to avoid null checks.
     * 
     * @return the current user's ID, or null if no user is logged in
     */
    protected Integer getCurrentUserId() {
        ensureInitialized();
        return sessionManager.getCurrentUserId();
    }
    
    /**
     * Get the full name of the currently logged-in user.
     * Convenience method to avoid null checks.
     * 
     * @return the current user's full name, or "Guest" if no user is logged in
     */
    protected String getCurrentUserFullName() {
        ensureInitialized();
        return sessionManager.getCurrentUserFullName();
    }
    
    /**
     * Check if the current user has ADMIN role.
     * 
     * @return true if current user is an admin, false otherwise
     */
    protected boolean isAdmin() {
        ensureInitialized();
        return sessionManager.isAdmin();
    }
    
    /**
     * Check if the current user has ASSISTANT role.
     * 
     * @return true if current user is an assistant, false otherwise
     */
    protected boolean isAssistant() {
        ensureInitialized();
        return sessionManager.isAssistant();
    }
    
    /**
     * Check if a user is currently logged in.
     * 
     * @return true if logged in, false otherwise
     */
    protected boolean isLoggedIn() {
        ensureInitialized();
        return sessionManager.isLoggedIn();
    }
    
    // ==================== ALERT METHODS ====================
    
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
     * Show an info alert to the user.
     * Use for informational messages.
     * 
     * @param message the info message to display
     */
    protected void showInfo( String title, String message) {
        AlertHelper.showInfo(title , message);
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
    
    // ==================== NAVIGATION METHODS ====================
    
    /**
     * Navigate to another screen by switching the entire scene.
     * Automatically calls cleanup() before navigation.
     * 
     * @param fxmlPath the path to the FXML file (e.g., "/view/students.fxml")
     */
    protected void navigateTo(String fxmlPath) {
        cleanup();
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

    protected SessionManager getSessionManager() {
        ensureInitialized();
        return sessionManager;
    }
    
    /**
     * Gets SceneManager instance (with lazy initialization).
     */
    protected SceneManager getSceneManager() {
        ensureInitialized();
        return sceneManager;
    }
    
    /**
     * Gets ServiceLocator instance (with lazy initialization).
     */
    protected ServiceLocator getServiceLocator() {
        ensureInitialized();
        return serviceLocator;
    }
}