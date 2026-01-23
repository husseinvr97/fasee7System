package com.studenttracker.service.impl;

import com.google.common.eventbus.EventBus;
import com.studenttracker.dao.StudentDAO;

import com.studenttracker.exception.DuplicatePhoneNumberException;
import com.studenttracker.exception.StudentAlreadyActiveException;
import com.studenttracker.exception.StudentAlreadyArchivedException;
import com.studenttracker.exception.StudentNotFoundException;

import com.studenttracker.model.Student;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.service.ConsecutivityTrackingService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.StudentService;
import com.studenttracker.service.event.StudentArchivedEvent;
import com.studenttracker.service.event.StudentRegisteredEvent;
import com.studenttracker.service.event.StudentRestoredEvent;
import com.studenttracker.service.impl.helpers.StudentServiceImplHelpers;
import com.studenttracker.service.validator.NameValidator;
import com.studenttracker.service.validator.PhoneValidator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of StudentService.
 * Handles business logic, validation, and event publishing for student operations.
 */
public class StudentServiceImpl implements StudentService {
    
    private final StudentDAO studentDAO;
    private final EventBusService eventBusService;
    private final ConsecutivityTrackingService consecutivityService;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param studentDAO DAO for student data access
     * @param eventBus Event bus for publishing domain events
     * @param consecutivityService Service for tracking consecutive absences
     */
    public StudentServiceImpl(StudentDAO studentDAO, EventBus eventBus, 
                             ConsecutivityTrackingService consecutivityService) {
        this.studentDAO = studentDAO;
        this.eventBusService = EventBusService.getInstance();
        this.consecutivityService = consecutivityService;
    }
    
    
    // ========== CRUD Operations ==========
    
    @Override
    public Integer registerStudent(String fullName, String phone, String parentPhone, 
                                  String whatsapp, String parentWhatsapp, Integer registeredBy) {
        // Step 1: Validate full name (must be 4 parts)
        NameValidator.validateFullName(fullName);
        String normalizedName = StudentServiceImplHelpers.normalizeFullName(fullName);
        
        // Step 2: Validate phone numbers
        PhoneValidator.validatePhoneNumber(phone);
        PhoneValidator.validatePhoneNumber(parentPhone);
        
        // Validate optional WhatsApp numbers if provided
        if (whatsapp != null && !whatsapp.trim().isEmpty()) {
            PhoneValidator.validatePhoneNumber(whatsapp);
        }
        if (parentWhatsapp != null && !parentWhatsapp.trim().isEmpty()) {
            PhoneValidator.validatePhoneNumber(parentWhatsapp);
        }
        
        // Step 3: Check for duplicate phone number
        Student existingStudent = studentDAO.findByPhoneNumber(phone);
        if (existingStudent != null) {
            throw new DuplicatePhoneNumberException(phone);
        }
        
        // Step 4: Create and populate Student object
        Student newStudent = new Student();
        newStudent.setFullName(normalizedName);
        newStudent.setPhoneNumber(phone);
        newStudent.setParentPhoneNumber(parentPhone);
        newStudent.setWhatsappNumber(whatsapp);
        newStudent.setParentWhatsappNumber(parentWhatsapp);
        newStudent.setRegistrationDate(LocalDateTime.now());
        newStudent.setStatus(StudentStatus.ACTIVE);
        
        // Step 5: Insert into database
        Integer studentId = studentDAO.insert(newStudent);
        
        // Step 6: Publish StudentRegisteredEvent
        StudentRegisteredEvent event = new StudentRegisteredEvent(
            studentId,
            normalizedName,
            newStudent.getRegistrationDate(),
            registeredBy
        );
        eventBusService.publish(event);
        
        // Step 7: Return generated student ID
        return studentId;
    }
    
