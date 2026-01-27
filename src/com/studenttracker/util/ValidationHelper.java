package com.studenttracker.util;

import java.time.LocalDate;

/**
 * ValidationHelper - Common validation methods used across multiple screens.
 * 
 * <p>This utility class provides fast, client-side validation checks that can be used
 * in controllers before making service layer calls. Unlike the service-layer validators
 * (NameValidator, PhoneValidator, PasswordValidator), these methods:</p>
 * <ul>
 *   <li>Return boolean instead of throwing exceptions</li>
 *   <li>Provide quick feedback to users</li>
 *   <li>Reduce unnecessary service/database calls</li>
 *   <li>Offer centralized error messages</li>
 * </ul>
 * 
 * <p><b>Design Pattern:</b> Two-Layer Validation</p>
 * <ol>
 *   <li><b>Client-Side (ValidationHelper):</b> Fast rejection of obviously invalid input</li>
 *   <li><b>Service-Side (Specific Validators):</b> Business rule enforcement with database checks</li>
 * </ol>
 * 
 * <p><b>Usage Example - Student Registration Form:</b></p>
 * <pre>
 * String phone = phoneField.getText();
 * if (!ValidationHelper.isValidEgyptianPhone(phone)) {
 *     showError(ValidationHelper.getPhoneErrorMessage());
 *     return;
 * }
 * 
 * String fullName = nameField.getText();
 * if (!ValidationHelper.isValid4PartName(fullName)) {
 *     showError(ValidationHelper.getNameErrorMessage());
 *     return;
 * }
 * 
 * // All client validations passed, proceed to service layer
 * studentService.registerStudent(fullName, phone, ...);
 * </pre>
 * 
 * @author fasee7System
 * @version 1.0.0
 * @since 2026-01-27
 * @see com.studenttracker.service.validator.NameValidator
 * @see com.studenttracker.service.validator.PhoneValidator
 * @see com.studenttracker.service.validator.PasswordValidator
 */
public class ValidationHelper {
    
    // ==================== Constants ====================
    
    /**
     * Pattern for Egyptian phone numbers: 01[0-9]{9}
     * - Must start with "01"
     * - Followed by exactly 9 digits
     * - Total: 11 digits
     */
    private static final String EGYPTIAN_PHONE_PATTERN = "^01[0-9]{9}$";
    
    /**
     * Required number of name parts for الاسم الرباعي (4-part name)
     */
    private static final int REQUIRED_NAME_PARTS = 4;
    
    /**
     * Minimum password length for basic validation
     */
    private static final int MIN_PASSWORD_LENGTH = 8;
    
    /**
     * Pattern for valid usernames:
     * - Alphanumeric characters only (a-z, A-Z, 0-9)
     * - Length between 3 and 20 characters
     */
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{3,20}$";
    
    
    // ==================== Private Constructor ====================
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private ValidationHelper() {
        throw new AssertionError("ValidationHelper is a utility class and should not be instantiated");
    }
    
    
    // ==================== Phone Number Validation ====================
    
    /**
     * Validates Egyptian phone number format.
     * 
     * <p><b>Valid Format:</b> 01XXXXXXXXX (11 digits starting with 01)</p>
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>Valid: "01234567890", "01012345678", "01 234 567 890"</li>
     *   <li>Invalid: "0123456789" (10 digits), "02123456789" (doesn't start with 01)</li>
     * </ul>
     * 
     * <p><b>Note:</b> This method automatically removes spaces and dashes before validation.</p>
     * 
     * @param phone the phone number to validate
     * @return true if phone number matches Egyptian format, false otherwise
     */
    public static boolean isValidEgyptianPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        // Remove spaces, dashes, and parentheses for flexible input
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");
        
