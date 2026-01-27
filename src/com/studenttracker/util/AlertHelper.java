package com.studenttracker.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Utility class for displaying alerts to the user.
 * Provides centralized alert management with consistent styling and behavior.
 * All methods are static - no instantiation required.
 * 
 * @author Student Tracker Team
 * @version 1.0
 */
public class AlertHelper {
    
    /**
     * Show error alert (red icon, "Error" title).
     * Use this for validation errors, operation failures, and exceptions.
     * 
     * @param message The error message to display to the user
     * 
     * @example
     * // In controller, if login fails:
     * if (user == null) {
     *     AlertHelper.showError("Invalid username or password");
     *     return;
     * }
     */
    public static void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show success alert (green checkmark, "Success" title).
     * Use this to confirm successful operations to the user.
     * 
     * @param message The success message to display to the user
     * 
     * @example
     * // After student registered:
     * Integer studentId = studentService.registerStudent(...);
     * if (studentId != null) {
     *     AlertHelper.showSuccess("Student registered successfully!");
     * }
     */
    public static void showSuccess(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show warning alert (yellow icon, "Warning" title).
     * Use this for non-critical issues that the user should be aware of.
     * 
     * @param message The warning message to display to the user
     * 
     * @example
     * // If user tries to archive already-archived student:
     * AlertHelper.showWarning("This student is already archived");
     */
    public static void showWarning(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show confirmation dialog (asks user Yes/No).
     * Returns true if user clicks Yes/OK, false if No/Cancel.
     * Use this before performing destructive or important operations.
     * 
     * @param title The title of the confirmation dialog
     * @param message The confirmation question to display to the user
     * @return true if user confirmed (clicked OK), false if user cancelled
     * 
     * @example
     * // Before deleting a lesson:
     * boolean confirmed = AlertHelper.showConfirmation(
     *     "Delete Lesson", 
     *     "Are you sure you want to delete this lesson? This action cannot be undone."
     * );
     * 
     * if (confirmed) {
     *     lessonService.deleteLesson(lessonId, currentUserId);
     *     AlertHelper.showSuccess("Lesson deleted");
     *     reloadLessons();
     * }
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
     * Show info alert (blue icon).
     * Use this for general information messages.
     * 
     * @param title The title of the information dialog
     * @param message The information message to display to the user
     * 
     * @example
     * // Display system information:
     * AlertHelper.showInfo("System Status", "All systems are running normally.");
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}