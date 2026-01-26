package com.studenttracker.service.impl;

import com.studenttracker.dao.*;
import com.studenttracker.exception.*;
import com.studenttracker.model.*;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.Homework.HomeworkStatus;
import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.event.LessonCreatedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("LessonService Tests")
public class LessonServiceImplTest {

    private LessonServiceImpl lessonService;
    private LessonDAO lessonDAO;
    private LessonTopicDAO lessonTopicDAO;
    private QuizDAO quizDAO;
    private AttendanceDAO attendanceDAO;
    private HomeworkDAO homeworkDAO;
    private UserDAO userDAO;
    private EventBusService eventBusService;

    @BeforeEach
    void setUp() {
        lessonDAO = mock(LessonDAO.class);
        lessonTopicDAO = mock(LessonTopicDAO.class);
        quizDAO = mock(QuizDAO.class);
        attendanceDAO = mock(AttendanceDAO.class);
        homeworkDAO = mock(HomeworkDAO.class);
        userDAO = mock(UserDAO.class);
        eventBusService = mock(EventBusService.class);

        lessonService = new LessonServiceImpl(
                lessonDAO, lessonTopicDAO, quizDAO, 
                attendanceDAO, homeworkDAO, userDAO
        );
        
        // Mock EventBusService singleton
        EventBusService.getInstance();
    }

    // ========== Test 3.1: Create Lesson - Valid Data with Topics ==========
    @Test
    @DisplayName("Test 3.1: Create Lesson - Valid Data with Topics")
    void testCreateLesson_ValidDataWithTopics() {
        // Arrange
        LocalDate lessonDate = LocalDate.of(2025, 1, 24);
        String monthGroup = "Month 2";
        List<LessonTopic> topics = Arrays.asList(
                new LessonTopic(null, TopicCategory.NAHW, "اسم الفاعل"),
                new LessonTopic(null, TopicCategory.ADAB, "الشعر الجاهلي")
        );
        Integer createdBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Mock DAO responses
        when(lessonDAO.insert(any(Lesson.class))).thenReturn(50);
        when(lessonTopicDAO.insert(any(LessonTopic.class)))
                .thenReturn(101)
                .thenReturn(102);

        // Act
        Integer lessonId = lessonService.createLesson(lessonDate, monthGroup, topics, createdBy);

        // Assert
        assertEquals(50, lessonId);
        
        // Verify lesson insert
        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonDAO, times(1)).insert(lessonCaptor.capture());
        Lesson capturedLesson = lessonCaptor.getValue();
        assertEquals(lessonDate, capturedLesson.getLessonDate());
        assertEquals(monthGroup, capturedLesson.getMonthGroup());
        assertEquals(createdBy, capturedLesson.getCreatedBy());

        // Verify topics insertion
        verify(lessonTopicDAO, times(2)).insert(any(LessonTopic.class));
        assertEquals(50, topics.get(0).getLessonId());
        assertEquals(50, topics.get(1).getLessonId());

