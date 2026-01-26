package com.studenttracker.service.impl;

import com.studenttracker.dao.*;
import com.studenttracker.exception.*;
import com.studenttracker.model.*;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.Homework.HomeworkStatus;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.HomeworkBatchCompletedEvent;
import com.studenttracker.service.event.HomeworkRecordedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HomeworkService Tests")
public class HomeworkServiceImplTest {

    private HomeworkServiceImpl homeworkService;
    private HomeworkDAO homeworkDAO;
    private StudentDAO studentDAO;
    private AttendanceDAO attendanceDAO;
    private EventBusService eventBusService;

    @BeforeEach
    void setUp() {
        homeworkDAO = mock(HomeworkDAO.class);
        studentDAO = mock(StudentDAO.class);
        attendanceDAO = mock(AttendanceDAO.class);
        eventBusService = mock(EventBusService.class);

        homeworkService = new HomeworkServiceImpl(homeworkDAO, studentDAO, attendanceDAO);
        
        // Mock EventBusService singleton
        EventBusService.getInstance();
    }

    // ========== Test 6.1: Mark Homework - Student Attended ==========
    @Test
    @DisplayName("Test 6.1: Mark Homework - Student Attended")
    void testMarkHomework_StudentAttended() {
        // Arrange
        Integer lessonId = 50;
        Integer studentId = 1;
        HomeworkStatus status = HomeworkStatus.DONE;
        Integer markedBy = 2;

        // Mock student - ACTIVE
        Student student = new Student("Student One", "1234567890", "0987654321");
        student.setStudentId(1);
        student.setStatus(StudentStatus.ACTIVE);
        when(studentDAO.findById(1)).thenReturn(student);

        // Mock attendance - PRESENT
        Attendance attendance = new Attendance(50, 1, AttendanceStatus.PRESENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 1)).thenReturn(attendance);

        // Mock homework insert
        when(homeworkDAO.insert(any(Homework.class))).thenReturn(100);

        // Act
        boolean result = homeworkService.markHomework(lessonId, studentId, status, markedBy);

        // Assert
        assertTrue(result);

        // Verify student validation
        verify(studentDAO, times(1)).findById(1);

