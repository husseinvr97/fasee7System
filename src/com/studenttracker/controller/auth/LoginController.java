package com.studenttracker.controller.auth;

import com.studenttracker.controller.BaseController;
import com.studenttracker.model.User;
import com.studenttracker.service.UserService;
import com.studenttracker.util.ServiceLocator;
import com.studenttracker.util.SessionManager;
import com.studenttracker.util.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoginController - Handles user authentication and login functionality.
 * 
 * <p>This controller manages the login screen, including:</p>
 * <ul>
 *   <li>User credential validation and authentication</li>
 *   <li>"Remember Me" functionality (username only, never password)</li>
 *   <li>Failed login attempt tracking with lockout mechanism</li>
 *   <li>Session management and navigation to main application</li>
 * </ul>
 * 
 * <p><b>Security Features:</b></p>
 * <ul>
 *   <li>3 failed attempts trigger a 3-second lockout</li>
 *   <li>Password is never stored locally</li>
 *   <li>Only username is saved for "Remember Me"</li>
 *   <li>Input validation before authentication</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-27
 */
public class LoginController extends BaseController {
    
    // ==================== Constants ====================
    
    /**
     * Logger for login-related events and errors.
     */
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    
    /**
     * File path for storing remembered username.
     * Located in user's home directory: ~/.fasee7/remembered_username.txt
     */
    private static final String REMEMBER_ME_FILE = System.getProperty("user.home") + 
                                                   "/.fasee7/remembered_username.txt";
    
    /**
     * Maximum number of failed login attempts before lockout.
     */
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    /**
     * Lockout duration in seconds after max failed attempts.
     */
    private static final int LOCKOUT_SECONDS = 3;
    
    // ==================== FXML Components ====================
    
    /**
     * Text field for username input.
     */
    @FXML 
    private TextField usernameField;
    
    /**
     * Password field for password input (masked).
     */
    @FXML 
    private PasswordField passwordField;
    
    /**
     * Checkbox for "Remember Me" functionality.
     */
    @FXML 
    private CheckBox rememberMeCheckbox;
    
    /**
     * Label for displaying error messages to the user.
     */
    @FXML 
    private Label errorLabel;
    
    /**
     * Login button - disabled during authentication and lockout.
     */
    @FXML 
    private Button loginButton;
    
    // ==================== Services ====================
    
    /**
     * UserService for authentication operations.
     */
    private final UserService userService;
    
    // Note: sessionManager and sceneManager inherited from BaseController
    
    // ==================== State ====================
    
    /**
     * Counter for tracking consecutive failed login attempts.
     * Resets to 0 on successful login or after lockout expires.
     */
    private int failedAttempts = 0;
    
    // ==================== Constructor ====================
    
    /**
     * Constructor - Initializes services via ServiceLocator.
     * Called automatically by JavaFX when loading the FXML.
     */
    public LoginController() {
        super(); // Initialize BaseController (sessionManager, sceneManager)
        this.userService = ServiceLocator.getInstance().getUserService();
    }
    
    // ==================== Initialization ====================
    