        // Verify event published
        verify(userDAO, times(1)).findById(1);
    }

    // ========== Test 3.2: Create Lesson - No Topics ==========
    @Test
    @DisplayName("Test 3.2: Create Lesson - No Topics")
    void testCreateLesson_NoTopics() {
        // Arrange
        LocalDate lessonDate = LocalDate.of(2025, 1, 24);
        String monthGroup = "Month 2";
        List<LessonTopic> emptyTopics = new ArrayList<>();
        Integer createdBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Act & Assert
        InvalidTopicsException exception = assertThrows(
                InvalidTopicsException.class,
                () -> lessonService.createLesson(lessonDate, monthGroup, emptyTopics, createdBy)
        );

        assertEquals("At least one topic is required", exception.getMessage());
        verify(lessonDAO, never()).insert(any(Lesson.class));
        verify(lessonTopicDAO, never()).insert(any(LessonTopic.class));
    }

    // ========== Test 3.3: Create Lesson - Future Date ==========
    @Test
    @DisplayName("Test 3.3: Create Lesson - Future Date")
    void testCreateLesson_FutureDate() {
        // Arrange
        LocalDate futureDate = LocalDate.of(2027, 12, 31);
        String monthGroup = "Month 10";
        List<LessonTopic> topics = Arrays.asList(
                new LessonTopic(null, TopicCategory.NAHW, "اسم الفاعل")
        );
        Integer createdBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Act & Assert
        InvalidLessonDateException exception = assertThrows(
                InvalidLessonDateException.class,
                () -> lessonService.createLesson(futureDate, monthGroup, topics, createdBy)
        );

        assertEquals("Lesson date cannot be in the future", exception.getMessage());
        verify(lessonDAO, never()).insert(any(Lesson.class));
    }

    // ========== Test 3.4: Create Lesson - Non-Admin User ==========
    @Test
    @DisplayName("Test 3.4: Create Lesson - Non-Admin User")
    void testCreateLesson_NonAdminUser() {
        // Arrange
        LocalDate lessonDate = LocalDate.of(2025, 1, 24);
        String monthGroup = "Month 2";
        List<LessonTopic> topics = Arrays.asList(
                new LessonTopic(null, TopicCategory.NAHW, "اسم الفاعل")
        );
        Integer createdBy = 5;

        // Mock assistant user (non-admin)
        User assistantUser = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistantUser.setUserId(5);
        when(userDAO.findById(5)).thenReturn(assistantUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> lessonService.createLesson(lessonDate, monthGroup, topics, createdBy)
        );

        assertEquals("Only administrators can perform this action", exception.getMessage());
        verify(lessonDAO, never()).insert(any(Lesson.class));
    }

    // ========== Test 3.5: Update Lesson - Success ==========
    @Test
    @DisplayName("Test 3.5: Update Lesson - Success")
    void testUpdateLesson_Success() {
        // Arrange
        Integer lessonId = 50;
        LocalDate newDate = LocalDate.of(2025, 1, 25);
        String monthGroup = "Month 2";
        List<LessonTopic> newTopics = Arrays.asList(
                new LessonTopic(null, TopicCategory.NAHW, "اسم المفعول"),
                new LessonTopic(null, TopicCategory.ADAB, "الشعر العباسي"),
                new LessonTopic(null, TopicCategory.QISSA, "قصة جديدة")
        );
        Integer updatedBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Mock existing lesson
        Lesson existingLesson = new Lesson(LocalDate.of(2025, 1, 24), "Month 2", 1);
        existingLesson.setLessonId(50);
        when(lessonDAO.findById(50)).thenReturn(existingLesson);
        when(lessonDAO.update(any(Lesson.class))).thenReturn(true);
        when(lessonTopicDAO.deleteByLessonId(50)).thenReturn(true);
        when(lessonTopicDAO.insert(any(LessonTopic.class)))
                .thenReturn(201)
                .thenReturn(202)
                .thenReturn(203);

        // Act
        boolean result = lessonService.updateLesson(lessonId, newDate, monthGroup, newTopics, updatedBy);

        // Assert
        assertTrue(result);
        verify(lessonDAO, times(1)).update(any(Lesson.class));
        verify(lessonTopicDAO, times(1)).deleteByLessonId(50);
        verify(lessonTopicDAO, times(3)).insert(any(LessonTopic.class));
        
        // Verify topics have lessonId set
        newTopics.forEach(topic -> assertEquals(50, topic.getLessonId()));
    }

    // ========== Test 3.6: Delete Lesson - Success ==========
    @Test
    @DisplayName("Test 3.6: Delete Lesson - Success")
    void testDeleteLesson_Success() {
        // Arrange
        Integer lessonId = 50;
        Integer deletedBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        when(userDAO.findById(1)).thenReturn(adminUser);
        when(lessonDAO.delete(50)).thenReturn(true);

        // Act
        boolean result = lessonService.deleteLesson(lessonId, deletedBy);

        // Assert
        assertTrue(result);
        verify(lessonDAO, times(1)).delete(50);
    }

    // ========== Test 3.7: Get Lesson Detail - Complete Data ==========
    @Test
    @DisplayName("Test 3.7: Get Lesson Detail - Complete Data")
    void testGetLessonDetail_CompleteData() {
        // Arrange
        Integer lessonId = 50;

        // Mock lesson
        Lesson lesson = new Lesson(LocalDate.of(2025, 1, 24), "Month 2", 1);
        lesson.setLessonId(50);
        when(lessonDAO.findById(50)).thenReturn(lesson);

        // Mock topics
        List<LessonTopic> topics = Arrays.asList(
                new LessonTopic(50, TopicCategory.NAHW, "اسم الفاعل"),
                new LessonTopic(50, TopicCategory.ADAB, "الشعر الجاهلي")
        );
        when(lessonTopicDAO.findByLessonId(50)).thenReturn(topics);

        // Mock quiz
        Quiz quiz = new Quiz(50, new byte[100], 20.0, 1);
        quiz.setQuizId(10);
        when(quizDAO.findByLessonId(50)).thenReturn(quiz);

        // Mock attendance (152 students: 145 present, 7 absent)
        List<Attendance> attendanceList = new ArrayList<>();
        for (int i = 1; i <= 145; i++) {
            attendanceList.add(new Attendance(50, i, AttendanceStatus.PRESENT, 1));
        }
        for (int i = 146; i <= 152; i++) {
            attendanceList.add(new Attendance(50, i, AttendanceStatus.ABSENT, 1));
        }
        when(attendanceDAO.findByLessonId(50)).thenReturn(attendanceList);

        // Mock homework (145 students: 110 done, 25 partial, 10 not done)
        Map<HomeworkStatus, Integer> homeworkStats = new HashMap<>();
        homeworkStats.put(HomeworkStatus.DONE, 110);
        homeworkStats.put(HomeworkStatus.PARTIALLY_DONE, 25);
        homeworkStats.put(HomeworkStatus.NOT_DONE, 10);
        when(homeworkDAO.getHomeworkStatsByLesson(50)).thenReturn(homeworkStats);

        // Act
        LessonDetail lessonDetail = lessonService.getLessonDetail(lessonId);

        // Assert
        assertNotNull(lessonDetail);
        assertEquals(lesson, lessonDetail.getLesson());
        assertEquals(2, lessonDetail.getTopics().size());
        assertNotNull(lessonDetail.getQuiz());

        // Verify attendance stats
        AttendanceSummary attendanceStats = lessonDetail.getAttendanceStats();
        assertEquals(152, attendanceStats.getTotalStudents());
        assertEquals(145, attendanceStats.getPresentCount());
        assertEquals(7, attendanceStats.getAbsentCount());
        assertEquals(95.39, attendanceStats.getAttendancePercentage(), 0.01);

        // Verify homework stats
        HomeworkSummary homeworkStats2 = lessonDetail.getHomeworkStats();
        assertEquals(145, homeworkStats2.getTotalStudents());
        assertEquals(110, homeworkStats2.getDoneCount());
        assertEquals(25, homeworkStats2.getPartiallyDoneCount());
        assertEquals(10, homeworkStats2.getNotDoneCount());

        verify(lessonDAO, times(1)).findById(50);
        verify(lessonTopicDAO, times(1)).findByLessonId(50);
        verify(quizDAO, times(1)).findByLessonId(50);
        verify(attendanceDAO, times(1)).findByLessonId(50);
        verify(homeworkDAO, times(1)).getHomeworkStatsByLesson(50);
    }

    // ========== Test 3.8: Get Lesson Detail - No Quiz ==========
    @Test
    @DisplayName("Test 3.8: Get Lesson Detail - No Quiz")
    void testGetLessonDetail_NoQuiz() {
        // Arrange
        Integer lessonId = 45;

        // Mock lesson
        Lesson lesson = new Lesson(LocalDate.of(2025, 1, 20), "Month 2", 1);
        lesson.setLessonId(45);
        when(lessonDAO.findById(45)).thenReturn(lesson);

        // Mock topics
        List<LessonTopic> topics = Arrays.asList(
                new LessonTopic(45, TopicCategory.NAHW, "اسم الفاعل")
        );
        when(lessonTopicDAO.findByLessonId(45)).thenReturn(topics);

        // No quiz
        when(quizDAO.findByLessonId(45)).thenReturn(null);

        // Mock attendance
        List<Attendance> attendanceList = Arrays.asList(
                new Attendance(45, 1, AttendanceStatus.PRESENT, 1),
                new Attendance(45, 2, AttendanceStatus.PRESENT, 1)
        );
        when(attendanceDAO.findByLessonId(45)).thenReturn(attendanceList);

        // Mock homework
        Map<HomeworkStatus, Integer> homeworkStats = new HashMap<>();
        homeworkStats.put(HomeworkStatus.DONE, 2);
        when(homeworkDAO.getHomeworkStatsByLesson(45)).thenReturn(homeworkStats);

        // Act
        LessonDetail lessonDetail = lessonService.getLessonDetail(lessonId);

        // Assert
        assertNotNull(lessonDetail);
        assertEquals(lesson, lessonDetail.getLesson());
        assertNull(lessonDetail.getQuiz());
        assertNotNull(lessonDetail.getAttendanceStats());
        assertNotNull(lessonDetail.getHomeworkStats());

        verify(quizDAO, times(1)).findByLessonId(45);
    }

    // ========== Test 3.9: Get Lessons By Date Range ==========
    @Test
    @DisplayName("Test 3.9: Get Lessons By Date Range")
    void testGetLessonsByDateRange() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        // Mock 12 lessons in January
        List<Lesson> januaryLessons = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Lesson lesson = new Lesson(LocalDate.of(2025, 1, i * 2), "Month 2", 1);
            lesson.setLessonId(i);
            januaryLessons.add(lesson);
        }
        when(lessonDAO.findByDateRange(start, end)).thenReturn(januaryLessons);

        // Act
        List<Lesson> result = lessonService.getLessonsByDateRange(start, end);

        // Assert
        assertEquals(12, result.size());
        verify(lessonDAO, times(1)).findByDateRange(start, end);
    }

    // ========== Test 3.10: Get Total Lesson Count ==========
    @Test
    @DisplayName("Test 3.10: Get Total Lesson Count")
    void testGetTotalLessonCount() {
        // Arrange
        when(lessonDAO.countAll()).thenReturn(48);

        // Act
        int count = lessonService.getTotalLessonCount();

        // Assert
        assertEquals(48, count);
        verify(lessonDAO, times(1)).countAll();
    }
}