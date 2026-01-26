package com.studenttracker.integration;

import com.studenttracker.dao.*;
import com.studenttracker.dao.impl.*;
import com.studenttracker.util.TestDatabaseConnection;

/**
 * Factory for creating DAO instances connected to test database.
 */
public class TestDAOFactory {
    
    private static final TestDatabaseConnection testDb = (TestDatabaseConnection)TestDatabaseConnection.getInstance();
    
    // Use a custom connection provider that uses test DB
    public static LessonDAO createLessonDAO() {
        return new LessonDAOImpl();
    }
    
    public static LessonTopicDAO createLessonTopicDAO() {
        return new LessonTopicDAOImpl();
    }
    
    public static UserDAO createUserDAO() {
        return new UserDAOImpl();
    }
    
    public static AttendanceDAO createAttendanceDAO() {
        return new AttendanceDAOImpl();
    }
    
    public static HomeworkDAO createHomeworkDAO() {
        return new HomeworkDAOImpl() ;
    }
    
    public static QuizDAO createQuizDAO() {
        return new QuizDAOImpl();
    }
}