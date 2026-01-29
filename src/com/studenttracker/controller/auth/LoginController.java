package com.studenttracker.controller.auth;

import com.studenttracker.controller.BaseController;
import com.studenttracker.exception.InvalidCredentialsException;
import com.studenttracker.model.User;
import com.studenttracker.service.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Login screen.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Authenticate users via UserService</li>
 *   <li>Handle "Remember Me" functionality (saves username only)</li>
 *   <li>Implement failed attempt tracking (3 strikes = lockout)</li>
 *   <li>Navigate to MainLayout on successful login</li>
 *   <li>Store authenticated user in SessionManager</li>
 * </ul>
 * 
 * <p><b>Security Features:</b></p>
 * <ul>
 *   <li>Password never stored locally</li>
 *   <li>3 failed attempts = 3 second lockout</li>
 *   <li>Generic error message (doesn't reveal if username exists)</li>
 *   <li>Remember Me only saves username, not password</li>
 * </ul>
 * 
 * <p><b>User Experience:</b></p>
 * <ul>
 *   <li>Enter key in password field triggers login</li>
 *   <li>Auto-focus on appropriate field</li>
 *   <li>Clear error messages</li>
 *   <li>Disabled button during authentication</li>
 * </ul>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class LoginController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    
    // ==================== CONSTANTS ====================
    
    /**
     * File path for storing remembered username.
     * Located in user's home directory for persistence.
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
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    
    // ==================== SERVICES ====================
    
    private UserService userService;
    
    // ==================== STATE ====================
    
    /**
     * Counter for failed login attempts.
     * Resets on successful login or after lockout period.
     */
    private int failedAttempts = 0;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor - initializes services via ServiceLocator.
     */
    public LoginController() {
        super();
    }
    
    // ==================== LIFECYCLE ====================
    
    /**
     * Initialize method - called after FXML injection.
     * Loads remembered username if exists and sets focus.
     */
    @Override
    public void initialize() {
        super.initialize();
        this.userService = serviceLocator.getUserService();
        // Load remembered username if "Remember Me" was checked previously
        loadRememberedUsername();
        
        // Set focus on appropriate field
        Platform.runLater(() -> {
            if (usernameField.getText().isEmpty()) {
                usernameField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
        });
        
        LOGGER.info("Login screen initialized");
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handles login button click or Enter key in password field.
     * Validates input, authenticates user, and navigates on success.
     */
    @FXML
    private void handleLogin() {
        // Clear previous error
        hideError();
        
        // Validate inputs
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty()) {
            showError("Please enter your username");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter your password");
            passwordField.requestFocus();
            return;
        }
        
        // Disable login button during authentication
        loginButton.setDisable(true);
        
        // Authenticate (asynchronous to keep UI responsive)
        authenticateUser(username, password);
    }
    
    // ==================== AUTHENTICATION ====================
    
    /**
     * Authenticates user credentials via UserService.
     * 
     * @param username Username entered by user
     * @param password Password entered by user
     */
    private void authenticateUser(String username, String password) {
        try {
            // Attempt authentication
            User user = userService.login(username, password);
            
            // Success!
            onLoginSuccess(user, username);
            
        } catch (InvalidCredentialsException e) {
            // Invalid credentials
            onLoginFailure("Invalid username or password");
            
        } catch (Exception e) {
            // Unexpected error
            LOGGER.log(Level.SEVERE, "Login error", e);
            onLoginFailure("Login failed. Please try again.");
            
        } finally {
            // Re-enable login button
            loginButton.setDisable(false);
        }
    }
    
    /**
     * Handles successful login.
     * Stores user in session, handles Remember Me, and navigates to MainLayout.
     * 
     * @param user Authenticated user object
     * @param username Username that was used (for Remember Me)
     */
    private void onLoginSuccess(User user, String username) {
        LOGGER.info("User logged in successfully: " + username);
        
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
            sceneManager.switchToMainLayout();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load main layout", e);
            showError("Failed to load application. Please restart.");
        }
    }
    
    /**
     * Handles failed login attempt.
     * Increments counter, shows error, and locks out after 3 failed attempts.
     * 
     * @param message Error message to display to user
     */
    private void onLoginFailure(String message) {
        failedAttempts++;
        
        LOGGER.warning("Failed login attempt " + failedAttempts + "/" + MAX_FAILED_ATTEMPTS);
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // Lockout after 3 failed attempts
            lockoutUser();
        } else {
            // Show error and clear password
            showError(message);
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    /**
     * Locks out user for LOCKOUT_SECONDS after too many failed attempts.
     * Re-enables login after lockout period.
     */
    private void lockoutUser() {
        loginButton.setDisable(true);
        showError("Too many failed attempts. Please wait " + LOCKOUT_SECONDS + " seconds.");
        
        // Re-enable after lockout period (using background thread)
        new Thread(() -> {
            try {
                Thread.sleep(LOCKOUT_SECONDS * 1000);
                
                // Re-enable login button on JavaFX thread
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    failedAttempts = 0;
                    showError("You can try logging in again");
                    passwordField.requestFocus();
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Lockout timer interrupted", e);
            }
        }).start();
    }
    
    // ==================== REMEMBER ME FUNCTIONALITY ====================
    
    /**
     * Loads remembered username from file if it exists.
     * Sets username field and checks Remember Me checkbox.
     */
    private void loadRememberedUsername() {
        try {
            Path path = Paths.get(REMEMBER_ME_FILE);
            
            if (Files.exists(path)) {
                String username = Files.readString(path).trim();
                
                if (!username.isEmpty()) {
                    usernameField.setText(username);
                    rememberMeCheckbox.setSelected(true);
                    LOGGER.fine("Loaded remembered username: " + username);
                }
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load remembered username", e);
            // Not critical - just continue without remembered username
        }
    }
    
    /**
     * Saves username to file for "Remember Me" functionality.
     * Creates directory if it doesn't exist.
     * 
     * @param username Username to save
     */
    private void saveRememberedUsername(String username) {
        try {
            Path path = Paths.get(REMEMBER_ME_FILE);
            
            // Create directory if it doesn't exist
            Files.createDirectories(path.getParent());
            
            // Write username to file
            Files.writeString(path, username);
            
            LOGGER.fine("Saved remembered username: " + username);
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save remembered username", e);
            // Not critical - just continue
        }
    }
    
    /**
     * Clears remembered username file.
     * Called when Remember Me is unchecked.
     */
    private void clearRememberedUsername() {
        try {
            Path path = Paths.get(REMEMBER_ME_FILE);
            Files.deleteIfExists(path);
            LOGGER.fine("Cleared remembered username");
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to clear remembered username", e);
            // Not critical
        }
    }
    
    // ==================== UI HELPERS ====================
    
    /**
     * Shows error message to user.
     * Makes error label visible with the given message.
     * 
     * @param message Error message to display
     */
    protected void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /**
     * Hides error message.
     * Makes error label invisible.
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Cleanup method - called before navigation away from login screen.
     * No event subscriptions to unregister for this controller.
     */
    @Override
    public void cleanup() {
        // No cleanup needed for login screen
        super.cleanup();
    }
}