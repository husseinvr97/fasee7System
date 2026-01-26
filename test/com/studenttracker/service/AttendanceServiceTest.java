package com.studenttracker.service;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.exception.DAOException;
import com.studenttracker.exception.StudentAlreadyArchivedException;
import com.studenttracker.exception.StudentNotFoundException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.AttendanceSummary;
import com.studenttracker.model.Student;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.service.event.AttendanceBatchCompletedEvent;
import com.studenttracker.service.event.AttendanceMarkedEvent;
import com.studenttracker.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class AttendanceServiceTest {

    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    private EventBusService eventBus;
    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceDAO = mock(AttendanceDAO.class);
        studentDAO = mock(StudentDAO.class);
        eventBus = mock(EventBusService.class);
        attendanceService = new AttendanceServiceImpl(attendanceDAO, studentDAO , eventBus);
    }

    // Test 4.1: Mark Attendance - Single Student Present
    @Test
    void testMarkAttendance_SingleStudentPresent_Success() {
        // Arrange
        Integer lessonId = 50;
        Integer studentId = 1;
        AttendanceStatus status = AttendanceStatus.PRESENT;
        Integer markedBy = 2;

        Student activeStudent = new Student();
        activeStudent.setStudentId(studentId);
        activeStudent.setStatus(StudentStatus.ACTIVE);

        when(studentDAO.findById(studentId)).thenReturn(activeStudent);
        when(attendanceDAO.insert(any(Attendance.class))).thenReturn(100);

        // Act
        boolean result = attendanceService.markAttendance(lessonId, studentId, status, markedBy);

        // Assert
        assertTrue(result);
        verify(studentDAO, times(1)).findById(studentId);
        verify(attendanceDAO, times(1)).insert(any(Attendance.class));
        
        ArgumentCaptor<AttendanceMarkedEvent> eventCaptor = ArgumentCaptor.forClass(AttendanceMarkedEvent.class);
        verify(eventBus, times(1)).publish(eventCaptor.capture());
        
        AttendanceMarkedEvent event = eventCaptor.getValue();
        assertEquals(lessonId, event.getLessonId());
        assertEquals(studentId, event.getStudentId());
        assertEquals(status, event.getStatus());
    }

    // Test 4.2: Mark Attendance - Archived Student
    @Test
    void testMarkAttendance_ArchivedStudent_ThrowsException() {
        // Arrange
        Integer lessonId = 50;
        Integer studentId = 3;
        AttendanceStatus status = AttendanceStatus.PRESENT;
        Integer markedBy = 2;

        Student archivedStudent = new Student();
        archivedStudent.setStudentId(studentId);
        archivedStudent.setStatus(StudentStatus.ARCHIVED);

        when(studentDAO.findById(studentId)).thenReturn(archivedStudent);

        // Act & Assert
        assertThrows(StudentAlreadyArchivedException.class, () -> {
            attendanceService.markAttendance(lessonId, studentId, status, markedBy);
        });

        verify(attendanceDAO, never()).insert(any(Attendance.class));
        verify(eventBus, never()).publish(any());
    }

    // Test 4.3: Mark Attendance - Duplicate (Already Marked)
    @Test
    void testMarkAttendance_Duplicate_ThrowsException() {
        // Arrange
        Integer lessonId = 50;
        Integer studentId = 1;
        AttendanceStatus status = AttendanceStatus.PRESENT;
        Integer markedBy = 2;

        Student activeStudent = new Student();
        activeStudent.setStudentId(studentId);
        activeStudent.setStatus(StudentStatus.ACTIVE);

        when(studentDAO.findById(studentId)).thenReturn(activeStudent);
        when(attendanceDAO.insert(any(Attendance.class)))
            .thenThrow(new DAOException("Duplicate entry"));

        // Act & Assert
        assertThrows(DAOException.class, () -> {
            attendanceService.markAttendance(lessonId, studentId, status, markedBy);
        });
    }

    // Test 4.4: Bulk Mark Attendance - Multiple Students
    @Test
    void testBulkMarkAttendance_MultipleStudents_Success() {
        // Arrange
        Integer markedBy = 2;
        List<Attendance> attendanceList = Arrays.asList(
            new Attendance(50, 1, AttendanceStatus.PRESENT, markedBy),
            new Attendance(50, 2, AttendanceStatus.PRESENT, markedBy),
            new Attendance(50, 3, AttendanceStatus.ABSENT, markedBy),
            new Attendance(50, 4, AttendanceStatus.PRESENT, markedBy),
            new Attendance(50, 5, AttendanceStatus.ABSENT, markedBy)
        );

        // Mock all students as active
        for (int i = 1; i <= 5; i++) {
            Student student = new Student();
            student.setStudentId(i);
            student.setStatus(StudentStatus.ACTIVE);
            when(studentDAO.findById(i)).thenReturn(student);
        }

        when(attendanceDAO.bulkInsert(attendanceList)).thenReturn(true);

        // Act
        boolean result = attendanceService.bulkMarkAttendance(attendanceList, markedBy);

        // Assert
        assertTrue(result);
        verify(attendanceDAO, times(1)).bulkInsert(attendanceList);
        
        // Verify 5 individual AttendanceMarkedEvent published
        ArgumentCaptor<AttendanceMarkedEvent> markedEventCaptor = ArgumentCaptor.forClass(AttendanceMarkedEvent.class);
        verify(eventBus, times(5)).publish(markedEventCaptor.capture());
        
        // Verify 1 AttendanceBatchCompletedEvent published
        ArgumentCaptor<AttendanceBatchCompletedEvent> batchEventCaptor = ArgumentCaptor.forClass(AttendanceBatchCompletedEvent.class);
        verify(eventBus, times(1)).publish(batchEventCaptor.capture());
        
        AttendanceBatchCompletedEvent batchEvent = batchEventCaptor.getValue();
        assertEquals(50, batchEvent.getLessonId());
        assertEquals(5, batchEvent.getTotalStudents());
        assertEquals(3, batchEvent.getPresentCount());
        assertEquals(2, batchEvent.getAbsentCount());
    }

    // Test 4.5: Bulk Mark Attendance - Contains Archived Student
    @Test
    void testBulkMarkAttendance_ContainsArchivedStudent_ThrowsException() {
        // Arrange
        Integer markedBy = 2;
        List<Attendance> attendanceList = Arrays.asList(
            new Attendance(50, 1, AttendanceStatus.PRESENT, markedBy),
            new Attendance(50, 3, AttendanceStatus.PRESENT, markedBy),
            new Attendance(50, 5, AttendanceStatus.ABSENT, markedBy)
        );

        // Mock student 1 as active
        Student student1 = new Student();
        student1.setStudentId(1);
        student1.setStatus(StudentStatus.ACTIVE);
        when(studentDAO.findById(1)).thenReturn(student1);

        // Mock student 3 as archived
        Student student3 = new Student();
        student3.setStudentId(3);
        student3.setStatus(StudentStatus.ARCHIVED);
        when(studentDAO.findById(3)).thenReturn(student3);

        // Act & Assert
        assertThrows(StudentAlreadyArchivedException.class, () -> {
            attendanceService.bulkMarkAttendance(attendanceList, markedBy);
        });

        verify(attendanceDAO, never()).bulkInsert(any());
        verify(eventBus, never()).publish(any());
    }

    // Test 4.6: Update Attendance - ABSENT to PRESENT
    @Test
    void testUpdateAttendance_AbsentToPresent_Success() {
        // Arrange
        Integer attendanceId = 123;
        AttendanceStatus newStatus = AttendanceStatus.PRESENT;

        Attendance existingAttendance = new Attendance(50, 1, AttendanceStatus.ABSENT, 2);
        existingAttendance.setAttendanceId(attendanceId);

        when(attendanceDAO.findById(attendanceId)).thenReturn(existingAttendance);
        when(attendanceDAO.update(any(Attendance.class))).thenReturn(true);

        // Act
        boolean result = attendanceService.updateAttendance(attendanceId, newStatus);

        // Assert
        assertTrue(result);
        verify(attendanceDAO, times(1)).update(any(Attendance.class));
        
        ArgumentCaptor<AttendanceMarkedEvent> eventCaptor = ArgumentCaptor.forClass(AttendanceMarkedEvent.class);
        verify(eventBus, times(1)).publish(eventCaptor.capture());
        
        AttendanceMarkedEvent event = eventCaptor.getValue();
        assertEquals(newStatus, event.getStatus());
    }

    // Test 4.7: Get Student Attendance Rate
    @Test
    void testGetStudentAttendanceRate_Success() {
        // Arrange
        Integer studentId = 1;
        double expectedRate = 93.75;

        when(attendanceDAO.getAttendanceRate(studentId)).thenReturn(expectedRate);

        // Act
        double result = attendanceService.getStudentAttendanceRate(studentId);

        // Assert
        assertEquals(expectedRate, result, 0.01);
        verify(attendanceDAO, times(1)).getAttendanceRate(studentId);
    }

    // Test 4.8: Get Consecutive Absences
    @Test
    void testGetConsecutiveAbsences_Success() {
        // Arrange
        Integer studentId = 1;
        List<Attendance> expectedAbsences = Arrays.asList(
            new Attendance(50, studentId, AttendanceStatus.ABSENT, 2),
            new Attendance(49, studentId, AttendanceStatus.ABSENT, 2),
            new Attendance(48, studentId, AttendanceStatus.ABSENT, 2)
        );

        when(attendanceDAO.findConsecutiveAbsences(studentId, 10)).thenReturn(expectedAbsences);

        // Act
        List<Attendance> result = attendanceService.getConsecutiveAbsences(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(attendanceDAO, times(1)).findConsecutiveAbsences(studentId, 10);
    }

    // Test 4.9: Get Lesson Attendance Summary
    @Test
    void testGetLessonAttendanceSummary_Success() {
        // Arrange
        Integer lessonId = 50;
        List<Attendance> attendanceList = new ArrayList<>();
        
        // Add 145 PRESENT
        for (int i = 1; i <= 145; i++) {
            attendanceList.add(new Attendance(lessonId, i, AttendanceStatus.PRESENT, 2));
        }
        
        // Add 7 ABSENT
        for (int i = 146; i <= 152; i++) {
            attendanceList.add(new Attendance(lessonId, i, AttendanceStatus.ABSENT, 2));
        }

        when(attendanceDAO.findByLessonId(lessonId)).thenReturn(attendanceList);

        // Act
        AttendanceSummary result = attendanceService.getLessonAttendanceSummary(lessonId);

        // Assert
        assertNotNull(result);
        assertEquals(152, result.getTotalStudents());
        assertEquals(145, result.getPresentCount());
        assertEquals(7, result.getAbsentCount());
        assertEquals(95.39, result.getAttendancePercentage(), 0.01);
        verify(attendanceDAO, times(1)).findByLessonId(lessonId);
    }
}