package com.studenttracker.dao;

import com.studenttracker.model.Student;
import java.util.List;

public interface StudentDAO {
    
    // Standard CRUD operations
    Integer insert(Student student);
    boolean update(Student student);
    boolean delete(int studentId);
    Student findById(int studentId);
    List<Student> findAll();
    
    // Custom methods
    List<Student> findByStatus(Student.StudentStatus status);
    List<Student> searchByName(String namePart);
    int countByStatus(Student.StudentStatus status);
    boolean archive(int studentId, int archivedBy);
    boolean restore(int studentId);
    Student findByPhoneNumber(String phoneNumber);
}