        // Verify attendance check
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 1);

        // Verify homework insert
        ArgumentCaptor<Homework> homeworkCaptor = ArgumentCaptor.forClass(Homework.class);
        verify(homeworkDAO, times(1)).insert(homeworkCaptor.capture());
        Homework capturedHomework = homeworkCaptor.getValue();
        assertEquals(50, capturedHomework.getLessonId());
        assertEquals(1, capturedHomework.getStudentId());
        assertEquals(HomeworkStatus.DONE, capturedHomework.getStatus());
        assertEquals(2, capturedHomework.getMarkedBy());
    }

    // ========== Test 6.2: Mark Homework - Student Absent ==========
    @Test
    @DisplayName("Test 6.2: Mark Homework - Student Absent")
    void testMarkHomework_StudentAbsent() {
        // Arrange
        Integer lessonId = 50;
        Integer studentId = 3;
        HomeworkStatus status = HomeworkStatus.DONE;
        Integer markedBy = 2;

        // Mock student - ACTIVE
        Student student = new Student("Student Three", "1111111111", "2222222222");
        student.setStudentId(3);
        student.setStatus(StudentStatus.ACTIVE);
        when(studentDAO.findById(3)).thenReturn(student);

        // Mock attendance - ABSENT
        Attendance attendance = new Attendance(50, 3, AttendanceStatus.ABSENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 3)).thenReturn(attendance);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> homeworkService.markHomework(lessonId, studentId, status, markedBy)
        );

        assertEquals("Cannot mark homework for absent student", exception.getMessage());

        // Verify student validation happened
        verify(studentDAO, times(1)).findById(3);

        // Verify attendance check happened
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 3);

        // Verify NO insert
        verify(homeworkDAO, never()).insert(any(Homework.class));
    }

    // ========== Test 6.3: Mark Homework - Student Didn't Attend (No Attendance Record) ==========
    @Test
    @DisplayName("Test 6.3: Mark Homework - Student Didn't Attend (No Attendance Record)")
    void testMarkHomework_NoAttendanceRecord() {
        // Arrange
        Integer lessonId = 50;
        Integer studentId = 5;
        HomeworkStatus status = HomeworkStatus.DONE;
        Integer markedBy = 2;

        // Mock student - ACTIVE
        Student student = new Student("Student Five", "3333333333", "4444444444");
        student.setStudentId(5);
        student.setStatus(StudentStatus.ACTIVE);
        when(studentDAO.findById(5)).thenReturn(student);

        // Mock attendance - NO RECORD (null)
        when(attendanceDAO.findByLessonAndStudent(50, 5)).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> homeworkService.markHomework(lessonId, studentId, status, markedBy)
        );

        assertEquals("Student has no attendance record for this lesson", exception.getMessage());

        // Verify student validation happened
        verify(studentDAO, times(1)).findById(5);

        // Verify attendance check happened
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 5);

        // Verify NO insert
        verify(homeworkDAO, never()).insert(any(Homework.class));
    }

    // ========== Test 6.4: Bulk Mark Homework - Multiple Students ==========
    @Test
    @DisplayName("Test 6.4: Bulk Mark Homework - Multiple Students")
    void testBulkMarkHomework_MultipleStudents() {
        // Arrange
        Integer markedBy = 2;
        
        // Create 3 homework records
        List<Homework> homeworkList = Arrays.asList(
                new Homework(50, 1, HomeworkStatus.DONE, 2),
                new Homework(50, 2, HomeworkStatus.PARTIALLY_DONE, 2),
                new Homework(50, 4, HomeworkStatus.NOT_DONE, 2)
        );

        // Mock students - all ACTIVE
        Student student1 = new Student("Student One", "1111111111", "2222222222");
        student1.setStudentId(1);
        student1.setStatus(StudentStatus.ACTIVE);
        
        Student student2 = new Student("Student Two", "3333333333", "4444444444");
        student2.setStudentId(2);
        student2.setStatus(StudentStatus.ACTIVE);
        
        Student student4 = new Student("Student Four", "5555555555", "6666666666");
        student4.setStudentId(4);
        student4.setStatus(StudentStatus.ACTIVE);

        when(studentDAO.findById(1)).thenReturn(student1);
        when(studentDAO.findById(2)).thenReturn(student2);
        when(studentDAO.findById(4)).thenReturn(student4);

        // Mock attendance - all PRESENT
        Attendance attendance1 = new Attendance(50, 1, AttendanceStatus.PRESENT, 2);
        Attendance attendance2 = new Attendance(50, 2, AttendanceStatus.PRESENT, 2);
        Attendance attendance4 = new Attendance(50, 4, AttendanceStatus.PRESENT, 2);

        when(attendanceDAO.findByLessonAndStudent(50, 1)).thenReturn(attendance1);
        when(attendanceDAO.findByLessonAndStudent(50, 2)).thenReturn(attendance2);
        when(attendanceDAO.findByLessonAndStudent(50, 4)).thenReturn(attendance4);

        // Mock bulk insert
        when(homeworkDAO.bulkInsert(homeworkList)).thenReturn(true);

        // Act
        boolean result = homeworkService.bulkMarkHomework(homeworkList, markedBy);

        // Assert
        assertTrue(result);

        // Verify all students validated
        verify(studentDAO, times(1)).findById(1);
        verify(studentDAO, times(1)).findById(2);
        verify(studentDAO, times(1)).findById(4);

        // Verify all attendance checked
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 1);
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 2);
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 4);

        // Verify bulk insert
        verify(homeworkDAO, times(1)).bulkInsert(homeworkList);
    }

    // ========== Test 6.5: Calculate Homework Points ==========
    @Test
    @DisplayName("Test 6.5: Calculate Homework Points")
    void testCalculateHomeworkPoints() {
        // Arrange
        Integer studentId = 1;

        // Create homework records: 10 DONE, 5 PARTIALLY_DONE, 2 NOT_DONE
        List<Homework> homeworkList = new ArrayList<>();
        
        // 10 DONE (3 points each)
        for (int i = 0; i < 10; i++) {
            Homework hw = new Homework(i + 1, 1, HomeworkStatus.DONE, 2);
            homeworkList.add(hw);
        }
        
        // 5 PARTIALLY_DONE (1 point each)
        for (int i = 0; i < 5; i++) {
            Homework hw = new Homework(i + 11, 1, HomeworkStatus.PARTIALLY_DONE, 2);
            homeworkList.add(hw);
        }
        
        // 2 NOT_DONE (0 points)
        for (int i = 0; i < 2; i++) {
            Homework hw = new Homework(i + 16, 1, HomeworkStatus.NOT_DONE, 2);
            homeworkList.add(hw);
        }

        when(homeworkDAO.findByStudentId(1)).thenReturn(homeworkList);

        // Act
        int totalPoints = homeworkService.calculateHomeworkPoints(studentId);

        // Assert
        // (10 × 3) + (5 × 1) + (2 × 0) = 30 + 5 + 0 = 35
        assertEquals(35, totalPoints);
        verify(homeworkDAO, times(1)).findByStudentId(1);
    }

    // ========== Test 6.6: Get Lesson Homework Summary ==========
    @Test
    @DisplayName("Test 6.6: Get Lesson Homework Summary")
    void testGetLessonHomeworkSummary() {
        // Arrange
        Integer lessonId = 50;

        // Mock homework statistics
        Map<HomeworkStatus, Integer> stats = new HashMap<>();
        stats.put(HomeworkStatus.DONE, 110);
        stats.put(HomeworkStatus.PARTIALLY_DONE, 25);
        stats.put(HomeworkStatus.NOT_DONE, 10);

        when(homeworkDAO.getHomeworkStatsByLesson(50)).thenReturn(stats);

        // Act
        HomeworkSummary summary = homeworkService.getLessonHomeworkSummary(lessonId);

        // Assert
        assertNotNull(summary);
        assertEquals(145, summary.getTotalStudents());
        assertEquals(110, summary.getDoneCount());
        assertEquals(25, summary.getPartiallyDoneCount());
        assertEquals(10, summary.getNotDoneCount());

        verify(homeworkDAO, times(1)).getHomeworkStatsByLesson(50);
    }

    // ========== Test 6.7: Update Homework Status ==========
    @Test
    @DisplayName("Test 6.7: Update Homework Status")
    void testUpdateHomeworkStatus() {
        // Arrange
        Integer homeworkId = 200;
        HomeworkStatus newStatus = HomeworkStatus.DONE;

        // Mock existing homework
        Homework existingHomework = new Homework(50, 1, HomeworkStatus.PARTIALLY_DONE, 2);
        existingHomework.setHomeworkId(200);
        when(homeworkDAO.findById(200)).thenReturn(existingHomework);

        // Mock update success
        when(homeworkDAO.update(any(Homework.class))).thenReturn(true);

        // Act
        boolean result = homeworkService.updateHomework(homeworkId, newStatus);

        // Assert
        assertTrue(result);

        // Verify findById called
        verify(homeworkDAO, times(1)).findById(200);

        // Verify update called
        ArgumentCaptor<Homework> homeworkCaptor = ArgumentCaptor.forClass(Homework.class);
        verify(homeworkDAO, times(1)).update(homeworkCaptor.capture());
        Homework updatedHomework = homeworkCaptor.getValue();
        assertEquals(HomeworkStatus.DONE, updatedHomework.getStatus());
    }
}