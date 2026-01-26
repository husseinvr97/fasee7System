package com.studenttracker.util;

import com.studenttracker.exception.DAOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabaseConnection extends DatabaseConnection {
    private static final String TEST_DB_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    
    public TestDatabaseConnection() {
        super();
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new DAOException("H2 JDBC driver not found", e);
        }
    }
    
    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(TEST_DB_URL, "sa", "");
        } catch (SQLException e) {
            throw new DAOException("Failed to establish test database connection", e);
        }
    }
    
    public void initializeTestDatabase() {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            
            // Create users table FIRST (because of foreign keys)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "full_name VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL CHECK(role IN ('ADMIN', 'ASSISTANT')), " +
                "created_at TIMESTAMP NOT NULL, " +
                "is_active BOOLEAN DEFAULT TRUE" +
                ")"
            );
            
            stmt.execute(
    "CREATE TABLE IF NOT EXISTS students (" +
    "student_id INT PRIMARY KEY AUTO_INCREMENT, " +
    "full_name VARCHAR(255) NOT NULL, " +
    "phone_number VARCHAR(20) UNIQUE NOT NULL, " +
    "whatsapp_number VARCHAR(20), " +
    "parent_phone_number VARCHAR(20), " +
    "parent_whatsapp_number VARCHAR(20), " +
    "registration_date TIMESTAMP NOT NULL, " +
    "status VARCHAR(20) NOT NULL CHECK(status IN ('ACTIVE', 'ARCHIVED')), " +
    "archived_at TIMESTAMP, " +
    "archived_by INT, " +
    "FOREIGN KEY (archived_by) REFERENCES users(user_id)" +
    ")"
);
            
            // Create lessons table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS lessons (" +
                "lesson_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "lesson_date DATE NOT NULL, " +
                "month_group VARCHAR(50) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "created_by INT NOT NULL, " +
                "FOREIGN KEY (created_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create lesson_topics table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS lesson_topics (" +
                "topic_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "lesson_id INT NOT NULL, " +
                "category VARCHAR(50) NOT NULL, " +
                "specific_topic VARCHAR(255) NOT NULL, " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE" +
                ")"
            );
            
            // Create attendance table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS attendance (" +
                "attendance_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "lesson_id INT NOT NULL, " +
                "student_id INT NOT NULL, " +
                "status VARCHAR(20) NOT NULL CHECK(status IN ('PRESENT', 'ABSENT')), " +
                "marked_at TIMESTAMP NOT NULL, " +
                "marked_by INT NOT NULL, " +
                "UNIQUE(lesson_id, student_id), " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                "FOREIGN KEY (marked_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create homework table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS homework (" +
                "homework_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "lesson_id INT NOT NULL, " +
                "student_id INT NOT NULL, " +
                "status VARCHAR(20) NOT NULL CHECK(status IN ('DONE', 'PARTIALLY_DONE', 'NOT_DONE')), " +
                "marked_at TIMESTAMP NOT NULL, " +
                "marked_by INT NOT NULL, " +
                "UNIQUE(lesson_id, student_id), " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                "FOREIGN KEY (marked_by) REFERENCES users(user_id)" +
                ")"
            );
            
            // Create quizzes table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS quizzes (" +
                "quiz_id INT PRIMARY KEY AUTO_INCREMENT, " +
                "lesson_id INT UNIQUE NOT NULL, " +
                "quiz_pdf_data BLOB NOT NULL, " +
                "total_marks DECIMAL(5,2) NOT NULL, " +
                "created_at TIMESTAMP NOT NULL, " +
                "created_by INT NOT NULL, " +
                "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (created_by) REFERENCES users(user_id)" +
                ")"
            );

            // Create missions table
stmt.execute(
    "CREATE TABLE IF NOT EXISTS missions (" +
    "mission_id INT PRIMARY KEY AUTO_INCREMENT, " +
    "lesson_id INT NOT NULL, " +
    "mission_type VARCHAR(50) NOT NULL CHECK(mission_type IN ('ATTENDANCE_HOMEWORK', 'QUIZ_GRADING')), " +
    "assigned_to INT NOT NULL, " +
    "assigned_by INT NOT NULL, " +
    "assigned_at TIMESTAMP NOT NULL, " +
    "status VARCHAR(20) NOT NULL CHECK(status IN ('IN_PROGRESS', 'COMPLETED')), " +
    "completed_at TIMESTAMP, " +
    "FOREIGN KEY (lesson_id) REFERENCES lessons(lesson_id) ON DELETE CASCADE, " +
    "FOREIGN KEY (assigned_to) REFERENCES users(user_id), " +
    "FOREIGN KEY (assigned_by) REFERENCES users(user_id)" +
    ")"
);

// Create mission_drafts table
stmt.execute(
    "CREATE TABLE IF NOT EXISTS mission_drafts (" +
    "draft_id INT PRIMARY KEY AUTO_INCREMENT, " +
    "mission_id INT UNIQUE NOT NULL, " +
    "draft_data TEXT NOT NULL, " +
    "last_saved TIMESTAMP NOT NULL, " +
    "FOREIGN KEY (mission_id) REFERENCES missions(mission_id) ON DELETE CASCADE" +
    ")"
);
            
            System.out.println("✓ Test database schema created successfully!");
            
        } catch (SQLException e) {
            throw new DAOException("Failed to initialize test database", e);
        } finally {
            closeConnection(conn);
        }
    }
    
    public void clearAllTables() {
        Connection conn = null;
        try {
            conn = getConnection();
            Statement stmt = conn.createStatement();
            
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            stmt.execute("TRUNCATE TABLE homework");
            stmt.execute("TRUNCATE TABLE attendance");
            stmt.execute("TRUNCATE TABLE quizzes");
            stmt.execute("TRUNCATE TABLE lesson_topics");
            stmt.execute("TRUNCATE TABLE lessons");
            stmt.execute("TRUNCATE TABLE students");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            stmt.execute("TRUNCATE TABLE mission_drafts");
stmt.execute("TRUNCATE TABLE missions");
            
        } catch (SQLException e) {
            throw new DAOException("Failed to clear test database", e);
        } finally {
            closeConnection(conn);
        }
    }
}