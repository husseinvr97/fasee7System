package com.studenttracker.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;

/**
 * Singleton class for managing JavaFX scene transitions and navigation.
 * Centralizes all scene switching, content loading, and dialog display operations.
 */
public class SceneManager {
    
    // Singleton instance
    private static SceneManager instance;
    
    // The main application window
    private Stage primaryStage;
    
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
    
    /**
     * Set the primary stage for the application.
     * Should be called once during application initialization.
     * @param stage The primary Stage from Application.start()
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }
    
    /**
     * Get the primary stage.
     * @return The primary Stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Switch to a different scene by loading an FXML file.
     * Replaces the entire current scene.
     * @param fxmlPath The path to the FXML file (e.g., "/view/login.fxml")
     */
    public void switchScene(String fxmlPath) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Create new scene
            Scene scene = new Scene(root);
            
            // Set scene on primary stage
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Scene Loading Failed", 
                "Failed to load screen: " + fxmlPath + "\n" + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            AlertHelper.showError("FXML Not Found", 
                "FXML file not found: " + fxmlPath);
        }
    }
    
    /**
     * Load FXML content and return the Parent node.
     * Useful for loading content into a container (e.g., BorderPane center).
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
            e.printStackTrace();
            AlertHelper.showError("Dialog Loading Failed", 
                "Failed to load dialog: " + fxmlPath + "\n" + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            AlertHelper.showError("FXML Not Found", 
                "Dialog FXML file not found: " + fxmlPath);
        }
    }
    
    /**
     * Show a modal dialog window and return the controller.
     * Useful when you need to interact with the dialog controller.
     * @param fxmlPath The path to the dialog FXML file
     * @param title The title for the dialog window
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
            e.printStackTrace();
            AlertHelper.showError("Dialog Loading Failed", 
                "Failed to load dialog: " + fxmlPath + "\n" + e.getMessage());
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            AlertHelper.showError("FXML Not Found", 
                "Dialog FXML file not found: " + fxmlPath);
            return null;
        }
    }
    
    /**
     * Switch to a scene and return its controller.
     * Useful when you need to initialize the controller with data.
     * @param fxmlPath The path to the FXML file
     * @return The controller instance of the loaded scene
     * @throws IOException If the FXML file cannot be loaded
     */
    public <T> T loadSceneWithController(String fxmlPath) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
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
     * @param fxmlPath The path to the FXML file
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
    
    /**
     * Helper class to return both Parent and Controller from loading operations.
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