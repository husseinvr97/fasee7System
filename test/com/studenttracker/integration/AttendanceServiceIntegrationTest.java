package com.studenttracker.integration;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.LessonDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.dao.impl.AttendanceDAOImpl;
import com.studenttracker.dao.impl.LessonDAOImpl;
import com.studenttracker.dao.impl.StudentDAOImpl;
import com.studenttracker.dao.impl.UserDAOImpl;
import com.studenttracker.exception.StudentAlreadyArchivedException;
import com.studenttracker.exception.StudentNotFoundException;
import com.studenttracker.model.*;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.AttendanceService;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.AttendanceBatchCompletedEvent;
import com.studenttracker.service.event.AttendanceMarkedEvent;
import com.studenttracker.service.impl.AttendanceServiceImpl;
import com.google.common.eventbus.Subscribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AttendanceServiceIntegrationTest extends BaseIntegrationTest {

    private AttendanceService attendanceService;
    private AttendanceDAO attendanceDAO;
    private StudentDAO studentDAO;
    private LessonDAO lessonDAO;
    private UserDAO userDAO;
    private EventBusService eventBus;
    
    private TestEventListener eventListener;
    
    private Integer testUserId;
    private Integer testLessonId;
    private List<Integer> testStudentIds;

    @BeforeEach
    void setUp() {
        // Initialize DAOs
        attendanceDAO = new AttendanceDAOImpl();
        studentDAO = new StudentDAOImpl();
        lessonDAO = new LessonDAOImpl();
        userDAO = new UserDAOImpl();
        
        // Initialize EventBus and listener
        eventBus = EventBusService.getInstance();
        eventListener = new TestEventListener();
        eventBus.register(eventListener);
        
        // Initialize service
        attendanceService = new AttendanceServiceImpl(attendanceDAO, studentDAO, eventBus);
        
        // Setup test data
        setupTestData();
    }
    
    private void setupTestData() {
        // Create a test user
        User testUser = new User("testadmin", "password123", "Test Admin", UserRole.ADMIN);
        testUserId = userDAO.insert(testUser);
        
        // Create a test lesson
        Lesson testLesson = new Lesson();
        testLesson.setLessonDate(LocalDate.now());
        testLesson.setMonthGroup("2025-01");
        testLesson.setCreatedBy(testUserId);
        testLesson.setCreatedAt(LocalDateTime.now());
        testLessonId = lessonDAO.insert(testLesson);
        
        // Create test students
        testStudentIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Student student = new Student();
            student.setFullName("Student " + i);
            student.setPhoneNumber("0100000000" + i);
            student.setParentPhoneNumber("0200000000" + i);
            student.setRegistrationDate(LocalDateTime.now());
            student.setStatus(StudentStatus.ACTIVE);
            Integer studentId = studentDAO.insert(student);
            testStudentIds.add(studentId);
        }
    }

    // Test 4.1: Mark Attendance - Single Student Present
    @Test
    void testMarkAttendance_SingleStudentPresent_Success() {
        // Arrange
        Integer studentId = testStudentIds.get(0);
        AttendanceStatus status = AttendanceStatus.PRESENT;
        
        // Act
        boolean result = attendanceService.markAttendance(testLessonId, studentId, status, testUserId);
        
        // Assert
        assertTrue(result);
        
        // Verify in database
        Attendance savedAttendance = attendanceDAO.findByLessonAndStudent(testLessonId, studentId);
        assertNotNull(savedAttendance);
        assertEquals(testLessonId, savedAttendance.getLessonId());
        assertEquals(studentId, savedAttendance.getStudentId());
        assertEquals(status, savedAttendance.getStatus());
        assertEquals(testUserId, savedAttendance.getMarkedBy());
        
        // Verify event was published
        assertTrue(eventListener.attendanceMarkedEvents.size() > 0);
        AttendanceMarkedEvent event = eventListener.attendanceMarkedEvents.get(0);
        assertEquals(testLessonId, event.getLessonId());
        assertEquals(studentId, event.getStudentId());
        assertEquals(status, event.getStatus());
    }

    // Test 4.2: Mark Attendance - Archived Student
    @Test
    void testMarkAttendance_ArchivedStudent_ThrowsException() {
        // Arrange - Archive a student
        Integer studentId = testStudentIds.get(0);
        studentDAO.archive(studentId, testUserId);
        
        // Act & Assert
        assertThrows(StudentAlreadyArchivedException.class, () -> {
            attendanceService.markAttendance(testLessonId, studentId, AttendanceStatus.PRESENT, testUserId);
        });
        
        // Verify no attendance was created
        Attendance attendance = attendanceDAO.findByLessonAndStudent(testLessonId, studentId);
        assertNull(attendance);
        
        // Verify no events were published
        assertEquals(0, eventListener.attendanceMarkedEvents.size());
    }

    // Test 4.3: Mark Attendance - Duplicate (Already Marked)
    @Test
    void testMarkAttendance_Duplicate_ThrowsException() {
        // Arrange - Mark attendance first time
        Integer studentId = testStudentIds.get(0);
        attendanceService.markAttendance(testLessonId, studentId, AttendanceStatus.PRESENT, testUserId);
        
        // Reset event listener
        eventListener.clear();
        
        // Act & Assert - Try to mark again
        assertThrows(Exception.class, () -> {
            attendanceService.markAttendance(testLessonId, studentId, AttendanceStatus.PRESENT, testUserId);
        });
    }

    // Test 4.4: Bulk Mark Attendance - Multiple Students
    @Test
    void testBulkMarkAttendance_MultipleStudents_Success() {
        // Arrange
        List<Attendance> attendanceList = Arrays.asList(
            new Attendance(testLessonId, testStudentIds.get(0), AttendanceStatus.PRESENT, testUserId),
            new Attendance(testLessonId, testStudentIds.get(1), AttendanceStatus.PRESENT, testUserId),
            new Attendance(testLessonId, testStudentIds.get(2), AttendanceStatus.ABSENT, testUserId),
            new Attendance(testLessonId, testStudentIds.get(3), AttendanceStatus.PRESENT, testUserId),
            new Attendance(testLessonId, testStudentIds.get(4), AttendanceStatus.ABSENT, testUserId)
        );
        
        // Act
        boolean result = attendanceService.bulkMarkAttendance(attendanceList, testUserId);
        
        // Assert
        assertTrue(result);
        
        // Verify all records in database
        List<Attendance> savedAttendances = attendanceDAO.findByLessonId(testLessonId);
        assertEquals(5, savedAttendances.size());
        
        // Verify counts
        long presentCount = savedAttendances.stream()
            .filter(Attendance::isPresent)
            .count();
        long absentCount = savedAttendances.stream()
            .filter(Attendance::isAbsent)
            .count();
        
        assertEquals(3, presentCount);
        assertEquals(2, absentCount);
        
        // Verify 5 individual AttendanceMarkedEvent published
        assertEquals(5, eventListener.attendanceMarkedEvents.size());
        
        // Verify 1 AttendanceBatchCompletedEvent published
        assertEquals(1, eventListener.batchCompletedEvents.size());
        AttendanceBatchCompletedEvent batchEvent = eventListener.batchCompletedEvents.get(0);
        assertEquals(testLessonId, batchEvent.getLessonId());
        assertEquals(5, batchEvent.getTotalStudents());
        assertEquals(3, batchEvent.getPresentCount());
        assertEquals(2, batchEvent.getAbsentCount());
    }

    // Test 4.5: Bulk Mark Attendance - Contains Archived Student
    @Test
    void testBulkMarkAttendance_ContainsArchivedStudent_ThrowsException() {
        // Arrange - Archive one student
        Integer archivedStudentId = testStudentIds.get(1);
        studentDAO.archive(archivedStudentId, testUserId);
        
        List<Attendance> attendanceList = Arrays.asList(
            new Attendance(testLessonId, testStudentIds.get(0), AttendanceStatus.PRESENT, testUserId),
            new Attendance(testLessonId, archivedStudentId, AttendanceStatus.PRESENT, testUserId),
            new Attendance(testLessonId, testStudentIds.get(2), AttendanceStatus.ABSENT, testUserId)
        );
        
        // Act & Assert
        assertThrows(StudentAlreadyArchivedException.class, () -> {
            attendanceService.bulkMarkAttendance(attendanceList, testUserId);
        });
        
        // Verify NO records were inserted (all-or-nothing)
        List<Attendance> savedAttendances = attendanceDAO.findByLessonId(testLessonId);
        assertEquals(0, savedAttendances.size());
        
        // Verify no events were published
        assertEquals(0, eventListener.attendanceMarkedEvents.size());
        assertEquals(0, eventListener.batchCompletedEvents.size());
    }

    // Test 4.6: Update Attendance - ABSENT to PRESENT
    @Test
    void testUpdateAttendance_AbsentToPresent_Success() {
        // Arrange - Create initial attendance as ABSENT
        Integer studentId = testStudentIds.get(0);
        attendanceService.markAttendance(testLessonId, studentId, AttendanceStatus.ABSENT, testUserId);
        
        Attendance savedAttendance = attendanceDAO.findByLessonAndStudent(testLessonId, studentId);
        Integer attendanceId = savedAttendance.getAttendanceId();
        
        // Reset event listener
        eventListener.clear();
        
        // Act - Update to PRESENT
        boolean result = attendanceService.updateAttendance(attendanceId, AttendanceStatus.PRESENT);
        
        // Assert
        assertTrue(result);
        
        // Verify in database
        Attendance updatedAttendance = attendanceDAO.findById(attendanceId);
        assertEquals(AttendanceStatus.PRESENT, updatedAttendance.getStatus());
        
        // Verify event was published
        assertEquals(1, eventListener.attendanceMarkedEvents.size());
        AttendanceMarkedEvent event = eventListener.attendanceMarkedEvents.get(0);
        assertEquals(AttendanceStatus.PRESENT, event.getStatus());
    }

    // Test 4.7: Get Student Attendance Rate
    @Test
    void testGetStudentAttendanceRate_Success() {
        // Arrange - Create attendance records for student
        Integer studentId = testStudentIds.get(0);
        
        // Create multiple lessons
        List<Integer> lessonIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Lesson lesson = new Lesson();
            lesson.setLessonDate(LocalDate.now().minusDays(i));
            lesson.setMonthGroup("2025-01");
            lesson.setCreatedBy(testUserId);
            lesson.setCreatedAt(LocalDateTime.now());
            lessonIds.add(lessonDAO.insert(lesson));
        }
        
        // Mark 6 PRESENT and 2 ABSENT (75% rate)
        for (int i = 0; i < 6; i++) {
            attendanceService.markAttendance(lessonIds.get(i), studentId, AttendanceStatus.PRESENT, testUserId);
        }
        for (int i = 6; i < 8; i++) {
            attendanceService.markAttendance(lessonIds.get(i), studentId, AttendanceStatus.ABSENT, testUserId);
        }
        
        // Act
        double rate = attendanceService.getStudentAttendanceRate(studentId);
        
        // Assert - 6/8 = 0.75
        assertEquals(0.75, rate, 0.01);
    }

    // Test 4.8: Get Consecutive Absences
    @Test
    void testGetConsecutiveAbsences_Success() {
        // Arrange - Create multiple lessons with specific pattern
        Integer studentId = testStudentIds.get(0);
        
        List<Integer> lessonIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Lesson lesson = new Lesson();
            lesson.setLessonDate(LocalDate.now().minusDays(6 - i)); // Oldest first
            lesson.setMonthGroup("2025-01");
            lesson.setCreatedBy(testUserId);
            lesson.setCreatedAt(LocalDateTime.now().minusDays(6 - i));
            lessonIds.add(lessonDAO.insert(lesson));
        }
        
        // Pattern: ABSENT, ABSENT, PRESENT, ABSENT, ABSENT, ABSENT
        attendanceService.markAttendance(lessonIds.get(0), studentId, AttendanceStatus.ABSENT, testUserId);
        attendanceService.markAttendance(lessonIds.get(1), studentId, AttendanceStatus.ABSENT, testUserId);
        attendanceService.markAttendance(lessonIds.get(2), studentId, AttendanceStatus.PRESENT, testUserId);
        attendanceService.markAttendance(lessonIds.get(3), studentId, AttendanceStatus.ABSENT, testUserId);
        attendanceService.markAttendance(lessonIds.get(4), studentId, AttendanceStatus.ABSENT, testUserId);
        attendanceService.markAttendance(lessonIds.get(5), studentId, AttendanceStatus.ABSENT, testUserId);
        
        // Act
        List<Attendance> consecutiveAbsences = attendanceService.getConsecutiveAbsences(studentId);
        
        // Assert - Should return the 3 most recent absences (lessons 3, 4, 5)
        assertNotNull(consecutiveAbsences);
        assertTrue(consecutiveAbsences.size() >= 3);
        
        // All should be ABSENT
        for (Attendance absence : consecutiveAbsences) {
            assertEquals(AttendanceStatus.ABSENT, absence.getStatus());
        }
    }

    // Test 4.9: Get Lesson Attendance Summary
    @Test
    void testGetLessonAttendanceSummary_Success() {
        // Arrange - Mark attendance for all 5 students
        attendanceService.markAttendance(testLessonId, testStudentIds.get(0), AttendanceStatus.PRESENT, testUserId);
        attendanceService.markAttendance(testLessonId, testStudentIds.get(1), AttendanceStatus.PRESENT, testUserId);
        attendanceService.markAttendance(testLessonId, testStudentIds.get(2), AttendanceStatus.PRESENT, testUserId);
        attendanceService.markAttendance(testLessonId, testStudentIds.get(3), AttendanceStatus.PRESENT, testUserId);
        attendanceService.markAttendance(testLessonId, testStudentIds.get(4), AttendanceStatus.ABSENT, testUserId);
        
        // Act
        AttendanceSummary summary = attendanceService.getLessonAttendanceSummary(testLessonId);
        
        // Assert
        assertNotNull(summary);
        assertEquals(5, summary.getTotalStudents());
        assertEquals(4, summary.getPresentCount());
        assertEquals(1, summary.getAbsentCount());
        assertEquals(80.0, summary.getAttendancePercentage(), 0.01); // 4/5 = 80%
    }

    // Helper class to capture events
    private static class TestEventListener {
        List<AttendanceMarkedEvent> attendanceMarkedEvents = new ArrayList<>();
        List<AttendanceBatchCompletedEvent> batchCompletedEvents = new ArrayList<>();
        
        @Subscribe
        public void onAttendanceMarked(AttendanceMarkedEvent event) {
            attendanceMarkedEvents.add(event);
        }
        
        @Subscribe
        public void onBatchCompleted(AttendanceBatchCompletedEvent event) {
            batchCompletedEvents.add(event);
        }
        
        public void clear() {
            attendanceMarkedEvents.clear();
            batchCompletedEvents.clear();
        }
    }
}