package com.studenttracker.util;

import com.studenttracker.service.UserService;

/**
 * ServiceLocator - Singleton class for centralized service management.
 * 
 * <p>Provides a central registry for accessing service instances throughout
 * the application. This pattern decouples controllers from service implementations
 * and simplifies dependency management.</p>
 * 
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *   <li>Store and provide access to service instances</li>
 *   <li>Initialize services during application startup</li>
 *   <li>Ensure consistent service instances across the application</li>
 * </ul>
 * 
 * <p><b>Usage Example - Initialization (in Main class):</b></p>
 * <pre>
 * UserService userService = new UserServiceImpl();
 * ServiceLocator.getInstance().setUserService(userService);
 * </pre>
 * 
 * <p><b>Usage Example - Retrieval (in Controllers):</b></p>
 * <pre>
 * UserService userService = ServiceLocator.getInstance().getUserService();
 * User user = userService.login(username, password);
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-27
 */
public class ServiceLocator {
    
    /**
     * Singleton instance - only one ServiceLocator exists in the application.
     */
    private static ServiceLocator instance;
    
    /**
     * The UserService instance for user authentication and management.
     */
    private UserService userService;
    
    /**
     * Private constructor to prevent external instantiation.
     * Enforces singleton pattern.
     */
    private ServiceLocator() {
        // Services will be set via setters during application initialization
    }
    
    /**
     * Gets the singleton instance of ServiceLocator.
     * Creates the instance on first call (lazy initialization).
     * 
     * <p><b>Thread Safety Note:</b> This implementation is not thread-safe.
     * If multi-threading is required, synchronization should be added.</p>
     * 
     * @return the singleton ServiceLocator instance
     */
    public static ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }
    
    /**
     * Gets the UserService instance.
     * 
     * @return the UserService instance
     * @throws IllegalStateException if UserService has not been set
     */
    public UserService getUserService() {
        if (userService == null) {
            throw new IllegalStateException(
                "UserService has not been initialized. " +
                "Call setUserService() during application startup."
            );
        }
        return userService;
    }
    
    /**
     * Sets the UserService instance.
     * Should be called once during application initialization.
     * 
     * @param userService the UserService implementation to use
     * @throws IllegalArgumentException if userService is null
     */
    public void setUserService(UserService userService) {
        if (userService == null) {
            throw new IllegalArgumentException("UserService cannot be null");
        }
        this.userService = userService;
    }
    
    /**
     * Resets the singleton instance and clears all services.
     * <b>WARNING:</b> This method should only be used for testing purposes.
     * 
     * <p>Destroys the singleton instance, allowing a fresh instance 
     * to be created on next getInstance() call.</p>
     */
    protected static void resetInstance() {
        if (instance != null) {
            instance.userService = null;
            instance = null;
        }
    }
}