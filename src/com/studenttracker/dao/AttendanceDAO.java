package com.studenttracker.dao;

import com.studenttracker.model.Attendance;
import java.util.List;

public interface AttendanceDAO {
    
    // Standard CRUD operations
    Integer insert(Attendance attendance);
    boolean update(Attendance attendance);
    boolean delete(int attendanceId);
    Attendance findById(int attendanceId);
    List<Attendance> findAll();
    
    // Custom methods
    List<Attendance> findByLessonId(int lessonId);
    List<Attendance> findByStudentId(int studentId);
    Attendance findByLessonAndStudent(int lessonId, int studentId);
    int countByStudentAndStatus(int studentId, Attendance.AttendanceStatus status);
    List<Attendance> findConsecutiveAbsences(int studentId, int limit);
    boolean bulkInsert(List<Attendance> attendanceList);
    double getAttendanceRate(int studentId);
}