package com.studenttracker.exception;

public class InvalidLessonDateException extends ValidationException {
    public InvalidLessonDateException(String message) {
        super(message);
    }
}