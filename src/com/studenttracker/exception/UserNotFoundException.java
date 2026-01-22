package com.studenttracker.exception;

public class UserNotFoundException extends ServiceException 
{
    public UserNotFoundException(String message) {
        super(message);
    }
}
