package com.studenttracker.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.studenttracker.dao.*;
import com.studenttracker.exception.ServiceException;
import com.studenttracker.model.*;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.BehavioralIncident.IncidentType;
import com.studenttracker.model.Homework.HomeworkStatus;
import static com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.service.*;
import com.studenttracker.service.event.MonthlyReportDeletedEvent;
import com.studenttracker.service.event.MonthlyReportGeneratedEvent;
import com.studenttracker.service.validator.AdminPermissionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ReportServiceImpl implements ReportService {
    
    private final MonthlyReportDAO monthlyReportDAO;
    private final StudentDAO studentDAO;
    private final LessonDAO lessonDAO;
    private final QuizDAO quizDAO;
    private final AttendanceDAO attendanceDAO;
    private final HomeworkDAO homeworkDAO;
    private final WarningDAO warningDAO;
    private final TargetDAO targetDAO;
    private final BehavioralIncidentDAO behavioralIncidentDAO;
    private final UserDAO userDAO;
    private final Fasee7TableService fasee7TableService;
    private final PerformanceAnalysisService performanceAnalysisService;
    private final EventBusService eventBus;
    private final Gson gson;

    public ReportServiceImpl(MonthlyReportDAO monthlyReportDAO, StudentDAO studentDAO,
                            LessonDAO lessonDAO, QuizDAO quizDAO, AttendanceDAO attendanceDAO,
                            HomeworkDAO homeworkDAO, WarningDAO warningDAO, TargetDAO targetDAO,
                            BehavioralIncidentDAO behavioralIncidentDAO, UserDAO userDAO,
                            Fasee7TableService fasee7TableService,
                            PerformanceAnalysisService performanceAnalysisService) {
        this.monthlyReportDAO = monthlyReportDAO;
        this.studentDAO = studentDAO;
        this.lessonDAO = lessonDAO;
        this.quizDAO = quizDAO;
        this.attendanceDAO = attendanceDAO;
        this.homeworkDAO = homeworkDAO;
        this.warningDAO = warningDAO;
        this.targetDAO = targetDAO;
        this.behavioralIncidentDAO = behavioralIncidentDAO;
        this.userDAO = userDAO;
        this.fasee7TableService = fasee7TableService;
        this.performanceAnalysisService = performanceAnalysisService;
        this.eventBus = EventBusService.getInstance();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Integer generateMonthlyReport(String monthGroup, Integer generatedBy) {
        // Validate admin permission
        AdminPermissionValidator.validateAdminPermission(generatedBy, userDAO);
        
        // Check if report already exists
        if (monthlyReportDAO.existsForMonth(monthGroup)) {
            throw new ServiceException("Report already exists for month: " + monthGroup);
        }
        
        try {
            // Collect all report data
            ReportData reportData = collectReportData(monthGroup);
            
            // Serialize to JSON
            String jsonData = gson.toJson(reportData);
            
            // Create and save report
            MonthlyReport report = new MonthlyReport(
                monthGroup,
                jsonData,
                LocalDateTime.now(),
                generatedBy
            );
            
            Integer reportId = monthlyReportDAO.insert(report);
            
            if (reportId == null) {
                throw new ServiceException("Failed to save monthly report");
            }
            
            // Publish event
            eventBus.publish(new MonthlyReportGeneratedEvent(reportId, monthGroup, generatedBy));
            
            return reportId;
            
        } catch (Exception e) {
            throw new ServiceException("Error generating monthly report: " + e.getMessage(), e);
        }
    }

    @Override
    public MonthlyReport getReportById(Integer reportId) {
        return monthlyReportDAO.findById(reportId);
    }

    @Override
    public MonthlyReport getReportByMonth(String monthGroup) {
        return monthlyReportDAO.findByMonth(monthGroup);
    }

    @Override
    public List<MonthlyReport> getAllReports() {
        return monthlyReportDAO.findAllOrderedByMonth();
    }

    @Override
    public MonthlyReport getLatestReport() {
        return monthlyReportDAO.findLatest();
    }

    @Override
    public String exportReportAsJson(Integer reportId) {
        MonthlyReport report = monthlyReportDAO.findById(reportId);
        if (report == null) {
            throw new ServiceException("Report not found with ID: " + reportId);
        }
        return report.getReportData();
    }

    @Override
    public boolean reportExists(String monthGroup) {
        return monthlyReportDAO.existsForMonth(monthGroup);
    }

    @Override
    public boolean deleteReport(Integer reportId, Integer deletedBy) {
        // Validate admin permission
        AdminPermissionValidator.validateAdminPermission(deletedBy, userDAO);
        
        // Get report before deletion for event
        MonthlyReport report = monthlyReportDAO.findById(reportId);
        if (report == null) {
            return false;
        }
        
        String monthGroup = report.getReportMonth();
        
        // Delete report
        boolean deleted = monthlyReportDAO.delete(reportId);
        
        if (deleted) {
            // Publish event
            eventBus.publish(new MonthlyReportDeletedEvent(reportId, monthGroup, deletedBy));
        }
        
        return deleted;
    }

    // ========== Private Helper Methods ==========

    private ReportData collectReportData(String monthGroup) {
        // Get all lessons in month
        List<Lesson> lessons = lessonDAO.findByMonthGroup(monthGroup);
        List<Integer> lessonIds = lessons.stream()
            .map(Lesson::getLessonId)
            .collect(Collectors.toList());
        
        // Get all students
        List<Student> allStudents = studentDAO.findAll();
        List<Integer> studentIds = allStudents.stream()
            .map(Student::getStudentId)
            .collect(Collectors.toList());
        
        // Collect each section
        OverviewData overview = getOverviewData(lessons, lessonIds);
        Fasee7SnapshotData fasee7 = getFasee7Data(monthGroup);
        PerformanceSummary performance = getPerformanceSummary(studentIds);
        TargetSummary targets = getTargetSummary(studentIds, monthGroup);
        AttendanceBehavioralSummary attendanceBehavioral = getAttendanceBehavioralData(lessonIds, monthGroup);
        HomeworkSummary homework = getHomeworkData(lessonIds);
        
        return new ReportData(monthGroup, overview, fasee7, performance, targets, attendanceBehavioral, homework);
    }

    private OverviewData getOverviewData(List<Lesson> lessons, List<Integer> lessonIds) {
        int totalLessons = lessons.size();
        int activeStudents = studentDAO.countByStatus(StudentStatus.ACTIVE);
        int archivedStudents = studentDAO.countByStatus(StudentStatus.ARCHIVED);
        
        // Calculate overall attendance rate
        double attendanceRate = calculateOverallAttendanceRate(lessonIds);
        
        // Count quizzes
        int quizzesCount = countQuizzesInLessons(lessonIds);
        
        // Average quiz score (across all students and quizzes)
        double avgQuizScore = calculateAverageQuizScore(lessonIds);
        
        // Warnings generated (count active warnings)
        int warningsGenerated = warningDAO.countActive();
        
        // Students archived this month (would need lesson date range)
        int studentsArchivedThisMonth = countArchivedInMonth(lessons);
        
        return new OverviewData(totalLessons, activeStudents, archivedStudents,
            attendanceRate, quizzesCount, avgQuizScore, warningsGenerated, studentsArchivedThisMonth);
    }

    private double calculateOverallAttendanceRate(List<Integer> lessonIds) {
        if (lessonIds.isEmpty()) return 0.0;
        
        int totalAttendances = 0;
        int presentCount = 0;
        
        for (Integer lessonId : lessonIds) {
            List<Attendance> attendances = attendanceDAO.findByLessonId(lessonId);
            totalAttendances += attendances.size();
            presentCount += (int) attendances.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        }
        
        return totalAttendances > 0 ? (presentCount * 100.0 / totalAttendances) : 0.0;
    }

    private int countQuizzesInLessons(List<Integer> lessonIds) {
        int count = 0;
        for (Integer lessonId : lessonIds) {
            if (quizDAO.findByLessonId(lessonId) != null) {
                count++;
            }
        }
        return count;
    }

    private double calculateAverageQuizScore(List<Integer> lessonIds) {
        // This is a simplified version - would need QuizScoreDAO for accurate calculation
        // For now, return 0.0 as placeholder
        return 0.0;
    }

    private int countArchivedInMonth(List<Lesson> lessons) {
        if (lessons.isEmpty()) return 0;
        
        LocalDate startDate = lessons.stream()
            .map(Lesson::getLessonDate)
            .min(LocalDate::compareTo)
            .orElse(LocalDate.now());
        
        LocalDate endDate = lessons.stream()
            .map(Lesson::getLessonDate)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());
        
        List<Student> archivedStudents = studentDAO.findByStatus(StudentStatus.ARCHIVED);
        return (int) archivedStudents.stream()
            .filter(s -> s.getArchivedAt() != null)
            .filter(s -> {
                LocalDate archivedDate = s.getArchivedAt().toLocalDate();
                return !archivedDate.isBefore(startDate) && !archivedDate.isAfter(endDate);
            })
            .count();
    }

    private Fasee7SnapshotData getFasee7Data(String monthGroup) {
        // Get current top 10
        List<Fasee7Points> currentRankings = fasee7TableService.getTopN(10);
        List<RankingEntry> currentTop10 = convertToRankingEntries(currentRankings);
        
        // Get previous month snapshot (simplified - would need month calculation)
        List<RankingEntry> previousTop10 = new ArrayList<>();
        
        // Calculate rank changes
        Map<Integer, Integer> rankChanges = new HashMap<>();
        
        return new Fasee7SnapshotData(currentTop10, previousTop10, rankChanges);
    }

    private List<RankingEntry> convertToRankingEntries(List<Fasee7Points> pointsList) {
        List<RankingEntry> entries = new ArrayList<>();
        int rank = 1;
        
        for (Fasee7Points points : pointsList) {
            Student student = studentDAO.findById(points.getStudentId());
            String studentName = student != null ? student.getFullName() : "Unknown";
            
            entries.add(new RankingEntry(
                points.getStudentId(),
                studentName,
                rank++,
                points.getTotalPoints(),
                points.getQuizPoints(),
                points.getAttendancePoints(),
                points.getHomeworkPoints(),
                points.getTargetPoints()
            ));
        }
        
        return entries;
    }

    private PerformanceSummary getPerformanceSummary(List<Integer> studentIds) {
        Map<TopicCategory, Double> avgByCategory = new HashMap<>();
        
        for (TopicCategory category : TopicCategory.values()) {
            double sum = 0;
            int count = 0;
            
            for (Integer studentId : studentIds) {
                int pi = performanceAnalysisService.getCurrentCumulativePI(studentId, category);
                if (pi > 0) {
                    sum += pi;
                    count++;
                }
            }
            
            avgByCategory.put(category, count > 0 ? sum / count : 0.0);
        }
        
        // Find strongest and weakest
        TopicCategory strongest = avgByCategory.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        TopicCategory weakest = avgByCategory.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        return new PerformanceSummary(avgByCategory, strongest, weakest);
    }

    private TargetSummary getTargetSummary(List<Integer> studentIds, String monthGroup) {
        int activeCount = 0;
        int achievedCount = 0;
        Map<Integer, Integer> studentTargetPoints = new HashMap<>();
        
        for (Integer studentId : studentIds) {
            List<Target> activeTargets = targetDAO.findActiveByStudent(studentId);
            activeCount += activeTargets.size();
            
            List<Target> achievedTargets = targetDAO.findAchievedByStudent(studentId);
            
            // Count achieved in this month (simplified)
            int monthAchieved = (int) achievedTargets.stream()
                .filter(t -> t.getAchievedAt() != null)
                .count();
            achievedCount += monthAchieved;
            
            // Calculate target points
            Fasee7Points points = fasee7TableService.getStudentPoints(studentId);
            if (points != null) {
                studentTargetPoints.put(studentId, points.getTargetPoints());
            }
        }
        
        // Get top 5 achievers
        List<TopAchiever> topAchievers = studentTargetPoints.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Student student = studentDAO.findById(entry.getKey());
                String name = student != null ? student.getFullName() : "Unknown";
                return new TopAchiever(entry.getKey(), name, entry.getValue());
            })
            .collect(Collectors.toList());
        
        return new TargetSummary(activeCount, achievedCount, topAchievers);
    }

    private AttendanceBehavioralSummary getAttendanceBehavioralData(List<Integer> lessonIds, String monthGroup) {
        List<Student> activeStudents = studentDAO.findByStatus(StudentStatus.ACTIVE);
        
        int perfectAttendance = 0;
        int oneToTwo = 0;
        int threePlus = 0;
        
        for (Student student : activeStudents) {
            int absences = attendanceDAO.countByStudentAndStatus(student.getStudentId(), AttendanceStatus.ABSENT);
            
            if (absences == 0) {
                perfectAttendance++;
            } else if (absences <= 2) {
                oneToTwo++;
            } else {
                threePlus++;
            }
        }
        
        int archivedCount = studentDAO.countByStatus(StudentStatus.ARCHIVED);
        
        // Behavioral incidents by type
        Map<String, Integer> incidentsByType = new HashMap<>();
        for (IncidentType type : IncidentType.values()) {
            incidentsByType.put(type.name(), behavioralIncidentDAO.findByType(type).size());
        }
        
        return new AttendanceBehavioralSummary(perfectAttendance, oneToTwo, threePlus, 
            archivedCount, incidentsByType);
    }

    private HomeworkSummary getHomeworkData(List<Integer> lessonIds) {
        int totalStudents = 0;
        int doneCount = 0;
        int partialCount = 0;
        int notDoneCount = 0;
        
        for (Integer lessonId : lessonIds) {
            Map<HomeworkStatus, Integer> stats = homeworkDAO.getHomeworkStatsByLesson(lessonId);
            
            doneCount += stats.getOrDefault(HomeworkStatus.DONE, 0);
            partialCount += stats.getOrDefault(HomeworkStatus.PARTIALLY_DONE, 0);
            notDoneCount += stats.getOrDefault(HomeworkStatus.NOT_DONE, 0);
            
            totalStudents += stats.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        return new HomeworkSummary(totalStudents, doneCount, partialCount, notDoneCount);
    }
}