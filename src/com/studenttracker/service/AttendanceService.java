package com.studenttracker.service;

import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.AttendanceSummary;
import java.util.List;

public interface AttendanceService {
    
    // Mark Attendance (Single)
    boolean markAttendance(Integer lessonId, Integer studentId, 
                          AttendanceStatus status, Integer markedBy);
    
    // Bulk Attendance (Mission)
    boolean bulkMarkAttendance(List<Attendance> attendanceList, Integer markedBy);
    
    // Update Attendance
    boolean updateAttendance(Integer attendanceId, AttendanceStatus newStatus);
    
    // Retrieval
    Attendance getAttendance(Integer lessonId, Integer studentId);
    List<Attendance> getAttendanceByLesson(Integer lessonId);
    List<Attendance> getAttendanceByStudent(Integer studentId);
    
    // Statistics
    int getStudentAttendanceCount(Integer studentId, AttendanceStatus status);
    double getStudentAttendanceRate(Integer studentId);
    AttendanceSummary getLessonAttendanceSummary(Integer lessonId);
    List<Attendance> getConsecutiveAbsences(Integer studentId);
}