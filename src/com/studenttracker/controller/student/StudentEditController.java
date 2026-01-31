package com.studenttracker.controller.student;

import com.studenttracker.controller.BaseController;
import com.studenttracker.exception.DuplicatePhoneNumberException;
import com.studenttracker.exception.InvalidPhoneNumberException;
import com.studenttracker.exception.StudentNotFoundException;
import com.studenttracker.model.Student;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.StudentService;
import com.studenttracker.service.event.StudentUpdatedEvent;
import com.studenttracker.util.AlertHelper;
import com.studenttracker.util.ServiceLocator;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for Student Edit Modal.
 * Allows admins to edit student contact information.
 * 
 * Responsibilities:
 * - Load student data by ID
 * - Validate phone numbers (Egyptian format)
 * - Update student contact info via service
 * - Trigger StudentUpdatedEvent on save
 * 
 * SOLID Principles:
 * - SRP: Single responsibility is student contact editing
 * - DIP: Depends on StudentService interface
 * - OCP: Closed for modification, event-driven updates
 */
public class StudentEditController extends BaseController {
    
    private static final Logger LOGGER = Logger.getLogger(StudentEditController.class.getName());
    
    // ==================== FXML COMPONENTS ====================
    
    @FXML private Label studentNameLabel;
    @FXML private TextField phoneField;
    @FXML private Label phoneErrorLabel;
    @FXML private TextField whatsappField;
    @FXML private TextField parentPhoneField;
    @FXML private Label parentPhoneErrorLabel;
    @FXML private TextField parentWhatsappField;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    
    // ==================== STATE ====================
    
    private int studentId;
    private Student student;
    private Stage dialogStage;
    
    // ==================== SERVICES ====================
    
    private final StudentService studentService;
    
    // ==================== CONSTRUCTOR ====================
    
    public StudentEditController() {
        super();
        this.studentService = ServiceLocator.getInstance().getStudentService();
        LOGGER.info("StudentEditController created");
    }
    
    // ==================== LIFECYCLE ====================
    
    @Override
    public void initialize() {
        super.initialize();
        LOGGER.fine("StudentEditController initialized");
    }
    
    @Override
    public void cleanup() {
        LOGGER.fine("StudentEditController cleanup");
        super.cleanup();
    }
    
    // ==================== PUBLIC API ====================
    
