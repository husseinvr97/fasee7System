package com.studenttracker.service;

import com.studenttracker.dao.*;
import com.studenttracker.model.*;
import com.studenttracker.service.event.Fasee7PointsUpdatedEvent;
import com.studenttracker.service.impl.Fasee7TableServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Fasee7TableService.
 * Tests all point calculations, rankings, snapshots, and comparisons.
 */
public class Fasee7TableServiceTest {
    
    private Fasee7TableService service;
    
    // Mocked dependencies
    private Fasee7PointsDAO pointsDAO;
    private Fasee7SnapshotDAO snapshotDAO;
    private QuizScoreDAO quizScoreDAO;
    private AttendanceDAO attendanceDAO;
    private HomeworkDAO homeworkDAO;
    private TargetAchievementStreakDAO streakDAO;
    private StudentDAO studentDAO;
    private EventBusService eventBus;
    
    @BeforeEach
    void setUp() {
        // Initialize mocks
        pointsDAO = mock(Fasee7PointsDAO.class);
        snapshotDAO = mock(Fasee7SnapshotDAO.class);
        quizScoreDAO = mock(QuizScoreDAO.class);
        attendanceDAO = mock(AttendanceDAO.class);
        homeworkDAO = mock(HomeworkDAO.class);
        streakDAO = mock(TargetAchievementStreakDAO.class);
        studentDAO = mock(StudentDAO.class);
        eventBus = mock(EventBusService.class);
        
        // Create service instance
        service = new Fasee7TableServiceImpl(
            pointsDAO, snapshotDAO, quizScoreDAO, attendanceDAO,
            homeworkDAO, streakDAO, studentDAO, eventBus
        );
    }
    
    
    // ========== Test 14.1: Recalculate Points - All Components ==========
    
    @Test
    @DisplayName("Test 14.1: Recalculate Points - All Components")
    void testRecalculatePoints_AllComponents() {
        // Setup
        Integer studentId = 1;
        
        // Mock quiz scores: total 85 points
        List<QuizScore> quizScores = Arrays.asList(
            createQuizScore(1, studentId, 45.0),
            createQuizScore(2, studentId, 40.0)
        );
        when(quizScoreDAO.findByStudentId(studentId)).thenReturn(quizScores);
        
        // Mock attendance: 45 PRESENT
        List<Attendance> attendances = new ArrayList<>();
        for (int i = 0; i < 45; i++) {
            attendances.add(createAttendance(i + 1, studentId, Attendance.AttendanceStatus.PRESENT));
        }
        when(attendanceDAO.findByStudentId(studentId)).thenReturn(attendances);
        
        // Mock homework: 30 DONE (90), 10 PARTIALLY_DONE (10), 5 NOT_DONE (0) = 100
        List<Homework> homeworks = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            homeworks.add(createHomework(i + 1, studentId, Homework.HomeworkStatus.DONE));
        }
        for (int i = 30; i < 40; i++) {
            homeworks.add(createHomework(i + 1, studentId, Homework.HomeworkStatus.PARTIALLY_DONE));
        }
        for (int i = 40; i < 45; i++) {
            homeworks.add(createHomework(i + 1, studentId, Homework.HomeworkStatus.NOT_DONE));
        }
        when(homeworkDAO.findByStudentId(studentId)).thenReturn(homeworks);
        
        // Mock target streak: 18 points
        TargetAchievementStreak streak = new TargetAchievementStreak();
        streak.setStudentId(studentId);
        streak.setTotalPointsEarned(18);
        when(streakDAO.findByStudentId(studentId)).thenReturn(streak);
        
        // Mock existing points record (return null to create new)
        when(pointsDAO.findByStudentId(studentId)).thenReturn(null);
        
        // Execute
        service.recalculatePoints(studentId);
        
        // Verify upsert called with correct values
        ArgumentCaptor<Fasee7Points> captor = ArgumentCaptor.forClass(Fasee7Points.class);
        verify(pointsDAO).upsert(captor.capture());
        
        Fasee7Points saved = captor.getValue();
        assertEquals(85.0, saved.getQuizPoints(), 0.01);
        assertEquals(45, saved.getAttendancePoints());
        assertEquals(100, saved.getHomeworkPoints());
        assertEquals(18, saved.getTargetPoints());
        assertEquals(248.0, saved.getTotalPoints(), 0.01);
        
