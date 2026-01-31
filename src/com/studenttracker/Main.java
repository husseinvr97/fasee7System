package com.studenttracker;

import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main - Entry point for Fasee7 Student Tracker System.
 * 
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Initialize JavaFX application</li>
 *   <li>Setup database (create tables if needed)</li>
 *   <li>Configure primary stage (window)</li>
 *   <li>Load initial scene (Login screen)</li>
 *   <li>Handle application lifecycle</li>
 * </ul>
 * 
 * <p><b>Startup Flow:</b></p>
 * <ol>
 *   <li>JavaFX calls launch()</li>
 *   <li>init() - Initialize database</li>
 *   <li>start() - Setup window and load Login screen</li>
 *   <li>Application runs...</li>
 *   <li>stop() - Cleanup on exit</li>
 * </ol>
 * 
 * <p><b>Usage:</b></p>
 * <pre>
 * // Run from IDE:
 * Right-click Main.java → Run 'Main.main()'
 * 
 * // Run from Maven:
 * mvn clean javafx:run
 * 
 * // Run from JAR:
 * java -jar fasee7-system.jar
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-28
 */
public class Main extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    // ==================== APPLICATION LIFECYCLE ====================
    
    /**
     * Initialization method - called before start().
     * Used for heavy initialization like database setup.
     * Runs on JavaFX-Launcher thread (NOT JavaFX Application Thread).
     * 
     * @throws Exception if initialization fails
     */
    @Override
    public void init() throws Exception {
        super.init();
        
        LOGGER.info("=================================================");
        LOGGER.info("  Fasee7 Student Tracker System");
        LOGGER.info("  Initializing Application...");
        LOGGER.info("=================================================");
        
        // Initialize database (creates tables if they don't exist)
        try {
            LOGGER.info("Initializing database...");
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            LOGGER.info("Database initialized successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw e; // Re-throw to prevent app from starting with broken DB
        }
        
        LOGGER.info("Application initialization complete");
    }
    
    /**
     * Start method - called after init().
     * Sets up the primary stage and loads the initial scene.
     * Runs on JavaFX Application Thread.
     * 
     * @param primaryStage The primary stage (main window) provided by JavaFX
     * @throws Exception if scene loading fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        LOGGER.info("Starting JavaFX application...");
        
        try {
            // Configure primary stage
            primaryStage.setTitle("Fasee7 Student Tracker System");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            
            // Register stage with SceneManager
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.setPrimaryStage(primaryStage);
            
            // Load initial scene (Login screen)
            LOGGER.info("Loading Login screen...");
            sceneManager.switchToLogin();
            
            // Show the window
            primaryStage.show();
            
            LOGGER.info("Application started successfully");
            LOGGER.info("=================================================");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start application", e);
            throw e;
        }
    }
    
    /**
     * Stop method - called when application is closing.
     * Used for cleanup operations.
     * Runs on JavaFX Application Thread.
     * 
     * @throws Exception if cleanup fails
     */
    @Override
    public void stop() throws Exception {
        LOGGER.info("=================================================");
        LOGGER.info("Application shutting down...");
        
        // Cleanup operations here (if needed)
        // - Close database connections
        // - Save application state
        // - Cancel background tasks
        
        super.stop();
        
        LOGGER.info("Application stopped successfully");
        LOGGER.info("=================================================");
    }
    
    // ==================== MAIN METHOD ====================
    
    /**
     * Main method - entry point for Java application.
     * Launches the JavaFX application.
     * 
     * <p><b>Note:</b> In a JavaFX application, this method should do nothing
     * but call launch(). All initialization happens in init() and start().</p>
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Launch JavaFX application
        // This will call: init() -> start() -> (wait for close) -> stop()
        launch(args);
    }
}