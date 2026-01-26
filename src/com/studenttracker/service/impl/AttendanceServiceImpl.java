package com.studenttracker.service.impl;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.exception.StudentAlreadyArchivedException;
import com.studenttracker.exception.StudentNotFoundException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.AttendanceSummary;
import com.studenttracker.model.Student;
import com.studenttracker.service.AttendanceService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.AttendanceBatchCompletedEvent;
import com.studenttracker.service.event.AttendanceMarkedEvent;

import java.time.LocalDateTime;
import java.util.List;

public class AttendanceServiceImpl implements AttendanceService {
    
    private final AttendanceDAO attendanceDAO;
    private final StudentDAO studentDAO;
    private final EventBusService eventBus;
    
    public AttendanceServiceImpl(AttendanceDAO attendanceDAO, StudentDAO studentDAO, EventBusService eventBus) {
        this.attendanceDAO = attendanceDAO;
        this.studentDAO = studentDAO;
        this.eventBus = eventBus;
    }
    
    @Override
    public boolean markAttendance(Integer lessonId, Integer studentId, 
                                 AttendanceStatus status, Integer markedBy) {
        // Validate student exists and is ACTIVE
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(studentId);
        }
        
        if (student.isArchived()) {
            throw new StudentAlreadyArchivedException(student.getStudentId());
        }
        
        // Create attendance record
        Attendance attendance = new Attendance(lessonId, studentId, status, markedBy);
        
        try {
            // Insert attendance
            Integer attendanceId = attendanceDAO.insert(attendance);
            attendance.setAttendanceId(attendanceId);
            
            // Publish event
            AttendanceMarkedEvent event = new AttendanceMarkedEvent(
                lessonId, studentId, status, markedBy, attendance.getMarkedAt()
            );
            eventBus.publish(event);
            
            return true;
            
        } catch (DAOException e) {
            throw new DAOException("Failed to mark attendance for student " + studentId, e);
        }
    }
    
    @Override
    public boolean bulkMarkAttendance(List<Attendance> attendanceList, Integer markedBy) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            return false;
        }
        
        // Validate all students are active
        for (Attendance attendance : attendanceList) {
            Student student = studentDAO.findById(attendance.getStudentId());
            if (student == null) {
                throw new StudentNotFoundException(attendance.getStudentId());
            }
            
            if (student.isArchived()) {
                throw new StudentAlreadyArchivedException(student.getStudentId());
            }
            
            // Set markedBy and markedAt if not set
            if (attendance.getMarkedBy() == null) {
                attendance.setMarkedBy(markedBy);
            }
            if (attendance.getMarkedAt() == null) {
                attendance.setMarkedAt(LocalDateTime.now());
            }
        }
        
        try {
            // Bulk insert
            boolean success = attendanceDAO.bulkInsert(attendanceList);
            
            if (!success) {
                return false;
            }
            
            // Publish individual events
            for (Attendance attendance : attendanceList) {
                AttendanceMarkedEvent event = new AttendanceMarkedEvent(
                    attendance.getLessonId(),
                    attendance.getStudentId(),
                    attendance.getStatus(),
                    attendance.getMarkedBy(),
                    attendance.getMarkedAt()
                );
                eventBus.publish(event);
            }
            
            // Calculate summary stats
            Integer lessonId = attendanceList.get(0).getLessonId();
            int totalStudents = attendanceList.size();
            int presentCount = 0;
            int absentCount = 0;
            
            for (Attendance attendance : attendanceList) {
                if (attendance.isPresent()) {
                    presentCount++;
                } else if (attendance.isAbsent()) {
                    absentCount++;
                }
            }
            
            // Publish batch completed event
            AttendanceBatchCompletedEvent batchEvent = new AttendanceBatchCompletedEvent(
                lessonId, totalStudents, presentCount, absentCount, markedBy
            );
            eventBus.publish(batchEvent);
            
            return true;
            
        } catch (DAOException e) {
            throw new DAOException("Failed to bulk mark attendance", e);
        }
    }
    
    @Override
    public boolean updateAttendance(Integer attendanceId, AttendanceStatus newStatus) {
        try {
            // Get existing attendance
            Attendance attendance = attendanceDAO.findById(attendanceId);
            if (attendance == null) {
                return false;
            }
            
            // Update status
            attendance.setStatus(newStatus);
            boolean success = attendanceDAO.update(attendance);
            
            if (success) {
                // Publish event
                AttendanceMarkedEvent event = new AttendanceMarkedEvent(
                    attendance.getLessonId(),
                    attendance.getStudentId(),
                    newStatus,
                    attendance.getMarkedBy(),
                    attendance.getMarkedAt()
                );
                eventBus.publish(event);
            }
            
            return success;
            
        } catch (DAOException e) {
            throw new DAOException("Failed to update attendance", e);
        }
    }
    
    @Override
    public Attendance getAttendance(Integer lessonId, Integer studentId) {
        try {
            return attendanceDAO.findByLessonAndStudent(lessonId, studentId);
        } catch (DAOException e) {
            throw new DAOException("Failed to get attendance", e);
        }
    }
    
    @Override
    public List<Attendance> getAttendanceByLesson(Integer lessonId) {
        try {
            return attendanceDAO.findByLessonId(lessonId);
        } catch (DAOException e) {
            throw new DAOException("Failed to get attendance by lesson", e);
        }
    }
    
    @Override
    public List<Attendance> getAttendanceByStudent(Integer studentId) {
        try {
            return attendanceDAO.findByStudentId(studentId);
        } catch (DAOException e) {
            throw new DAOException("Failed to get attendance by student", e);
        }
    }
    
    @Override
    public int getStudentAttendanceCount(Integer studentId, AttendanceStatus status) {
        try {
            return attendanceDAO.countByStudentAndStatus(studentId, status);
        } catch (DAOException e) {
            throw new DAOException("Failed to get attendance count", e);
        }
    }
    
    @Override
    public double getStudentAttendanceRate(Integer studentId) {
        try {
            return attendanceDAO.getAttendanceRate(studentId);
        } catch (DAOException e) {
            throw new DAOException("Failed to get attendance rate", e);
        }
    }
    
    @Override
    public AttendanceSummary getLessonAttendanceSummary(Integer lessonId) {
        try {
            List<Attendance> attendanceList = attendanceDAO.findByLessonId(lessonId);
            
            int totalStudents = attendanceList.size();
            int presentCount = 0;
            int absentCount = 0;
            
            for (Attendance attendance : attendanceList) {
                if (attendance.isPresent()) {
                    presentCount++;
                } else if (attendance.isAbsent()) {
                    absentCount++;
                }
            }
            
            return new AttendanceSummary(totalStudents, presentCount, absentCount);
            
        } catch (DAOException e) {
            throw new DAOException("Failed to get lesson attendance summary", e);
        }
    }
    
    @Override
    public List<Attendance> getConsecutiveAbsences(Integer studentId) {
        try {
            // Get up to 10 recent absences
            return attendanceDAO.findConsecutiveAbsences(studentId, 10);
        } catch (DAOException e) {
            throw new DAOException("Failed to get consecutive absences", e);
        }
    }
}