package com.studenttracker.exception;

public class DuplicateUsernameException extends ValidationException {
    public DuplicateUsernameException(String message) {
        super(message);
    }
}