package com.studenttracker.service;

import com.studenttracker.dao.*;
import com.studenttracker.exception.ServiceException;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.model.*;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ReportService Tests")
public class ReportServiceTest {

    private ReportService reportService;
    
    // Mock DAOs
    private MonthlyReportDAO monthlyReportDAO;
    private StudentDAO studentDAO;
    private LessonDAO lessonDAO;
    private QuizDAO quizDAO;
    private AttendanceDAO attendanceDAO;
    private HomeworkDAO homeworkDAO;
    private WarningDAO warningDAO;
    private TargetDAO targetDAO;
    private BehavioralIncidentDAO behavioralIncidentDAO;
    private UserDAO userDAO;
    
    // Mock Services
    private Fasee7TableService fasee7TableService;
    private PerformanceAnalysisService performanceAnalysisService;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        monthlyReportDAO = mock(MonthlyReportDAO.class);
        studentDAO = mock(StudentDAO.class);
        lessonDAO = mock(LessonDAO.class);
        quizDAO = mock(QuizDAO.class);
        attendanceDAO = mock(AttendanceDAO.class);
        homeworkDAO = mock(HomeworkDAO.class);
        warningDAO = mock(WarningDAO.class);
        targetDAO = mock(TargetDAO.class);
        behavioralIncidentDAO = mock(BehavioralIncidentDAO.class);
        userDAO = mock(UserDAO.class);
        fasee7TableService = mock(Fasee7TableService.class);
        performanceAnalysisService = mock(PerformanceAnalysisService.class);
        