        // Verify event published
        ArgumentCaptor<Fasee7PointsUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(Fasee7PointsUpdatedEvent.class);
        verify(eventBus).publish(eventCaptor.capture());
        
        Fasee7PointsUpdatedEvent event = eventCaptor.getValue();
        assertEquals(studentId, event.getStudentId());
        assertEquals(248.0, event.getTotalPoints(), 0.01);
    }
    
    
    // ========== Test 14.2: Update Quiz Points - After Quiz Grading ==========
    
    @Test
    @DisplayName("Test 14.2: Update Quiz Points - After Quiz Grading")
    void testUpdateQuizPoints_AfterQuizGrading() {
        // Setup
        Integer studentId = 1;
        
        // Mock existing points
        Fasee7Points existingPoints = new Fasee7Points();
        existingPoints.setStudentId(studentId);
        existingPoints.setQuizPoints(85.0);
        existingPoints.setAttendancePoints(45);
        existingPoints.setHomeworkPoints(100);
        existingPoints.setTargetPoints(18);
        existingPoints.setTotalPoints(248.0);
        when(pointsDAO.findByStudentId(studentId)).thenReturn(existingPoints);
        
        // Mock quiz scores: previous 85 + new 15 = 100
        List<QuizScore> quizScores = Arrays.asList(
            createQuizScore(1, studentId, 45.0),
            createQuizScore(2, studentId, 40.0),
            createQuizScore(3, studentId, 15.0)
        );
        when(quizScoreDAO.findByStudentId(studentId)).thenReturn(quizScores);
        
        // Execute
        service.updateQuizPoints(studentId);
        
        // Verify update called
        ArgumentCaptor<Fasee7Points> captor = ArgumentCaptor.forClass(Fasee7Points.class);
        verify(pointsDAO).update(captor.capture());
        
        Fasee7Points updated = captor.getValue();
        assertEquals(100.0, updated.getQuizPoints(), 0.01);
        assertEquals(263.0, updated.getTotalPoints(), 0.01); // 100 + 45 + 100 + 18
        
        // Verify event published
        verify(eventBus).publish(any(Fasee7PointsUpdatedEvent.class));
    }
    
    
    // ========== Test 14.3: Get Rankings - Correct Ordering ==========
    
    @Test
    @DisplayName("Test 14.3: Get Rankings - Correct Ordering")
    void testGetRankings_CorrectOrdering() {
        // Setup: 5 students with different total points
        List<Fasee7Points> allPoints = Arrays.asList(
            createFasee7Points(5, 260.0, 130.0, 50, 60, 20),
            createFasee7Points(2, 250.0, 120.0, 45, 60, 25),
            createFasee7Points(3, 250.0, 118.0, 45, 62, 25),
            createFasee7Points(1, 248.0, 115.0, 45, 63, 25),
            createFasee7Points(4, 230.0, 100.0, 45, 60, 25)
        );
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        // Mock all students as ACTIVE
        List<Student> activeStudents = Arrays.asList(
            createStudent(1, "أحمد", LocalDateTime.of(2024, 9, 1, 10, 0)),
            createStudent(2, "محمد", LocalDateTime.of(2024, 9, 1, 10, 0)),
            createStudent(3, "علي", LocalDateTime.of(2024, 9, 1, 10, 0)),
            createStudent(4, "حسن", LocalDateTime.of(2024, 9, 1, 10, 0)),
            createStudent(5, "يوسف", LocalDateTime.of(2024, 9, 1, 10, 0))
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        // Mock studentDAO.findById for each student
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute
        List<Fasee7Points> rankings = service.getRankings();
        
        // Verify correct order: [5(260), 2(250), 3(250), 1(248), 4(230)]
        assertEquals(5, rankings.size());
        assertEquals(5, rankings.get(0).getStudentId()); // 260 points
        assertEquals(2, rankings.get(1).getStudentId()); // 250 points (quiz: 120 > 118)
        assertEquals(3, rankings.get(2).getStudentId()); // 250 points (quiz: 118)
        assertEquals(1, rankings.get(3).getStudentId()); // 248 points
        assertEquals(4, rankings.get(4).getStudentId()); // 230 points
    }
    
    
    // ========== Test 14.4: Get Rankings - Complex Tie-Breaking ==========
    
    @Test
    @DisplayName("Test 14.4: Get Rankings - Complex Tie-Breaking")
    void testGetRankings_ComplexTieBreaking() {
        // Setup: Students 2 and 3 with identical total, quiz, target, homework
        // But different attendance: Student 2 has 45, Student 3 has 44
        List<Fasee7Points> allPoints = Arrays.asList(
        createFasee7Points(2, 250.0, 120.0, 45, 60, 25),
        createFasee7Points(3, 250.0, 120.0, 44, 60, 25)  // Changed homework from 61 to 60
    );
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        List<Student> activeStudents = Arrays.asList(
            createStudent(2, "محمد", LocalDateTime.of(2024, 9, 1, 10, 0)),
            createStudent(3, "علي", LocalDateTime.of(2024, 9, 1, 10, 0))
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute
        List<Fasee7Points> rankings = service.getRankings();
        
        // Verify: Student 2 should be first (attendance: 45 > 44)
        // Verify: Student 2 should be first (attendance: 45 > 44)
assertEquals(2, rankings.get(0).getStudentId());
assertEquals(3, rankings.get(1).getStudentId());
    }
    
    
    // ========== Test 14.5: Get Rankings - Final Tie-Break by Registration Date ==========
    
    @Test
    @DisplayName("Test 14.5: Get Rankings - Final Tie-Break by Registration Date")
    void testGetRankings_TieBreakByRegistrationDate() {
        // Setup: Students with ALL points identical
        List<Fasee7Points> allPoints = Arrays.asList(
            createFasee7Points(2, 250.0, 120.0, 45, 60, 25),
            createFasee7Points(3, 250.0, 120.0, 45, 60, 25)
        );
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        // Student 2 registered earlier
        List<Student> activeStudents = Arrays.asList(
            createStudent(2, "محمد", LocalDateTime.of(2024, 9, 1, 10, 0)),
            createStudent(3, "علي", LocalDateTime.of(2024, 9, 5, 10, 0))
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute
        List<Fasee7Points> rankings = service.getRankings();
        
        // Verify: Student 2 should be first (older registration)
        assertEquals(2, rankings.get(0).getStudentId());
        assertEquals(3, rankings.get(1).getStudentId());
    }
    
    
    // ========== Test 14.6: Get Rankings - Alphabetical as Last Resort ==========
    
    @Test
    @DisplayName("Test 14.6: Get Rankings - Alphabetical as Last Resort")
    void testGetRankings_AlphabeticalLastResort() {
        // Setup: Students with identical points AND same registration date
        List<Fasee7Points> allPoints = Arrays.asList(
            createFasee7Points(2, 250.0, 120.0, 45, 60, 25),
            createFasee7Points(3, 250.0, 120.0, 45, 60, 25)
        );
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        // Same registration date, different names
        LocalDateTime sameDate = LocalDateTime.of(2024, 9, 1, 10, 0);
        List<Student> activeStudents = Arrays.asList(
            createStudent(2, "محمد", sameDate), // Mu
            createStudent(3, "أحمد", sameDate)  // Ah (comes first alphabetically)
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute
        List<Fasee7Points> rankings = service.getRankings();
        
        // Verify: Student 3 (أحمد) should be first alphabetically
        assertEquals(3, rankings.get(0).getStudentId());
        assertEquals(2, rankings.get(1).getStudentId());
    }
    
    
    // ========== Test 14.7: Get Student Rank ==========
    
    @Test
    @DisplayName("Test 14.7: Get Student Rank")
    void testGetStudentRank() {
        // Setup rankings
        List<Fasee7Points> allPoints = Arrays.asList(
            createFasee7Points(5, 260.0, 130.0, 50, 60, 20),
            createFasee7Points(2, 250.0, 120.0, 45, 60, 25),
            createFasee7Points(3, 250.0, 118.0, 45, 62, 25),
            createFasee7Points(1, 248.0, 115.0, 45, 63, 25),
            createFasee7Points(4, 230.0, 100.0, 45, 60, 25)
        );
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        List<Student> activeStudents = Arrays.asList(
            createStudent(1, "أحمد", LocalDateTime.now()),
            createStudent(2, "محمد", LocalDateTime.now()),
            createStudent(3, "علي", LocalDateTime.now()),
            createStudent(4, "حسن", LocalDateTime.now()),
            createStudent(5, "يوسف", LocalDateTime.now())
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute
        int rank = service.getStudentRank(3);
        
        // Verify: Student 3 should be at rank 3
        assertEquals(3, rank);
    }
    
    
    // ========== Test 14.8: Create Snapshot - Monthly Save ==========
    
    @Test
    @DisplayName("Test 14.8: Create Snapshot - Monthly Save")
    void testCreateSnapshot_MonthlySave() {
        // Setup
        LocalDate snapshotDate = LocalDate.of(2025, 1, 31);
        Integer createdBy = 1;
        
        // Mock current rankings
        List<Fasee7Points> rankings = Arrays.asList(
            createFasee7Points(1, 248.0, 115.0, 45, 63, 25),
            createFasee7Points(2, 250.0, 120.0, 45, 60, 25)
        );
        
        List<Fasee7Points> allPoints = new ArrayList<>(rankings);
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        List<Student> activeStudents = Arrays.asList(
            createStudent(1, "أحمد", LocalDateTime.now()),
            createStudent(2, "محمد", LocalDateTime.now())
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Mock snapshot insert
        when(snapshotDAO.insert(any(Fasee7Snapshot.class))).thenReturn(100);
        
        // Execute
        Integer snapshotId = service.createSnapshot(snapshotDate, createdBy);
        
        // Verify
        assertEquals(100, snapshotId);
        
        ArgumentCaptor<Fasee7Snapshot> captor = ArgumentCaptor.forClass(Fasee7Snapshot.class);
        verify(snapshotDAO).insert(captor.capture());
        
        Fasee7Snapshot saved = captor.getValue();
        assertEquals(snapshotDate, saved.getSnapshotDate());
        assertNotNull(saved.getSnapshotData());
        assertNotNull(saved.getCreatedAt());
    }
    
    
    // ========== Test 14.9: Compare Rankings - Rank Changes ==========
    
    @Test
    @DisplayName("Test 14.9: Compare Rankings - Rank Changes")
    void testCompareRankings_RankChanges() {
        // Setup
        LocalDate date1 = LocalDate.of(2024, 12, 31);
        LocalDate date2 = LocalDate.of(2025, 1, 31);
        
        // Mock Dec 31 snapshot: Student 1 was rank 5
        String snapshot1Data = "[{\"studentId\":1.0,\"rank\":5.0},{\"studentId\":2.0,\"rank\":1.0}]";
        Fasee7Snapshot snapshot1 = new Fasee7Snapshot();
        snapshot1.setSnapshotDate(date1);
        snapshot1.setSnapshotData(snapshot1Data);
        when(snapshotDAO.findByDate(date1)).thenReturn(snapshot1);
        
        // Mock Jan 31 snapshot: Student 1 is now rank 3
        String snapshot2Data = "[{\"studentId\":1.0,\"rank\":3.0},{\"studentId\":2.0,\"rank\":1.0}]";
        Fasee7Snapshot snapshot2 = new Fasee7Snapshot();
        snapshot2.setSnapshotDate(date2);
        snapshot2.setSnapshotData(snapshot2Data);
        when(snapshotDAO.findByDate(date2)).thenReturn(snapshot2);
        
        // Execute
        Map<Integer, Integer> comparison = service.compareRankings(date1, date2);
        
        // Verify: Student 1 improved from rank 5 to 3 = +2
        assertEquals(2, comparison.get(1)); // 5 - 3 = +2 (improved)
        assertEquals(0, comparison.get(2)); // 1 - 1 = 0 (no change)
    }
    
    
    // ========== Test 14.10: Initialize Points - New Student ==========
    
    @Test
    @DisplayName("Test 14.10: Initialize Points - New Student")
    void testInitializePoints_NewStudent() {
        // Setup
        Integer studentId = 200;
        
        // Execute
        service.initializePoints(studentId);
        
        // Verify insert called with all zeros
        ArgumentCaptor<Fasee7Points> captor = ArgumentCaptor.forClass(Fasee7Points.class);
        verify(pointsDAO).insert(captor.capture());
        
        Fasee7Points saved = captor.getValue();
        assertEquals(studentId, saved.getStudentId());
        assertEquals(0.0, saved.getQuizPoints(), 0.01);
        assertEquals(0, saved.getAttendancePoints());
        assertEquals(0, saved.getHomeworkPoints());
        assertEquals(0, saved.getTargetPoints());
        assertEquals(0.0, saved.getTotalPoints(), 0.01);
        
        // Verify event published
        verify(eventBus).publish(any(Fasee7PointsUpdatedEvent.class));
    }
    
    
    // ========== Test 14.11: Get Top N Students ==========
    
    @Test
    @DisplayName("Test 14.11: Get Top N Students")
    void testGetTopN() {
        // Setup: 15 students total
        List<Fasee7Points> allPoints = new ArrayList<>();
        List<Student> activeStudents = new ArrayList<>();
        
        for (int i = 1; i <= 15; i++) {
            allPoints.add(createFasee7Points(i, 300.0 - i, 100.0, 50, 50, 100 - i));
            activeStudents.add(createStudent(i, "Student" + i, LocalDateTime.now()));
        }
        
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute: Get top 10
        List<Fasee7Points> top10 = service.getTopN(10);
        
        // Verify
        assertEquals(10, top10.size());
        assertEquals(1, top10.get(0).getStudentId()); // Highest points
    }
    
    
    // ========== Test 14.12: Get Average Points ==========
    
    @Test
    @DisplayName("Test 14.12: Get Average Points")
    void testGetAveragePoints() {
        // Setup: 5 students with points [260, 250, 250, 248, 230]
        // Sum = 1238, Average = 247.6
        List<Fasee7Points> allPoints = Arrays.asList(
            createFasee7Points(5, 260.0, 130.0, 50, 60, 20),
            createFasee7Points(2, 250.0, 120.0, 45, 60, 25),
            createFasee7Points(3, 250.0, 118.0, 45, 62, 25),
            createFasee7Points(1, 248.0, 115.0, 45, 63, 25),
            createFasee7Points(4, 230.0, 100.0, 45, 60, 25)
        );
        when(pointsDAO.findAllOrderedByTotal()).thenReturn(allPoints);
        
        List<Student> activeStudents = Arrays.asList(
            createStudent(1, "أحمد", LocalDateTime.now()),
            createStudent(2, "محمد", LocalDateTime.now()),
            createStudent(3, "علي", LocalDateTime.now()),
            createStudent(4, "حسن", LocalDateTime.now()),
            createStudent(5, "يوسف", LocalDateTime.now())
        );
        when(studentDAO.findByStatus(Student.StudentStatus.ACTIVE)).thenReturn(activeStudents);
        
        for (Student student : activeStudents) {
            when(studentDAO.findById(student.getStudentId())).thenReturn(student);
        }
        
        // Execute
        Double average = service.getAveragePoints();
        
        // Verify
        assertEquals(247.6, average, 0.01);
    }
    
    
    // ========== Helper Methods ==========
    
    private QuizScore createQuizScore(Integer scoreId, Integer studentId, Double pointsEarned) {
        QuizScore score = new QuizScore();
        score.setScoreId(scoreId);
        score.setStudentId(studentId);
        score.setPointsEarned(pointsEarned);
        return score;
    }
    
    private Attendance createAttendance(Integer attendanceId, Integer studentId, Attendance.AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(attendanceId);
        attendance.setStudentId(studentId);
        attendance.setStatus(status);
        return attendance;
    }
    
    private Homework createHomework(Integer homeworkId, Integer studentId, Homework.HomeworkStatus status) {
        Homework homework = new Homework();
        homework.setHomeworkId(homeworkId);
        homework.setStudentId(studentId);
        homework.setStatus(status);
        return homework;
    }
    
    private Fasee7Points createFasee7Points(Integer studentId, Double total, Double quiz, 
                                            int attendance, int homework, int target) {
        Fasee7Points points = new Fasee7Points();
        points.setStudentId(studentId);
        points.setTotalPoints(total);
        points.setQuizPoints(quiz);
        points.setAttendancePoints(attendance);
        points.setHomeworkPoints(homework);
        points.setTargetPoints(target);
        return points;
    }
    
    private Student createStudent(Integer studentId, String name, LocalDateTime registrationDate) {
        Student student = new Student();
        student.setStudentId(studentId);
        student.setFullName(name);
        student.setRegistrationDate(registrationDate);
        student.setStatus(Student.StudentStatus.ACTIVE);
        return student;
    }
}