    /**
     * Set student ID and load student data.
     * Must be called after controller creation.
     */
    public void setStudentId(int studentId) {
        if (studentId <= 0) {
            String errorMsg = "Invalid student ID: " + studentId;
            LOGGER.severe(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        this.studentId = studentId;
        loadStudent();
        LOGGER.info("Student ID set to: " + studentId);
    }
    
    /**
     * Set dialog stage reference.
     * Called by SceneManager to allow controller to close modal.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    // ==================== DATA LOADING ====================
    
    /**
     * Load student from database and populate form fields.
     */
    private void loadStudent() {
        try {
            LOGGER.fine("Loading student with ID: " + studentId);
            
            student = studentService.getStudentById(studentId);
            
            if (student == null) {
                LOGGER.severe("Student not found: " + studentId);
                AlertHelper.showError("Student not found with ID: " + studentId);
                closeModal();
                return;
            }
            
            // Populate form fields
            studentNameLabel.setText("Student: " + student.getFullName());
            phoneField.setText(student.getPhoneNumber() != null ? student.getPhoneNumber() : "");
            whatsappField.setText(student.getWhatsappNumber() != null ? student.getWhatsappNumber() : "");
            parentPhoneField.setText(student.getParentPhoneNumber() != null ? student.getParentPhoneNumber() : "");
            parentWhatsappField.setText(student.getParentWhatsappNumber() != null ? student.getParentWhatsappNumber() : "");
            
            LOGGER.info("Student data loaded successfully");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load student", e);
            AlertHelper.showError("Failed to load student data: " + e.getMessage());
            closeModal();
        }
    }
    
    // ==================== EVENT HANDLERS ====================
    
    /**
     * Handle Cancel button click.
     */
    @FXML
    private void handleCancel() {
        LOGGER.info("Cancel button clicked");
        closeModal();
    }
    
    /**
     * Handle Save button click.
     * Validates form and updates student if valid.
     */
    @FXML
    private void handleSave() {
        LOGGER.info("Save button clicked");
        
        // Clear previous errors
        clearErrors();
        
        // Validate form
        if (!validateForm()) {
            LOGGER.fine("Form validation failed");
            return;
        }
        
        // Collect data
        String phone = phoneField.getText().trim();
        String whatsapp = whatsappField.getText().trim();
        String parentPhone = parentPhoneField.getText().trim();
        String parentWhatsapp = parentWhatsappField.getText().trim();
        
        // Convert empty strings to null
        if (whatsapp.isEmpty()) whatsapp = null;
        if (parentWhatsapp.isEmpty()) parentWhatsapp = null;
        
        // Update student via service
        try {
            LOGGER.info("Updating student " + studentId);
            
            // Call service method
            // Note: fullName is unchanged (immutable in edit), so pass existing value
            boolean success = studentService.updateStudentInfo(
                studentId,
                student.getFullName(), // unchanged
                phone,
                parentPhone,
                whatsapp,
                parentWhatsapp
            );
            
            if (success) {
                LOGGER.info("Student updated successfully");
                AlertHelper.showSuccess("Student information updated successfully");
                 StudentUpdatedEvent event = new StudentUpdatedEvent(studentId, getCurrentUserId());
                 EventBusService.getInstance().publish(event);
                closeModal();
                // StudentUpdatedEvent will be published by service
            } else {
                LOGGER.warning("Update returned false");
                AlertHelper.showError("Failed to update student. Please try again.");
            }
            
        } catch (StudentNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Student not found during update", e);
            AlertHelper.showError("Student not found: " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            LOGGER.log(Level.WARNING, "Invalid phone number", e);
            AlertHelper.showError("Invalid phone number: " + e.getMessage());
        } catch (DuplicatePhoneNumberException e) {
            LOGGER.log(Level.WARNING, "Duplicate phone number", e);
            AlertHelper.showError("Phone number already exists: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update student", e);
            AlertHelper.showError("Failed to update student: " + e.getMessage());
        }
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Validate all form fields.
     * Shows inline error messages for invalid fields.
     * 
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateForm() {
        boolean valid = true;
        
        // Validate phone number (required)
        String phone = phoneField.getText().trim();
        if (!isValidEgyptianPhone(phone)) {
            showError(phoneErrorLabel, "Invalid phone number. Format: 01XXXXXXXXX (11 digits)");
            valid = false;
        }
        
        // Validate WhatsApp (optional, but if provided must be valid)
        String whatsapp = whatsappField.getText().trim();
        if (!whatsapp.isEmpty() && !isValidEgyptianPhone(whatsapp)) {
            AlertHelper.showWarning("Invalid WhatsApp number. Format: 01XXXXXXXXX (11 digits)");
            valid = false;
        }
        
        // Validate parent phone (required)
        String parentPhone = parentPhoneField.getText().trim();
        if (!isValidEgyptianPhone(parentPhone)) {
            showError(parentPhoneErrorLabel, "Invalid parent phone. Format: 01XXXXXXXXX (11 digits)");
            valid = false;
        }
        
        // Validate parent WhatsApp (optional, but if provided must be valid)
        String parentWhatsapp = parentWhatsappField.getText().trim();
        if (!parentWhatsapp.isEmpty() && !isValidEgyptianPhone(parentWhatsapp)) {
            AlertHelper.showWarning("Invalid parent WhatsApp. Format: 01XXXXXXXXX (11 digits)");
            valid = false;
        }
        
        return valid;
    }
    
    /**
     * Validate Egyptian phone number format.
     * Format: 01XXXXXXXXX (exactly 11 digits, starts with 01)
     * 
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidEgyptianPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        // Must be exactly 11 digits, starting with 01
        return phone.matches("^01\\d{9}$");
    }
    
    /**
     * Show inline error message under a field.
     */
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /**
     * Clear all inline error messages.
     */
    private void clearErrors() {
        phoneErrorLabel.setVisible(false);
        phoneErrorLabel.setManaged(false);
        parentPhoneErrorLabel.setVisible(false);
        parentPhoneErrorLabel.setManaged(false);
    }
    
    // ==================== HELPERS ====================
    
    /**
     * Close the modal dialog.
     */
    private void closeModal() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}