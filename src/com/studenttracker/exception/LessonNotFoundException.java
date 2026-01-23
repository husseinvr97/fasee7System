package com.studenttracker.exception;

public class LessonNotFoundException extends EntityException {
    public LessonNotFoundException(Integer lessonId, String message) {
        super(lessonId, message);
    }
}