    @Override
    public boolean updateStudentInfo(Integer studentId, String fullName, String phone, 
                                    String parentPhone, String whatsapp, String parentWhatsapp) {
        // Step 1: Validate inputs
        NameValidator.validateFullName(fullName);
        PhoneValidator.validatePhoneNumber(phone);
        PhoneValidator.validatePhoneNumber(parentPhone);
        
        if (whatsapp != null && !whatsapp.trim().isEmpty()) {
            PhoneValidator.validatePhoneNumber(whatsapp);
        }
        if (parentWhatsapp != null && !parentWhatsapp.trim().isEmpty()) {
            PhoneValidator.validatePhoneNumber(parentWhatsapp);
        }
        
        // Step 2: Find existing student
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(studentId);
        }
        
        // Step 3: Check if phone number changed and if new number is taken
        if (!phone.equals(student.getPhoneNumber())) {
            Student existingWithPhone = studentDAO.findByPhoneNumber(phone);
            if (existingWithPhone != null && !existingWithPhone.getStudentId().equals(studentId)) {
                throw new DuplicatePhoneNumberException(phone);
            }
        }
        
        // Step 4: Update student fields
        student.setFullName(StudentServiceImplHelpers.normalizeFullName(fullName));
        student.setPhoneNumber(phone);
        student.setParentPhoneNumber(parentPhone);
        student.setWhatsappNumber(whatsapp);
        student.setParentWhatsappNumber(parentWhatsapp);
        
        // Step 5: Persist changes
        return studentDAO.update(student);
    }
    
    @Override
    public Student getStudentById(Integer studentId) {
        return studentDAO.findById(studentId);
    }
    
    @Override
    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }
    
    @Override
    public List<Student> getActiveStudents() {
        return studentDAO.findByStatus(StudentStatus.ACTIVE);
    }
    
    @Override
    public List<Student> getArchivedStudents() {
        return studentDAO.findByStatus(StudentStatus.ARCHIVED);
    }
    
    @Override
    public List<Student> searchStudentsByName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of(); // Return empty list for empty search
        }
        return studentDAO.searchByName(searchTerm.trim());
    }
    
    
    // ========== Archive/Restore Operations ==========
    
    @Override
    public boolean archiveStudent(Integer studentId, Integer archivedBy, String reason) {
        // Step 1: Find student
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(studentId);
        }
        
        // Step 2: Check if already archived
        if (student.isArchived()) {
            throw new StudentAlreadyArchivedException(studentId);
        }
        
        // Step 3: Archive the student
        boolean success = studentDAO.archive(studentId, archivedBy);
        
        if (!success) {
            return false;
        }
        
        // Step 4: Publish StudentArchivedEvent
        StudentArchivedEvent event = new StudentArchivedEvent(
            studentId,
            LocalDateTime.now(),
            archivedBy,
            reason
        );
        eventBusService.publish(event);
        
        return true;
    }
    
    @Override
    public boolean restoreStudent(Integer studentId, Integer restoredBy) {
        // Step 1: Find student
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(studentId);
        }
        
        // Step 2: Check if already active
        if (!student.isArchived()) {
            throw new StudentAlreadyActiveException(studentId);
        }
        
        // Step 3: Restore the student
        boolean success = studentDAO.restore(studentId);
        
        if (!success) {
            return false;
        }
        
        // Step 4: Reset consecutivity tracking
        // TODO: Implement when consecutivity module is ready
        try {
            consecutivityService.resetConsecutivity(studentId);
        } catch (Exception e) {
            // Log warning but don't fail the restore operation
            System.err.println("Warning: Failed to reset consecutivity for student " + 
                             studentId + ": " + e.getMessage());
        }
        
        // Step 5: Publish StudentRestoredEvent
        StudentRestoredEvent event = new StudentRestoredEvent(
            studentId,
            LocalDateTime.now(),
            restoredBy
        );
        eventBusService.publish(event);
        
        return true;
    }
    
    
    // ========== Statistics ==========
    
    @Override
    public int getActiveStudentCount() {
        return studentDAO.countByStatus(StudentStatus.ACTIVE);
    }
    
    @Override
    public int getArchivedStudentCount() {
        return studentDAO.countByStatus(StudentStatus.ARCHIVED);
    }
    
    
}