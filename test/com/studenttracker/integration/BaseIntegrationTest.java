package com.studenttracker.integration;

import com.studenttracker.util.DatabaseConnection;
import com.studenttracker.util.TestDatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;


public abstract  class BaseIntegrationTest {
    
    protected static TestDatabaseConnection testDb;
    
    @BeforeAll
    static void setUpDatabase() {
        testDb = new TestDatabaseConnection();
        DatabaseConnection.setTestInstance(testDb);
        testDb.initializeTestDatabase();
        System.out.println("✓ Integration test environment ready");
    }
    
    @BeforeEach
    void setUpTest() {
        testDb.clearAllTables();
    }
    
    @AfterEach
    void tearDownTest() {
        // Additional cleanup if needed
    }
    
    @AfterAll
    static void tearDownDatabase() {
        DatabaseConnection.clearTestInstance();
        System.out.println("✓ Integration test environment cleaned up");
    }
}