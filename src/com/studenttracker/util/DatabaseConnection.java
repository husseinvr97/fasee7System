package com.studenttracker.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.studenttracker.exception.DAOException;

/**
 * Singleton utility class for managing SQLite database connections.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private static final String DB_URL = "jdbc:sqlite:student_performance.db";
    
    private DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new DAOException("SQLite JDBC driver not found", e);
        }
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            throw new DAOException("Failed to establish database connection", e);
        }
    }
    
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Warning: Failed to close connection - " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize database by creating all tables if they don't exist.
     * This should be called once when the application starts.
     */
    public void initializeDatabase() {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Create students table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS students (" +
                "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "student_name TEXT NOT NULL, " +
                "phone_number TEXT UNIQUE NOT NULL, " +
                "status TEXT NOT NULL CHECK(status IN ('ACTIVE', 'ARCHIVED')), " +
                "created_at TEXT NOT NULL, " +
                "archived_at TEXT, " +
                "archived_by INTEGER, " +
                "FOREIGN KEY (archived_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "role TEXT NOT NULL CHECK(role IN ('ADMIN', 'ASSISTANT')), " +
                "created_at TEXT NOT NULL" +
                ")"
            );
            
            // Create lessons table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS lessons (" +
                "lesson_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lesson_date TEXT NOT NULL, " +
                "month_group TEXT NOT NULL, " +
                "created_at TEXT NOT NULL, " +
                "created_by INTEGER NOT NULL, " +
                "FOREIGN KEY (created_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create lesson_topics table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS lesson_topics (" +
                "topic_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lesson_id INTEGER NOT NULL, " +
                "category TEXT NOT NULL, " +
                "specific_topic TEXT NOT NULL, " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Create attendance table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS attendance (" +
                "attendance_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lesson_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "status TEXT NOT NULL CHECK(status IN ('PRESENT', 'ABSENT')), " +
                "entered_at TEXT NOT NULL, " +
                "entered_by INTEGER NOT NULL, " +
                "UNIQUE(lesson_id, student_id), " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                "FOREIGN KEY (entered_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create homework table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS homework (" +
                "homework_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lesson_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "status TEXT NOT NULL CHECK(status IN ('DONE', 'PARTIAL', 'NOT_DONE')), " +
                "entered_at TEXT NOT NULL, " +
                "entered_by INTEGER NOT NULL, " +
                "UNIQUE(lesson_id, student_id), " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                "FOREIGN KEY (entered_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create quizzes table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS quizzes (" +
                "quiz_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lesson_id INTEGER UNIQUE NOT NULL, " +
                "quiz_name TEXT NOT NULL, " +
                "quiz_pdf BLOB NOT NULL, " +
                "uploaded_at TEXT NOT NULL, " +
                "uploaded_by INTEGER NOT NULL, " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (uploaded_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create quiz_questions table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS quiz_questions (" +
                "question_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_id INTEGER NOT NULL, " +
                "question_number INTEGER NOT NULL, " +
                "question_type TEXT NOT NULL CHECK(question_type IN ('MCQ', 'TRUE_FALSE', 'ESSAY', 'SHORT_ANSWER')), " +
                "category TEXT NOT NULL, " +
                "total_points REAL NOT NULL, " +
                "UNIQUE(quiz_id, question_number), " +
                "FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Create quiz_scores table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS quiz_scores (" +
                "score_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "question_id INTEGER NOT NULL, " +
                "points_earned REAL NOT NULL, " +
                "entered_at TEXT NOT NULL, " +
                "entered_by INTEGER NOT NULL, " +
                "FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                "FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id), " +
                "FOREIGN KEY (entered_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create quiz_category_totals table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS quiz_category_totals (" +
                "total_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "quiz_id INTEGER NOT NULL, " +
                "student_id INTEGER NOT NULL, " +
                "category TEXT NOT NULL, " +
                "points_earned REAL NOT NULL, " +
                "total_points REAL NOT NULL, " +
                "FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id)" +
                ")"
            );
            
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            throw new DAOException("Failed to initialize database", e);
        } finally {
            closeConnection(conn);
        }
    }
}