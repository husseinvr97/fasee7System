package com.studenttracker.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;

/**
 * Utility class for displaying alert dialogs to users.
 * Provides standardized methods for error, info, warning, and confirmation dialogs.
 */
public class AlertHelper {
    
    private AlertHelper() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Show an error alert dialog.
     * @param message The error message to display
     */
    public static void showError(String message) {
        showAlert(AlertType.ERROR, "Error", message);
    }
    
    /**
     * Show an error alert dialog with custom title.
     * @param title The dialog title
     * @param message The error message to display
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }
    
    /**
     * Show an information alert dialog.
     * @param message The information message to display
     */
    public static void showInfo(String message) {
        showAlert(AlertType.INFORMATION, "Information", message);
    }
    
    /**
     * Show an information alert dialog with custom title.
     * @param title The dialog title
     * @param message The information message to display
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }
    
    /**
     * Show a warning alert dialog.
     * @param message The warning message to display
     */
    public static void showWarning(String message) {
        showAlert(AlertType.WARNING, "Warning", message);
    }
    
    /**
     * Show a warning alert dialog with custom title.
     * @param title The dialog title
     * @param message The warning message to display
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }
    
    /**
     * Show a confirmation dialog and return user's choice.
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String message) {
        return showConfirmation("Confirmation", message);
    }
    
    /**
     * Show a confirmation dialog with custom title and return user's choice.
     * @param title The dialog title
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show a success alert (styled as information).
     * @param message The success message to display
     */
    public static void showSuccess(String message) {
        showAlert(AlertType.INFORMATION, "Success", message);
    }
    
    /**
     * Show a success alert with custom title.
     * @param title The dialog title
     * @param message The success message to display
     */
    public static void showSuccess(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }
    
    /**
     * Internal helper method to create and show alerts.
     */
    private static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}