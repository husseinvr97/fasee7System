package com.studenttracker.service.validator;

import org.mindrot.jbcrypt.BCrypt;

import com.studenttracker.exception.WeakPasswordException;

public class PasswordValidator 
{

    private static final int MIN_PASSWORD_LENGTH = 8;

    private PasswordValidator() {}

    public static void validatePassword(String password) {
        // 1. Basic Length Check
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new WeakPasswordException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }

        // 2. Complexity Check using Regex
        // ^                 - Start of string
        // (?=.*[0-9])       - Contains at least one digit
        // (?=.*[a-z])       - Contains at least one lowercase letter
        // (?=.*[A-Z])       - Contains at least one uppercase letter
        // (?=.*[@#$%^&+=!]) - Contains at least one special character
        // \S+$              - No whitespace allowed, and end of string
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])\\S+$";

        if (!password.matches(regex)) {
            throw new WeakPasswordException(
                "Password must contain at least one uppercase letter, " +
                "one lowercase letter, one number, and one special character (@#$%^&+=!)."
            );
        }
    }

    public static boolean verifyPassword(String plainPassword, String passwordHash) {
        return BCrypt.checkpw(plainPassword, passwordHash);
    }

    public static boolean isStrongPassword(String password) {
        try {
            validatePassword(password);
            return true;
        } catch (WeakPasswordException e) {
            return false;
        }
    }
}
