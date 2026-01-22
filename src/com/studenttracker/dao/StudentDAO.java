package com.studenttracker.dao;

import com.studenttracker.model.Student;
import com.studenttracker.model.Student.StudentStatus;
import java.util.List;

/**
 * Data Access Object interface for Student entity operations.
 */
public interface StudentDAO {
    
    // Standard CRUD operations
    Integer insert(Student student);
    boolean update(Student student);
    boolean delete(int studentId);
    Student findById(int studentId);
    List<Student> findAll();
    
    // Custom query methods
    List<Student> findByStatus(StudentStatus status);
    List<Student> searchByName(String namePart);
    int countByStatus(StudentStatus status);
    boolean archive(int studentId, int archivedBy);
    boolean restore(int studentId);
    Student findByPhoneNumber(String phoneNumber);
}