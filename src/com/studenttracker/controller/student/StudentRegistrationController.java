package com.studenttracker.controller.student;

import com.studenttracker.controller.BaseController;
import com.studenttracker.model.User;
import com.studenttracker.service.StudentService;
import com.studenttracker.util.AlertHelper;
import com.studenttracker.util.ValidationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Student Registration modal dialog.
 * Handles form validation, data collection, and service calls.
 * 
 * <p><b>Design Pattern:</b> MVC - Controller</p>
 * <p><b>Validation Strategy:</b> Two-layer validation (client-side + service-side)</p>
 * 
 * <p><b>Lifecycle:</b></p>
 * <ol>
 *   <li>Dialog opened → initialize() called → set default values</li>
 *   <li>User fills form → real-time validation on Save click</li>
 *   <li>If valid → call service → show success → close dialog</li>
 *   <li>If invalid → show errors → keep dialog open for correction</li>
 * </ol>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-30
 */
public class StudentRegistrationController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentRegistrationController.class.getName());
    
    // ==================== FXML INJECTED COMPONENTS ====================
    
    @FXML private TextField studentIdField;
    @FXML private TextField fullNameField;
    @FXML private Label nameErrorLabel;
    @FXML private TextField phoneField;
    @FXML private Label phoneErrorLabel;
    @FXML private TextField whatsappField;
    @FXML private TextField parentPhoneField;
    @FXML private Label parentPhoneErrorLabel;
    @FXML private TextField parentWhatsappField;
    @FXML private DatePicker registrationDatePicker;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    // ==================== DEPENDENCIES ====================
    
    private StudentService studentService;
    private Stage dialogStage;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Constructor - initializes services from ServiceLocator.
     * Called before FXML injection.
     */
    public StudentRegistrationController() {
        super();
    }
    
    // ==================== LIFECYCLE METHODS ====================
    
    /**
     * Initialize method - called after FXML injection.
     * Sets up default values and prepares the form.
     */
    @Override
    public void initialize() {
        super.initialize();
        
        // Get StudentService from ServiceLocator
        this.studentService = serviceLocator.getStudentService();
        
        // Set default registration date to today
        registrationDatePicker.setValue(LocalDate.now());
        
        // Show "Auto-generated" in ID field
        studentIdField.setText("Auto-generated");
        
        LOGGER.info("StudentRegistrationController initialized");
    }
    
    /**
     * Set the dialog stage (called by SceneManager or parent controller).
     * Required for closing the dialog after successful registration.
     * 
     * @param dialogStage The Stage containing this dialog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    /**
     * Cleanup method - called before controller is destroyed.
     * Currently no cleanup needed as we don't subscribe to events.
     */
    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handle cancel button click.
     * Closes the dialog without saving.
     */
    @FXML
    private void handleCancel() {
        LOGGER.fine("User cancelled student registration");
        closeDialog();
    }
    
    /**
     * Handle save button click.
     * Validates form, calls service, and closes dialog on success.
     */
    @FXML
    private void handleSave() {
        LOGGER.fine("User clicked Register Student button");
        
        // Step 1: Clear previous error messages
        clearErrors();
        
        // Step 2: Validate all fields (client-side)
        if (!validateForm()) {
            LOGGER.fine("Form validation failed");
            return;
        }
        
        // Step 3: Collect data from form
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String whatsapp = whatsappField.getText().trim();
        String parentPhone = parentPhoneField.getText().trim();
        String parentWhatsapp = parentWhatsappField.getText().trim();
        LocalDate regDate = registrationDatePicker.getValue();
        
        // Convert empty strings to null for optional fields
        if (whatsapp.isEmpty()) whatsapp = null;
        if (parentWhatsapp.isEmpty()) parentWhatsapp = null;
        
        // Step 4: Get current user
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null) {
            LOGGER.severe("No user logged in - cannot register student");
            AlertHelper.showError("Session Error : You must be logged in to register students");
            return;
        }
        
        // Step 5: Call service to register student
        try {
            LOGGER.info("Calling StudentService.registerStudent()");
            
            Integer studentId = studentService.registerStudent(
                fullName,
                phone,
                parentPhone,
                whatsapp,
                parentWhatsapp,
                currentUser.getUserId()
            );
            
            // Success!
            LOGGER.info("Student registered successfully with ID: " + studentId);
            AlertHelper.showSuccess("Success : Student registered successfully!\nStudent ID: " + studentId);
            
            // Close the dialog
            closeDialog();
            
            // Note: StudentRegisteredEvent will be published by the service layer
            // Any listeners will be automatically notified
            
        } catch (Exception e) {
            // Handle all exceptions (validation, duplicate, database, etc.)
            LOGGER.log(Level.SEVERE, "Failed to register student", e);
            AlertHelper.showError("Registration Error : Failed to register student:\n" + e.getMessage());
        }
    }
    
    // ==================== VALIDATION LOGIC ====================
    
    /**
     * Validate all form fields using client-side validation.
     * Shows inline error messages for invalid fields.
     * 
     * @return true if all validations pass, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;
        
        // Validate full name (4 parts)
        String fullName = fullNameField.getText().trim();
        if (!ValidationHelper.isValid4PartName(fullName)) {
            showError(nameErrorLabel, ValidationHelper.getNameErrorMessage());
            valid = false;
        }
        
        // Validate phone number
        String phone = phoneField.getText().trim();
        if (!ValidationHelper.isValidEgyptianPhone(phone)) {
            showError(phoneErrorLabel, ValidationHelper.getPhoneErrorMessage());
            valid = false;
        }
        
        // Validate WhatsApp (optional, but if provided must be valid)
        String whatsapp = whatsappField.getText().trim();
        if (!whatsapp.isEmpty() && !ValidationHelper.isValidEgyptianPhone(whatsapp)) {
            AlertHelper.showWarning("Invalid WhatsApp : Student WhatsApp number format: 01XXXXXXXXX (11 digits)");
            valid = false;
        }
        
        // Validate parent phone
        String parentPhone = parentPhoneField.getText().trim();
        if (!ValidationHelper.isValidEgyptianPhone(parentPhone)) {
            showError(parentPhoneErrorLabel, ValidationHelper.getPhoneErrorMessage());
            valid = false;
        }
        
        // Validate parent WhatsApp (optional, but if provided must be valid)
        String parentWhatsapp = parentWhatsappField.getText().trim();
        if (!parentWhatsapp.isEmpty() && !ValidationHelper.isValidEgyptianPhone(parentWhatsapp)) {
            AlertHelper.showWarning("Invalid Parent WhatsApp : Parent WhatsApp number format: 01XXXXXXXXX (11 digits)");
            valid = false;
        }
        
        // Validate registration date (not in future)
        LocalDate regDate = registrationDatePicker.getValue();
        if (regDate == null) {
            AlertHelper.showWarning("Missing Date : Please select a registration date");
            valid = false;
        } else if (!ValidationHelper.isNotFutureDate(regDate)) {
            AlertHelper.showWarning("Invalid Date :" + 
                ValidationHelper.getFutureDateErrorMessage());
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * Show error message under a specific field.
     * Makes the error label visible with the given message.
     * 
     * @param errorLabel The error label to show
     * @param message The error message text
     */
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /**
     * Clear all inline error messages.
     * Hides all error labels.
     */
    private void clearErrors() {
        hideError(nameErrorLabel);
        hideError(phoneErrorLabel);
        hideError(parentPhoneErrorLabel);
    }
    
    /**
     * Hide a specific error label.
     * 
     * @param errorLabel The error label to hide
     */
    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Close the dialog window.
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}