        // Check format: starts with 01, exactly 11 digits
        return cleanPhone.matches(EGYPTIAN_PHONE_PATTERN);
    }
    
    
    // ==================== Name Validation ====================
    
    /**
     * Validates that full name contains exactly 4 parts (الاسم الرباعي).
     * 
     * <p><b>Requirements:</b></p>
     * <ul>
     *   <li>Must contain exactly 4 space-separated parts</li>
     *   <li>Cannot be null or empty</li>
     *   <li>Extra spaces are ignored</li>
     * </ul>
     * 
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>Valid: "أحمد محمد علي حسن", "Ahmed Mohamed Ali Hassan"</li>
     *   <li>Invalid: "أحمد محمد علي" (only 3 parts), "" (empty)</li>
     * </ul>
     * 
     * <p><b>Note:</b> This method only checks the count of parts. For Arabic script
     * validation, the service layer's NameValidator should be used.</p>
     * 
     * @param fullName the full name to validate
     * @return true if name has exactly 4 parts, false otherwise
     */
    public static boolean isValid4PartName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        
        // Split by whitespace and count parts
        // \\s+ matches one or more whitespace characters (handles multiple spaces)
        String[] parts = fullName.trim().split("\\s+");
        return parts.length >= REQUIRED_NAME_PARTS;
    }
    
    
    // ==================== Empty String Validation ====================
    
    /**
     * Checks if a string value is not empty.
     * 
     * <p>This method is useful for validating required form fields.</p>
     * 
     * <p><b>Returns false if:</b></p>
     * <ul>
     *   <li>Value is null</li>
     *   <li>Value is empty string ""</li>
     *   <li>Value contains only whitespace "   "</li>
     * </ul>
     * 
     * @param value the string to check
     * @return true if value is not null and contains non-whitespace characters, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    
    // ==================== Date Validation ====================
    
    /**
     * Validates that a date is not in the future.
     * 
     * <p>This is useful for validating dates like:</p>
     * <ul>
     *   <li>Lesson dates (lessons cannot be scheduled in the future)</li>
     *   <li>Attendance dates</li>
     *   <li>Birth dates</li>
     *   <li>Registration dates</li>
     * </ul>
     * 
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>Valid: Today's date, yesterday, last week</li>
     *   <li>Invalid: Tomorrow, next week, next year</li>
     * </ul>
     * 
     * @param date the date to validate
     * @return true if date is today or in the past, false if date is null or in the future
     */
    public static boolean isNotFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return !date.isAfter(LocalDate.now());
    }
    
    
    // ==================== Password Validation ====================
    
    /**
     * Validates basic password strength (minimum length only).
     * 
     * <p><b>Basic Requirement:</b> Password must be at least 8 characters long.</p>
     * 
     * <p><b>Note:</b> This is a simplified check for quick client-side validation.
     * For full password complexity validation (uppercase, lowercase, digits, special characters),
     * use PasswordValidator.validatePassword() in the service layer.</p>
     * 
     * <p><b>Usage:</b></p>
     * <pre>
     * // Quick check in controller
     * if (!ValidationHelper.isValidPassword(password)) {
     *     showError("Password must be at least 8 characters");
     *     return;
     * }
     * 
     * // Full validation in service layer
     * PasswordValidator.validatePassword(password); // throws WeakPasswordException
     * </pre>
     * 
     * @param password the password to validate
     * @return true if password is at least 8 characters long, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
    
    
    // ==================== Username Validation ====================
    
    /**
     * Validates username format.
     * 
     * <p><b>Requirements:</b></p>
     * <ul>
     *   <li>Alphanumeric characters only (a-z, A-Z, 0-9)</li>
     *   <li>Length between 3 and 20 characters</li>
     *   <li>No spaces or special characters</li>
     * </ul>
     * 
     * <p><b>Examples:</b></p>
     * <ul>
     *   <li>Valid: "admin", "teacher01", "Ahmed2024"</li>
     *   <li>Invalid: "ad" (too short), "admin user" (has space), "admin@123" (special char)</li>
     * </ul>
     * 
     * @param username the username to validate
     * @return true if username matches the pattern, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches(USERNAME_PATTERN);
    }
    
    
    // ==================== Error Message Methods ====================
    
    /**
     * Gets the standard error message for invalid phone numbers.
     * 
     * <p>This ensures consistent error messages across all forms.</p>
     * 
     * @return the phone validation error message
     */
    public static String getPhoneErrorMessage() {
        return "Phone number must be Egyptian format: 01XXXXXXXXX (11 digits)";
    }
    
    /**
     * Gets the standard error message for invalid 4-part names.
     * 
     * <p>This ensures consistent error messages across all forms.</p>
     * 
     * @return the name validation error message
     */
    public static String getNameErrorMessage() {
        return "Full name must have exactly 4 parts (الاسم الرباعي)";
    }
    
    /**
     * Gets the standard error message for empty required fields.
     * 
     * @param fieldName the name of the field (e.g., "Username", "Password")
     * @return the customized error message
     */
    public static String getEmptyFieldMessage(String fieldName) {
        return fieldName + " cannot be empty";
    }
    
    /**
     * Gets the standard error message for future dates.
     * 
     * @return the date validation error message
     */
    public static String getFutureDateErrorMessage() {
        return "Date cannot be in the future";
    }
    
    /**
     * Gets the standard error message for weak passwords.
     * 
     * @return the password validation error message
     */
    public static String getWeakPasswordMessage() {
        return "Password must be at least 8 characters long";
    }
    
    /**
     * Gets the standard error message for invalid usernames.
     * 
     * @return the username validation error message
     */
    public static String getInvalidUsernameMessage() {
        return "Username must be 3-20 alphanumeric characters (no spaces or special characters)";
    }
}