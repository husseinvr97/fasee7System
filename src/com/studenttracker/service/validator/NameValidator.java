package com.studenttracker.service.validator;

import com.studenttracker.exception.InvalidFullNameException;

/**
 * Validator for student full names.
 * Requirement: Must be 4 parts (الاسم الرباعي)
 */
public class NameValidator {
    
    private static final int REQUIRED_NAME_PARTS = 4;
    
    /**
     * Validates that full name contains exactly 4 parts.
     * 
     * @param fullName The full name to validate
     * @throws InvalidFullNameException if validation fails
     */
    public static void validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new InvalidFullNameException("Full name cannot be empty");
        }
        
        // 1. Validate Arabic Script
        // This regex allows Arabic letters and spaces. 
        // It excludes numbers, Latin characters, and special symbols.
        if (!fullName.matches("^[\\u0600-\\u06FF\\s]+$")) {
            throw new InvalidFullNameException("Name must be written in Arabic characters only.");
        }
        // Split by whitespace and filter empty strings
        String[] parts = fullName.trim().split("\\s+");
        
        if (parts.length < REQUIRED_NAME_PARTS) {
            throw new InvalidFullNameException(
                fullName,
                "Full name must contain at least " + REQUIRED_NAME_PARTS + 
                " parts (الاسم الرباعي). Found: " + parts.length + " part(s)"
            );
        }
    }
    
    /**
     * Checks if full name is valid without throwing exception.
     * 
     * @param fullName The full name to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidFullName(String fullName) {
        try {
            validateFullName(fullName);
            return true;
        } catch (InvalidFullNameException e) {
            return false;
        }
    }
    
    /**
     * Gets the number of name parts.
     * 
     * @param fullName The full name
     * @return Number of name parts
     */
    public static int getNamePartCount(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return 0;
        }
        return fullName.trim().split("\\s+").length;
    }
    
    /**
     * Normalizes full name by trimming and reducing multiple spaces to single space.
     * 
     * @param fullName The full name to normalize
     * @return Normalized full name
     */
    public static String normalizeFullName(String fullName) {
        if (fullName == null) {
            return null;
        }
        return fullName.trim().replaceAll("\\s+", " ");
    }
}