    /**
     * Initialize method called automatically after FXML loading.
     * Sets up the login screen with remembered username if available.
     */
    @FXML
    public void initialize() {
        // Load remembered username if exists
        loadRememberedUsername();
        
        // Focus on appropriate field after UI is ready
        Platform.runLater(() -> {
            if (usernameField.getText().isEmpty()) {
                usernameField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        });
    }
    
    // ==================== Event Handlers ====================
    
    /**
     * Handle login button click or Enter key in password field.
     * Validates inputs, authenticates user, and handles success/failure.
     */
    @FXML
    private void handleLogin() {
        // Clear previous error
        hideError();
        
        // Validate inputs
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty()) {
            showError("Please enter username");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter password");
            passwordField.requestFocus();
            return;
        }
        
        // Disable login button during authentication
        loginButton.setDisable(true);
        
        // Authenticate
        try {
            User user = userService.login(username, password);
            
            if (user != null) {
                // Success
                onLoginSuccess(user, username);
            } else {
                // Invalid credentials
                onLoginFailure("Invalid username or password");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Login error", e);
            onLoginFailure("Login failed: " + e.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }
    
    // ==================== Login Success/Failure Handling ====================
    
    /**
     * Handle successful login.
     * Resets failed attempts, stores user in session, handles Remember Me,
     * and navigates to main application.
     * 
     * @param user the authenticated User object
     * @param username the username entered (for Remember Me)
     */
    private void onLoginSuccess(User user, String username) {
        // Reset failed attempts
        failedAttempts = 0;
        
        // Store user in session
        sessionManager.setCurrentUser(user);
        
        // Handle "Remember Me"
        if (rememberMeCheckbox.isSelected()) {
            saveRememberedUsername(username);
        } else {
            clearRememberedUsername();
        }
        
        // Navigate to main layout
        try {
            // TODO: Update this path if main layout FXML is in a different location
            sceneManager.switchScene("/view/MainLayout.fxml");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load main layout", e);
            showError("Failed to load application: " + e.getMessage());
        }
    }
    
    /**
     * Handle failed login.
     * Tracks failed attempts and triggers lockout after 3 failures.
     * 
     * @param message the error message to display
     */
    private void onLoginFailure(String message) {
        failedAttempts++;
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // Lockout after 3 failed attempts
            loginButton.setDisable(true);
            showError("Too many failed attempts. Please wait " + LOCKOUT_SECONDS + " seconds.");
            
            // Re-enable after lockout period
            new Thread(() -> {
                try {
                    Thread.sleep(LOCKOUT_SECONDS * 1000);
                    Platform.runLater(() -> {
                        loginButton.setDisable(false);
                        failedAttempts = 0;
                        showError("Please try again");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Lockout thread interrupted", e);
                }
            }).start();
            
        } else {
            // Show error message
            showError(message);
            passwordField.clear();
            passwordField.requestFocus();
            
            // Optional: Shake animation (can implement later)
            // shakeLoginForm();
        }
    }
    
    // ==================== Remember Me File Handling ====================
    
    /**
     * Load remembered username from file.
     * If file exists and contains a username, pre-fills the username field
     * and checks the "Remember Me" checkbox.
     */
    private void loadRememberedUsername() {
        try {
            Path path = Paths.get(REMEMBER_ME_FILE);
            if (Files.exists(path)) {
                String username = Files.readString(path).trim();
                if (!username.isEmpty()) {
                    usernameField.setText(username);
                    rememberMeCheckbox.setSelected(true);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load remembered username", e);
            // Don't show error to user - this is not critical
        }
    }
    
    /**
     * Save username to file for "Remember Me" functionality.
     * Creates the ~/.fasee7/ directory if it doesn't exist.
     * 
     * @param username the username to save
     */
    private void saveRememberedUsername(String username) {
        try {
            Path path = Paths.get(REMEMBER_ME_FILE);
            Files.createDirectories(path.getParent());
            Files.writeString(path, username);
            LOGGER.log(Level.INFO, "Username remembered for next login");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save remembered username", e);
            // Don't show error to user - this is not critical
        }
    }
    
    /**
     * Clear remembered username file.
     * Called when user unchecks "Remember Me" and logs in.
     */
    private void clearRememberedUsername() {
        try {
            Path path = Paths.get(REMEMBER_ME_FILE);
            Files.deleteIfExists(path);
            LOGGER.log(Level.INFO, "Remembered username cleared");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to clear remembered username", e);
            // Don't show error to user - this is not critical
        }
    }
    
    // ==================== Error Display Methods ====================
    
    /**
     * Show error message to user.
     * Makes the error label visible and displays the message.
     * 
     * @param message the error message to display
     */
    @Override
    protected void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /**
     * Hide error message.
     * Makes the error label invisible and removes it from layout.
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    // ==================== Cleanup ====================
    
    /**
     * Cleanup method called when controller is destroyed.
     * No event subscriptions to unregister for LoginController.
     */
    public void cleanup() {
        // No event subscriptions to unregister
        // LoginController doesn't use EventBus
    }
}