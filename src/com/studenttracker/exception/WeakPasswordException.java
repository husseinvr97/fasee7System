package com.studenttracker.exception;

public class WeakPasswordException extends ValidationException {
    public WeakPasswordException(String message) {
        super(message);
    }
}