package com.studenttracker.service.validator;

import com.studenttracker.exception.InvalidPhoneNumberException;

/**
 * Validator for Egyptian phone numbers.
 * Format: 01XXXXXXXXX (11 digits starting with 01)
 */
public class PhoneValidator {
    
    // Egyptian phone number pattern: 01[0-9]{9}
    private static final String EGYPTIAN_PHONE_PATTERN = "^01[0-9]{9}$";
    
    /**
     * Validates Egyptian phone number format.
     * 
     * @param phoneNumber The phone number to validate
     * @throws InvalidPhoneNumberException if validation fails
     */
    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new InvalidPhoneNumberException("Phone number cannot be empty");
        }
        
        // Remove any spaces, dashes, or parentheses
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-()]", "");
        
        // Validate Egyptian format
        if (!cleanNumber.matches(EGYPTIAN_PHONE_PATTERN)) {
            throw new InvalidPhoneNumberException(
                phoneNumber,
                "Invalid Egyptian phone number format. Must be 01XXXXXXXXX (11 digits starting with 01). Got: " + phoneNumber
            );
        }
    }
    
    /**
     * Checks if phone number is valid without throwing exception.
     * 
     * @param phoneNumber The phone number to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        try {
            validatePhoneNumber(phoneNumber);
            return true;
        } catch (InvalidPhoneNumberException e) {
            return false;
        }
    }
    
    /**
     * Cleans phone number by removing formatting characters.
     * 
     * @param phoneNumber The phone number to clean
     * @return Cleaned phone number (digits only)
     */
    public static String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return phoneNumber.replaceAll("[\\s\\-()]", "");
    }
}