package com.studenttracker.util;

import com.studenttracker.model.User;

/**
 * SessionManager - Singleton class for managing the current user session.
 * 
 * <p>This class maintains information about the currently logged-in user throughout
 * the application lifecycle. It provides centralized access to user information and
 * role-based authorization checks.</p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Store and retrieve the current logged-in user</li>
 *   <li>Provide session state checks (logged in/out)</li>
 *   <li>Offer convenient role-based authorization methods</li>
 *   <li>Handle session cleanup on logout</li>
 * </ul>
 * 
 * <p><b>Usage Example - Login:</b></p>
 * <pre>
 * User user = userService.login(username, password);
 * SessionManager.getInstance().setCurrentUser(user);
 * </pre>
 * 
 * <p><b>Usage Example - Role Check:</b></p>
 * <pre>
 * if (SessionManager.getInstance().isAdmin()) {
 *     // Show admin features
 * }
 * </pre>
 * 
 * <p><b>Usage Example - Logout:</b></p>
 * <pre>
 * SessionManager.getInstance().clearSession();
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-27
 */
public class SessionManager {
    
    /**
     * Singleton instance - only one SessionManager exists in the application.
     */
    private static SessionManager instance;
    
    /**
     * The currently logged-in user. Null if no user is logged in.
     */
    private User currentUser;
    
    /**
     * Private constructor to prevent external instantiation.
     * Enforces singleton pattern.
     */
    private SessionManager() {
        this.currentUser = null;
    }
    
    /**
     * Gets the singleton instance of SessionManager.
     * Creates the instance on first call (lazy initialization).
     * 
     * <p><b>Thread Safety Note:</b> This implementation is not thread-safe.
     * If multi-threading is required, synchronization should be added.</p>
     * 
     * @return the singleton SessionManager instance
     */
    public synchronized static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Sets the current logged-in user.
     * Called after successful authentication.
     * 
     * @param user the authenticated user to set as current user
     * @throws IllegalArgumentException if user is null
     */
    public void setCurrentUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null. Use clearSession() to logout.");
        }
        this.currentUser = user;
    }
    
    /**
     * Gets the current logged-in user.
     * 
     * @return the current user, or null if no user is logged in
     */
    public User getCurrentUser() {
        return this.currentUser;
    }
    
    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }
    
    /**
     * Clears the current session by removing the logged-in user.
     * Called during logout process.
     * 
     * <p>After calling this method, isLoggedIn() will return false
     * and getCurrentUser() will return null.</p>
     */
    public synchronized void clearSession() {
        this.currentUser = null;
    }
    
    /**
     * Checks if the current user has ADMIN role.
     * 
     * @return true if current user is an admin, false if no user is logged in
     *         or if user has a different role
     */
    public boolean isAdmin() {
        return currentUser != null && 
               currentUser.getRole() == User.UserRole.ADMIN;
    }
    
    /**
     * Checks if the current user has ASSISTANT role.
     * 
     * @return true if current user is an assistant, false if no user is logged in
     *         or if user has a different role
     */
    public boolean isAssistant() {
        return currentUser != null && 
               currentUser.getRole() == User.UserRole.ASSISTANT;
    }
    
    /**
     * Gets the full name of the current user.
     * Convenience method to avoid null checks in calling code.
     * 
     * @return the full name of current user, or "Guest" if no user is logged in
     */
    public String getCurrentUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "Guest";
    }
    
    /**
     * Gets the user ID of the current user.
     * Convenience method to avoid null checks in calling code.
     * 
     * @return the user ID of current user, or null if no user is logged in
     */
    public Integer getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : null;
    }
    
    /**
     * Resets the singleton instance.
     * <b>WARNING:</b> This method should only be used for testing purposes.
     * 
     * <p>Clears the session and destroys the singleton instance,
     * allowing a fresh instance to be created on next getInstance() call.</p>
     */
    protected synchronized static void resetInstance() {
        if (instance != null) {
            instance.clearSession();
            instance = null;
        }
    }
}
