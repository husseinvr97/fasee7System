package com.studenttracker.util;

import com.studenttracker.controller.BaseController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SceneManager - Singleton class for managing JavaFX scene transitions and navigation.
 * 
 * <p><b>Enhanced Features (v2.0):</b></p>
 * <ul>
 *   <li>Load views into containers (for MainLayout pattern)</li>
 *   <li>Controller lifecycle management (cleanup before switching)</li>
 *   <li>Centralized FXML path mapping</li>
 *   <li>Convenient navigation methods</li>
 * </ul>
 * 
 * <p><b>Design Pattern:</b> Singleton + Strategy (different loading strategies)</p>
 * 
 * <p><b>Usage Example - Full Scene Switch:</b></p>
 * <pre>
 * sceneManager.switchToLogin();
 * sceneManager.switchToMainLayout();
 * </pre>
 * 
 * <p><b>Usage Example - Load into Container:</b></p>
 * <pre>
 * // In MainLayoutController
 * sceneManager.loadViewIntoContainer("Dashboard", contentArea);
 * sceneManager.loadViewIntoContainer("StudentList", contentArea);
 * </pre>
 * 
 * @author fasee7System
 * @version 2.0.0
 * @since 2026-01-28
 */
public class SceneManager {
    
    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());
    
    // ==================== SINGLETON PATTERN ====================
    
    private static SceneManager instance;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private SceneManager() {}
    
    /**
     * Get the singleton instance of SceneManager.
     * @return The SceneManager instance
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }
    
    // ==================== STATE ====================
    
    /**
     * The main application window.
     */
    private Stage primaryStage;
    
    /**
     * The current active controller (for lifecycle management).
     */
    private BaseController currentController;
    
    /**
     * Set the primary stage for the application.
     * Should be called once during application initialization.
     * 
     * @param stage The primary Stage from Application.start()
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Get the primary stage.
     * 
     * @return The primary Stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    // ==================== CONVENIENT NAVIGATION METHODS ====================
    
    /**
     * Switch to Login screen (after logout or on app start).
     * Cleans up current controller before switching.
     * 
     * @throws IOException if Login.fxml cannot be loaded
     */
    public void switchToLogin() throws IOException {
        cleanupCurrentController();
        loadScene("Login");
    }
    
    /**
     * Switch to MainLayout (after successful login).
     * Cleans up current controller before switching.
     * 
     * @throws IOException if MainLayout.fxml cannot be loaded
     */
    public void switchToMainLayout() throws IOException {
        cleanupCurrentController();
        loadScene("MainLayout");
    }
    
    // ==================== CORE LOADING METHODS ====================
    
    /**
     * Load a view into a container (for MainLayout content area).
     * Automatically cleans up previous controller and tracks new one.
     * 
     * <p><b>Use Case:</b> Loading different views into MainLayout's center area.</p>
     * 
     * @param viewName The logical name of the view (e.g., "Dashboard", "StudentList")
     * @param container The container to load the view into (typically a StackPane)
     * @throws IOException if the FXML file cannot be loaded
     */
    public void loadViewIntoContainer(String viewName, StackPane container) throws IOException {
        // Cleanup previous controller if exists
        cleanupCurrentController();
        
        // Get FXML path
        String fxmlPath = getFXMLPath(viewName);
        
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();
        
        // Get controller and track it
        BaseController controller = loader.getController();
        currentController = controller;
        
        // Clear container and add new view
        container.getChildren().clear();
        container.getChildren().add(view);
        
        LOGGER.info("Loaded view '" + viewName + "' into container");
    }
    
    /**
     * Load a scene by logical name (replaces entire scene).
     * 
     * @param viewName The logical name of the view (e.g., "Login", "MainLayout")
     * @throws IOException if the FXML file cannot be loaded
     */
    private void loadScene(String viewName) throws IOException {
        String fxmlPath = getFXMLPath(viewName);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        // Get controller and track it
        BaseController controller = loader.getController();
        currentController = controller;
        
        // Create and set scene
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        LOGGER.info("Switched to scene: " + viewName);
    }
    
    // ==================== LEGACY METHODS (Backward Compatibility) ====================
    
    /**
     * Switch to a different scene by loading an FXML file.
     * Replaces the entire current scene.
     * 
     * <p><b>Deprecated:</b> Use switchToLogin() or switchToMainLayout() instead for type safety.</p>
     * 
     * @param fxmlPath The path to the FXML file (e.g., "/view/login.fxml")
     */
    public void switchScene(String fxmlPath) {
        try {
            // Cleanup previous controller
            cleanupCurrentController();
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Track controller
            BaseController controller = loader.getController();
            currentController = controller;
            
            // Create new scene
            Scene scene = new Scene(root);
            
            // Set scene on primary stage
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load screen: " + fxmlPath, e);
            AlertHelper.showError("Failed to load screen: " + fxmlPath);
        } catch (NullPointerException e) {
            LOGGER.log(Level.SEVERE, "FXML file not found: " + fxmlPath, e);
            AlertHelper.showError("FXML file not found: " + fxmlPath);
        }
    }
    
    /**
     * Load FXML content and return the Parent node.
     * Useful for loading content into a container (e.g., BorderPane center).
     * 
     * @param fxmlPath The path to the FXML file
     * @return The loaded Parent node
     * @throws IOException If the FXML file cannot be loaded
     */
    public Parent loadContent(String fxmlPath) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            return loader.load();
        } catch (NullPointerException e) {
            throw new IOException("FXML file not found: " + fxmlPath, e);
        }
    }
    
    /**
     * Show a modal dialog window.
     * The main window will be blocked until the dialog is closed.
     * 
     * @param fxmlPath The path to the dialog FXML file
     * @param title The title for the dialog window
     */
    public void showDialog(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Create new stage (window) for dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL); // Block main window
            dialogStage.initOwner(primaryStage);
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait(); // Wait until dialog is closed
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load dialog: " + fxmlPath, e);
            AlertHelper.showError("Failed to load dialog: " + fxmlPath);
        } catch (NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Dialog FXML file not found: " + fxmlPath, e);
            AlertHelper.showError("Dialog FXML file not found: " + fxmlPath);
        }
    }
    
    /**
     * Show a modal dialog window and return the controller.
     * Useful when you need to interact with the dialog controller.
     * 
     * @param fxmlPath The path to the dialog FXML file
     * @param title The title for the dialog window
     * @param <T> The controller type
     * @return The controller instance of the loaded dialog
     */
    public <T> T showDialogWithController(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Create new stage (window) for dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
            return loader.getController();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load dialog: " + fxmlPath, e);
            AlertHelper.showError("Failed to load dialog: " + fxmlPath);
            return null;
        } catch (NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Dialog FXML file not found: " + fxmlPath, e);
            AlertHelper.showError("Dialog FXML file not found: " + fxmlPath);
            return null;
        }
    }
    
    /**
     * Switch to a scene and return its controller.
     * Useful when you need to initialize the controller with data.
     * 
     * @param fxmlPath The path to the FXML file
     * @param <T> The controller type
     * @return The controller instance of the loaded scene
     * @throws IOException If the FXML file cannot be loaded
     */
    public <T> T loadSceneWithController(String fxmlPath) throws IOException {
        try {
            // Cleanup previous controller
            cleanupCurrentController();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Track controller
            BaseController controller = loader.getController();
            currentController = controller;
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            return loader.getController();
            
        } catch (NullPointerException e) {
            throw new IOException("FXML file not found: " + fxmlPath, e);
        }
    }
    
    /**
     * Load FXML content and return both the Parent node and its controller.
     * 
     * @param fxmlPath The path to the FXML file
     * @param <T> The controller type
     * @return A LoadResult containing both the Parent and controller
     * @throws IOException If the FXML file cannot be loaded
     */
    public <T> LoadResult<T> loadContentWithController(String fxmlPath) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();
            return new LoadResult<>(root, controller);
            
        } catch (NullPointerException e) {
            throw new IOException("FXML file not found: " + fxmlPath, e);
        }
    }
    
    // ==================== CONTROLLER LIFECYCLE MANAGEMENT ====================
    
    /**
     * Cleanup the current controller before switching views.
     * Calls the controller's cleanup() method if it exists.
     */
    private void cleanupCurrentController() {
        if (currentController != null) {
            try {
                currentController.cleanup();
                LOGGER.fine("Cleaned up controller: " + currentController.getClass().getSimpleName());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during controller cleanup", e);
            }
            currentController = null;
        }
    }
    
    // ==================== FXML PATH MAPPING ====================
    
    /**
     * Maps logical view names to FXML file paths.
     * Centralized mapping for easy maintenance and refactoring.
     * 
     * @param viewName The logical name of the view
     * @return The full FXML file path
     * @throws IllegalArgumentException if view name is unknown
     */
    private String getFXMLPath(String viewName) {
        switch (viewName) {
            // Authentication
            case "Login":
                return "/com/studenttracker/view/fxml/auth/Login.fxml";
            
            // Main Layout
            case "MainLayout":
                return "/com/studenttracker/view/fxml/layout/MainLayout.fxml";
            
            // Dashboard
            case "Dashboard":
                return "/com/studenttracker/view/fxml/layout/Dashboard.fxml";
            
            // Students
            case "StudentList":
                return "/com/studenttracker/view/fxml/students/StudentList.fxml";
            case "StudentRegistration":
                return "/com/studenttracker/view/fxml/students/StudentRegistration.fxml";
            case "StudentDetail":
                return "/com/studenttracker/view/fxml/students/StudentDetail.fxml";
            
            // Lessons
            case "LessonList":
                return "/com/studenttracker/view/fxml/lessons/LessonList.fxml";
            case "LessonCreation":
                return "/com/studenttracker/view/fxml/lessons/LessonCreation.fxml";
            case "LessonDetail":
                return "/com/studenttracker/view/fxml/lessons/LessonDetail.fxml";
            
            // Missions
            case "MissionList":
                return "/com/studenttracker/view/fxml/missions/MissionList.fxml";
            case "MissionExecution":
                return "/com/studenttracker/view/fxml/missions/MissionExecution.fxml";
            
            // Fasee7 Table
            case "Fasee7Table":
                return "/com/studenttracker/view/fxml/fasee7/Fasee7Table.fxml";
            
            // Warnings
            case "WarningList":
                return "/com/studenttracker/view/fxml/warnings/WarningList.fxml";
            
            // Notifications
            case "NotificationCenter":
                return "/com/studenttracker/view/fxml/notifications/NotificationCenter.fxml";
            
            // Reports
            case "MonthlyReportList":
                return "/com/studenttracker/view/fxml/reports/MonthlyReportList.fxml";
            case "ReportGeneration":
                return "/com/studenttracker/view/fxml/reports/ReportGeneration.fxml";
            
            // Update Requests
            case "UpdateRequestQueue":
                return "/com/studenttracker/view/fxml/requests/UpdateRequestQueue.fxml";
            case "SubmitRequest":
                return "/com/studenttracker/view/fxml/requests/SubmitRequest.fxml";
            
            // Settings
            case "Settings":
                return "/com/studenttracker/view/fxml/settings/Settings.fxml";
            
            default:
                throw new IllegalArgumentException("Unknown view name: " + viewName);
        }
    }
    
    // ==================== HELPER CLASSES ====================
    
    /**
     * Helper class to return both Parent and Controller from loading operations.
     * 
     * @param <T> The controller type
     */
    public static class LoadResult<T> {
        private final Parent parent;
        private final T controller;
        
        public LoadResult(Parent parent, T controller) {
            this.parent = parent;
            this.controller = controller;
        }
        
        public Parent getParent() {
            return parent;
        }
        
        public T getController() {
            return controller;
        }
    }
}