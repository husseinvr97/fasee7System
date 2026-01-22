package com.studenttracker.service;

import com.studenttracker.model.Student;
import java.util.List;

/**
 * Service interface for Student business operations.
 * Handles validation, business rules, and event publishing.
 */
public interface StudentService {
    
    // ========== CRUD Operations ==========
    
    /**
     * Registers a new student in the system.
     * 
     * @param fullName Student's full name (must be 4 parts - الاسم الرباعي)
     * @param phone Student's phone number (Egyptian format: 01XXXXXXXXX)
     * @param parentPhone Parent's phone number (Egyptian format: 01XXXXXXXXX)
     * @param whatsapp Student's WhatsApp number (optional)
     * @param parentWhatsapp Parent's WhatsApp number (optional)
     * @param registeredBy User ID who is registering the student
     * @return Generated student ID
     * @throws InvalidFullNameException if name is not 4 parts
     * @throws InvalidPhoneNumberException if phone format is invalid
     * @throws DuplicatePhoneNumberException if phone number already exists
     */
    Integer registerStudent(String fullName, String phone, String parentPhone, 
                          String whatsapp, String parentWhatsapp, Integer registeredBy);
    
    /**
     * Updates student information.
     * 
     * @param studentId The ID of the student to update
     * @param fullName Updated full name
     * @param phone Updated phone number
     * @param parentPhone Updated parent phone number
     * @param whatsapp Updated WhatsApp number
     * @param parentWhatsapp Updated parent WhatsApp number
     * @return true if update successful, false otherwise
     * @throws StudentNotFoundException if student doesn't exist
     * @throws InvalidFullNameException if name validation fails
     * @throws InvalidPhoneNumberException if phone validation fails
     */
    boolean updateStudentInfo(Integer studentId, String fullName, String phone, 
                            String parentPhone, String whatsapp, String parentWhatsapp);
    
    /**
     * Retrieves a student by ID.
     * 
     * @param studentId The student ID
     * @return Student object or null if not found
     */
    Student getStudentById(Integer studentId);
    
    /**
     * Retrieves all students (active and archived).
     * 
     * @return List of all students
     */
    List<Student> getAllStudents();
    
    /**
     * Retrieves only active students.
     * 
     * @return List of active students
     */
    List<Student> getActiveStudents();
    
    /**
     * Retrieves only archived students.
     * 
     * @return List of archived students
     */
    List<Student> getArchivedStudents();
    
    /**
     * Searches for students by name (partial match).
     * 
     * @param searchTerm Name search term
     * @return List of matching students
     */
    List<Student> searchStudentsByName(String searchTerm);
    
    
    // ========== Archive/Restore Operations ==========
    
    /**
     * Archives an active student.
     * 
     * @param studentId The student ID to archive
     * @param archivedBy User ID performing the archiving
     * @param reason Reason for archiving (e.g., "3 consecutive absences")
     * @return true if archiving successful, false otherwise
     * @throws StudentNotFoundException if student doesn't exist
     * @throws StudentAlreadyArchivedException if student is already archived
     */
    boolean archiveStudent(Integer studentId, Integer archivedBy, String reason);
    
    /**
     * Restores an archived student to active status.
     * 
     * @param studentId The student ID to restore
     * @param restoredBy User ID performing the restoration
     * @return true if restoration successful, false otherwise
     * @throws StudentNotFoundException if student doesn't exist
     * @throws StudentAlreadyActiveException if student is already active
     */
    boolean restoreStudent(Integer studentId, Integer restoredBy);
    
    
    // ========== Statistics ==========
    
    /**
     * Gets the count of active students.
     * 
     * @return Number of active students
     */
    int getActiveStudentCount();
    
    /**
     * Gets the count of archived students.
     * 
     * @return Number of archived students
     */
    int getArchivedStudentCount();
    
    
}