        // Initialize service
        reportService = new ReportServiceImpl(
            monthlyReportDAO, studentDAO, lessonDAO, quizDAO, attendanceDAO,
            homeworkDAO, warningDAO, targetDAO, behavioralIncidentDAO, userDAO,
            fasee7TableService, performanceAnalysisService
        );
    }

    @Test
    @DisplayName("Test 16.1: Generate Monthly Report - Complete")
    void testGenerateMonthlyReport_Complete() {
        // Setup
        String monthGroup = "Month 2";
        Integer generatedBy = 1;
        
        // Mock admin user
        User admin = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        admin.setUserId(1);
        when(userDAO.findById(1)).thenReturn(admin);
        
        // Mock report doesn't exist
        when(monthlyReportDAO.existsForMonth(monthGroup)).thenReturn(false);
        
        // Mock data collection dependencies
        when(lessonDAO.findByMonthGroup(monthGroup)).thenReturn(createMockLessons(12));
        when(studentDAO.findAll()).thenReturn(createMockStudents(152));
        when(studentDAO.countByStatus(any())).thenReturn(145, 7, 7);
        when(attendanceDAO.findByLessonId(anyInt())).thenReturn(Arrays.asList());
        when(quizDAO.findByLessonId(anyInt())).thenReturn(null);
        when(warningDAO.countActive()).thenReturn(5);
        when(fasee7TableService.getTopN(10)).thenReturn(Arrays.asList());
        when(homeworkDAO.getHomeworkStatsByLesson(anyInt())).thenReturn(java.util.Collections.emptyMap());
        
        // Mock successful insert
        when(monthlyReportDAO.insert(any(MonthlyReport.class))).thenReturn(100);
        
        // Execute
        Integer reportId = reportService.generateMonthlyReport(monthGroup, generatedBy);
        
        // Verify
        assertNotNull(reportId);
        assertEquals(100, reportId);
        
        // Verify admin validation
        verify(userDAO).findById(1);
        
        // Verify duplicate check
        verify(monthlyReportDAO).existsForMonth(monthGroup);
        
        // Verify data collection
        verify(lessonDAO).findByMonthGroup(monthGroup);
        verify(studentDAO).findAll();
        verify(studentDAO, times(3)).countByStatus(any());
        verify(warningDAO).countActive();
        verify(fasee7TableService).getTopN(10);
        
        // Verify report insertion with JSON data
        ArgumentCaptor<MonthlyReport> reportCaptor = ArgumentCaptor.forClass(MonthlyReport.class);
        verify(monthlyReportDAO).insert(reportCaptor.capture());
        
        MonthlyReport capturedReport = reportCaptor.getValue();
        assertEquals(monthGroup, capturedReport.getReportMonth());
        assertEquals(generatedBy, capturedReport.getGeneratedBy());
        assertNotNull(capturedReport.getReportData());
        assertTrue(capturedReport.getReportData().contains("Month 2"));
        assertNotNull(capturedReport.getGeneratedAt());
    }

    @Test
    @DisplayName("Test 16.2: Generate Monthly Report - Non-Admin")
    void testGenerateMonthlyReport_NonAdmin() {
        // Setup
        String monthGroup = "Month 2";
        Integer generatedBy = 5;
        
        // Mock assistant user (non-admin)
        User assistant = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistant.setUserId(5);
        when(userDAO.findById(5)).thenReturn(assistant);
        
        // Execute & Verify
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> reportService.generateMonthlyReport(monthGroup, generatedBy)
        );
        
        assertEquals("Only administrators can perform this action", exception.getMessage());
        
        // Verify admin check was performed
        verify(userDAO).findById(5);
        
        // Verify no further operations
        verify(monthlyReportDAO, never()).existsForMonth(anyString());
        verify(monthlyReportDAO, never()).insert(any());
    }

    @Test
    @DisplayName("Test 16.3: Generate Monthly Report - Duplicate")
    void testGenerateMonthlyReport_Duplicate() {
        // Setup
        String monthGroup = "Month 2";
        Integer generatedBy = 1;
        
        // Mock admin user
        User admin = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        admin.setUserId(1);
        when(userDAO.findById(1)).thenReturn(admin);
        
        // Mock report already exists
        when(monthlyReportDAO.existsForMonth(monthGroup)).thenReturn(true);
        
        // Execute & Verify
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> reportService.generateMonthlyReport(monthGroup, generatedBy)
        );
        
        assertEquals("Report already exists for month: Month 2", exception.getMessage());
        
        // Verify checks were performed
        verify(userDAO).findById(1);
        verify(monthlyReportDAO).existsForMonth(monthGroup);
        
        // Verify no insertion attempted
        verify(monthlyReportDAO, never()).insert(any());
    }

    @Test
    @DisplayName("Test 16.4: Get Report by Month")
    void testGetReportByMonth() {
        // Setup
        String monthGroup = "Month 2";
        MonthlyReport expectedReport = new MonthlyReport(
            monthGroup, 
            "{\"data\":\"test\"}", 
            LocalDateTime.now(), 
            1
        );
        expectedReport.setReportId(5);
        
        when(monthlyReportDAO.findByMonth(monthGroup)).thenReturn(expectedReport);
        
        // Execute
        MonthlyReport result = reportService.getReportByMonth(monthGroup);
        
        // Verify
        assertNotNull(result);
        assertEquals(5, result.getReportId());
        assertEquals(monthGroup, result.getReportMonth());
        verify(monthlyReportDAO).findByMonth(monthGroup);
    }

    @Test
    @DisplayName("Test 16.5: Get All Reports")
    void testGetAllReports() {
        // Setup
        List<MonthlyReport> expectedReports = Arrays.asList(
            createReport(3, "Month 3"),
            createReport(2, "Month 2"),
            createReport(1, "Month 1")
        );
        
        when(monthlyReportDAO.findAllOrderedByMonth()).thenReturn(expectedReports);
        
        // Execute
        List<MonthlyReport> results = reportService.getAllReports();
        
        // Verify
        assertNotNull(results);
        assertEquals(3, results.size());
        assertEquals("Month 3", results.get(0).getReportMonth());
        assertEquals("Month 2", results.get(1).getReportMonth());
        assertEquals("Month 1", results.get(2).getReportMonth());
        verify(monthlyReportDAO).findAllOrderedByMonth();
    }

    @Test
    @DisplayName("Test 16.6: Report Exists - True")
    void testReportExists_True() {
        // Setup
        String monthGroup = "Month 2";
        when(monthlyReportDAO.existsForMonth(monthGroup)).thenReturn(true);
        
        // Execute
        boolean exists = reportService.reportExists(monthGroup);
        
        // Verify
        assertTrue(exists);
        verify(monthlyReportDAO).existsForMonth(monthGroup);
    }

    @Test
    @DisplayName("Test 16.7: Report Exists - False")
    void testReportExists_False() {
        // Setup
        String monthGroup = "Month 10";
        when(monthlyReportDAO.existsForMonth(monthGroup)).thenReturn(false);
        
        // Execute
        boolean exists = reportService.reportExists(monthGroup);
        
        // Verify
        assertFalse(exists);
        verify(monthlyReportDAO).existsForMonth(monthGroup);
    }

    @Test
    @DisplayName("Test 16.8: Delete Report - Admin")
    void testDeleteReport_Admin() {
        // Setup
        Integer reportId = 5;
        Integer deletedBy = 1;
        
        // Mock admin user
        User admin = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        admin.setUserId(1);
        when(userDAO.findById(1)).thenReturn(admin);
        
        // Mock report exists
        MonthlyReport report = createReport(5, "Month 2");
        when(monthlyReportDAO.findById(reportId)).thenReturn(report);
        
        // Mock successful deletion
        when(monthlyReportDAO.delete(reportId)).thenReturn(true);
        
        // Execute
        boolean deleted = reportService.deleteReport(reportId, deletedBy);
        
        // Verify
        assertTrue(deleted);
        verify(userDAO).findById(1);
        verify(monthlyReportDAO).findById(reportId);
        verify(monthlyReportDAO).delete(reportId);
    }

    @Test
    @DisplayName("Test 16.9: Delete Report - Non-Admin")
    void testDeleteReport_NonAdmin() {
        // Setup
        Integer reportId = 5;
        Integer deletedBy = 5;
        
        // Mock assistant user (non-admin)
        User assistant = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistant.setUserId(5);
        when(userDAO.findById(5)).thenReturn(assistant);
        
        // Execute & Verify
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> reportService.deleteReport(reportId, deletedBy)
        );
        
        assertEquals("Only administrators can perform this action", exception.getMessage());
        
        // Verify admin check was performed
        verify(userDAO).findById(5);
        
        // Verify no deletion attempted
        verify(monthlyReportDAO, never()).delete(anyInt());
    }

    @Test
    @DisplayName("Test 16.10: Export Report as JSON")
    void testExportReportAsJson() {
        // Setup
        Integer reportId = 5;
        String expectedJson = "{\"monthGroup\":\"Month 2\",\"overview\":{\"totalLessons\":12}}";
        
        MonthlyReport report = new MonthlyReport(
            "Month 2",
            expectedJson,
            LocalDateTime.now(),
            1
        );
        report.setReportId(reportId);
        
        when(monthlyReportDAO.findById(reportId)).thenReturn(report);
        
        // Execute
        String resultJson = reportService.exportReportAsJson(reportId);
        
        // Verify
        assertNotNull(resultJson);
        assertEquals(expectedJson, resultJson);
        verify(monthlyReportDAO).findById(reportId);
    }

    @Test
    @DisplayName("Test 16.10b: Export Report as JSON - Report Not Found")
    void testExportReportAsJson_NotFound() {
        // Setup
        Integer reportId = 999;
        when(monthlyReportDAO.findById(reportId)).thenReturn(null);
        
        // Execute & Verify
        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> reportService.exportReportAsJson(reportId)
        );
        
        assertEquals("Report not found with ID: 999", exception.getMessage());
        verify(monthlyReportDAO).findById(reportId);
    }

    // ========== Helper Methods ==========

    private List<Lesson> createMockLessons(int count) {
        List<Lesson> lessons = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Lesson lesson = new Lesson();
            lesson.setLessonId(i);
            lesson.setMonthGroup("Month 2");
            lesson.setLessonDate(java.time.LocalDate.now().minusDays(count - i));
            lessons.add(lesson);
        }
        return lessons;
    }

    private List<Student> createMockStudents(int count) {
        List<Student> students = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Student student = new Student();
            student.setStudentId(i);
            student.setFullName("Student " + i);
            student.setStatus(i <= 145 ? Student.StudentStatus.ACTIVE : Student.StudentStatus.ARCHIVED);
            students.add(student);
        }
        return students;
    }

    private MonthlyReport createReport(int id, String monthGroup) {
        MonthlyReport report = new MonthlyReport(
            monthGroup,
            "{\"data\":\"test\"}",
            LocalDateTime.now(),
            1
        );
        report.setReportId(id);
        return report;
    }
}