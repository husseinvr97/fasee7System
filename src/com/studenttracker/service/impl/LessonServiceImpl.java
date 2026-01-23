package com.studenttracker.service.impl;

import com.google.common.eventbus.EventBus;
import com.studenttracker.dao.*;
import com.studenttracker.exception.*;
import com.studenttracker.model.*;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.Homework.HomeworkStatus;
import com.studenttracker.service.LessonService;
import com.studenttracker.service.event.LessonCreatedEvent;
import com.studenttracker.service.validator.AdminPermissionValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LessonServiceImpl implements LessonService {

    private final LessonDAO lessonDAO;
    private final LessonTopicDAO lessonTopicDAO;
    private final QuizDAO quizDAO;
    private final AttendanceDAO attendanceDAO;
    private final HomeworkDAO homeworkDAO;
    private final UserDAO userDAO;
    private final EventBus eventBus;

    public LessonServiceImpl(LessonDAO lessonDAO, LessonTopicDAO lessonTopicDAO, 
                            QuizDAO quizDAO, AttendanceDAO attendanceDAO, 
                            HomeworkDAO homeworkDAO, UserDAO userDAO, EventBus eventBus) {
        this.lessonDAO = lessonDAO;
        this.lessonTopicDAO = lessonTopicDAO;
        this.quizDAO = quizDAO;
        this.attendanceDAO = attendanceDAO;
        this.homeworkDAO = homeworkDAO;
        this.userDAO = userDAO;
        this.eventBus = eventBus;
    }

    @Override
    public Integer createLesson(LocalDate lessonDate, String monthGroup, 
                               List<LessonTopic> topics, Integer createdBy) {
        // Validate admin permission
        AdminPermissionValidator.validateAdminPermission(createdBy, userDAO);
        
        // Validate business rules
        validateLessonDate(lessonDate);
        validateTopics(topics);
        
        // Create lesson
        Lesson lesson = new Lesson(lessonDate, monthGroup, createdBy);
        Integer lessonId = lessonDAO.insert(lesson);
        
        if (lessonId == null) {
            throw new ServiceException("Failed to create lesson");
        }
        
        // Set lessonId in all topics
        topics.forEach(topic -> topic.setLessonId(lessonId));
        
        // Bulk insert topics
        boolean topicsInserted = insertTopicsBulk(topics);
        if (!topicsInserted) {
            throw new ServiceException("Failed to insert lesson topics");
        }
        
        // Collect topic IDs
        List<Integer> topicIds = topics.stream()
                .map(LessonTopic::getTopicId)
                .collect(Collectors.toList());
        
        // Publish event
        LessonCreatedEvent event = new LessonCreatedEvent(
                lessonId, lessonDate, monthGroup, topicIds, createdBy);
        eventBus.post(event);
        
        return lessonId;
    }

    @Override
    public boolean updateLesson(Integer lessonId, LocalDate lessonDate, String monthGroup, 
                               List<LessonTopic> topics, Integer updatedBy) {
        // Validate admin permission
        AdminPermissionValidator.validateAdminPermission(updatedBy, userDAO);
        
        // Validate business rules
        validateLessonDate(lessonDate);
        validateTopics(topics);
        
        // Check lesson exists
        Lesson lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new LessonNotFoundException( lessonId , "Lesson not found with ID: " );
        }
        
        // Update lesson
        lesson.setLessonDate(lessonDate);
        lesson.setMonthGroup(monthGroup);
        boolean updated = lessonDAO.update(lesson);
        
        if (!updated) {
            return false;
        }
        
        // Delete old topics
        lessonTopicDAO.deleteByLessonId(lessonId);
        
        // Set lessonId in new topics
        topics.forEach(topic -> topic.setLessonId(lessonId));
        
        // Insert new topics
        return insertTopicsBulk(topics);
    }

    @Override
    public boolean deleteLesson(Integer lessonId, Integer deletedBy) {
        // Validate admin permission
        AdminPermissionValidator.validateAdminPermission(deletedBy, userDAO);
        
        // Delete lesson (CASCADE handles related data)
        return lessonDAO.delete(lessonId);
    }

    @Override
    public Lesson getLessonById(Integer lessonId) {
        Lesson lesson = lessonDAO.findById(lessonId);
        if (lesson == null) {
            throw new LessonNotFoundException( lessonId, "Lesson not found with ID: ");
        }
        return lesson;
    }

    @Override
    public List<Lesson> getAllLessons() {
        return lessonDAO.findAll();
    }

    @Override
    public List<Lesson> getLessonsByDateRange(LocalDate start, LocalDate end) {
        return lessonDAO.findByDateRange(start, end);
    }

    @Override
    public List<Lesson> getLessonsByMonth(String monthGroup) {
        return lessonDAO.findByMonthGroup(monthGroup);
    }

    @Override
    public Lesson getLatestLesson() {
        return lessonDAO.findLatest();
    }

    @Override
    public LessonDetail getLessonDetail(Integer lessonId) {
        // Get lesson
        Lesson lesson = getLessonById(lessonId);
        
        // Get topics
        List<LessonTopic> topics = lessonTopicDAO.findByLessonId(lessonId);
        
        // Get quiz (nullable)
        Quiz quiz = quizDAO.findByLessonId(lessonId);
        
        // Get attendance summary
        AttendanceSummary attendanceStats = buildAttendanceSummary(lessonId);
        
        // Get homework summary (nullable)
        HomeworkSummary homeworkStats = buildHomeworkSummary(lessonId);
        
        return new LessonDetail(lesson, topics, quiz, attendanceStats, homeworkStats);
    }

    @Override
    public List<LessonTopic> getLessonTopics(Integer lessonId) {
        return lessonTopicDAO.findByLessonId(lessonId);
    }

    @Override
    public int getTotalLessonCount() {
        return lessonDAO.countAll();
    }

    @Override
    public List<Lesson> searchLessonsByTopic(String searchTerm) {
        List<LessonTopic> topics = lessonTopicDAO.searchBySpecificTopic(searchTerm);
        
        return topics.stream()
                .map(topic -> lessonDAO.findById(topic.getLessonId()))
                .distinct()
                .collect(Collectors.toList());
    }

    // ===== Private Helper Methods =====

    private void validateLessonDate(LocalDate lessonDate) {
        if (lessonDate.isAfter(LocalDate.now())) {
            throw new InvalidLessonDateException("Lesson date cannot be in the future");
        }
    }

    private void validateTopics(List<LessonTopic> topics) {
        if (topics == null || topics.isEmpty()) {
            throw new InvalidTopicsException("At least one topic is required");
        }
    }

    private boolean insertTopicsBulk(List<LessonTopic> topics) {
        // Insert topics one by one and collect IDs
        for (LessonTopic topic : topics) {
            Integer topicId = lessonTopicDAO.insert(topic);
            if (topicId == null) {
                return false;
            }
            topic.setTopicId(topicId);
        }
        return true;
    }

    private AttendanceSummary buildAttendanceSummary(Integer lessonId) {
        List<Attendance> attendanceList = attendanceDAO.findByLessonId(lessonId);
        
        int totalStudents = attendanceList.size();
        int presentCount = (int) attendanceList.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();
        int absentCount = totalStudents - presentCount;
        
        return new AttendanceSummary(totalStudents, presentCount, absentCount);
    }

    private HomeworkSummary buildHomeworkSummary(Integer lessonId) {
        Map<HomeworkStatus, Integer> stats = homeworkDAO.getHomeworkStatsByLesson(lessonId);
        
        if (stats == null || stats.isEmpty()) {
            return null; // No homework for this lesson
        }
        
        int doneCount = stats.getOrDefault(HomeworkStatus.DONE, 0);
        int partiallyDoneCount = stats.getOrDefault(HomeworkStatus.PARTIALLY_DONE, 0);
        int notDoneCount = stats.getOrDefault(HomeworkStatus.NOT_DONE, 0);
        int totalStudents = doneCount + partiallyDoneCount + notDoneCount;
        
        return new HomeworkSummary(totalStudents, doneCount, partiallyDoneCount, notDoneCount);
    }
}