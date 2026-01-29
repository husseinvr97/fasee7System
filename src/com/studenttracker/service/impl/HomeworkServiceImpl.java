package com.studenttracker.service.impl;

import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.HomeworkDAO;
import com.studenttracker.dao.StudentDAO;
import com.studenttracker.exception.StudentNotFoundException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Homework;
import com.studenttracker.model.Homework.HomeworkStatus;
import com.studenttracker.model.HomeworkSummary;
import com.studenttracker.model.Student;
import com.studenttracker.model.Student.StudentStatus;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.HomeworkService;
import com.studenttracker.service.event.HomeworkBatchCompletedEvent;
import com.studenttracker.service.event.HomeworkRecordedEvent;

import java.util.List;
import java.util.Map;

/**
 * Implementation of HomeworkService.
 * Handles business logic, validation, and event publishing for homework operations.
 */
public class HomeworkServiceImpl implements HomeworkService {
    
    private final HomeworkDAO homeworkDAO;
    private final StudentDAO studentDAO;
    private final AttendanceDAO attendanceDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param homeworkDAO DAO for homework data access
     * @param studentDAO DAO for student data access
     * @param attendanceDAO DAO for attendance data access
     */
    public HomeworkServiceImpl(HomeworkDAO homeworkDAO, StudentDAO studentDAO, 
                              AttendanceDAO attendanceDAO , EventBusService eventBusService) {
        this.homeworkDAO = homeworkDAO;
        this.studentDAO = studentDAO;
        this.attendanceDAO = attendanceDAO;
        this.eventBusService = eventBusService;
    }
    
    
    // ========== Mark Homework Operations ==========
    
    @Override
    public boolean markHomework(Integer lessonId, Integer studentId, 
                               HomeworkStatus status, Integer markedBy) {
        // Step 1: Validate student exists and is ACTIVE
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(studentId);
        }
        
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new ValidationException("Cannot mark homework for archived student: " + studentId);
        }
        
        // Step 2: Validate student attended this lesson
        Attendance attendance = attendanceDAO.findByLessonAndStudent(lessonId, studentId);
        if (attendance == null) {
    throw new IllegalStateException("Student has no attendance record for this lesson");
}

if (!attendance.isPresent()) {
    throw new IllegalStateException("Cannot mark homework for absent student");
}
        
        // Step 3: Create homework record
        Homework homework = new Homework(lessonId, studentId, status, markedBy);
        
        // Step 4: Insert into database
        Integer homeworkId = homeworkDAO.insert(homework);
        
        if (homeworkId == null) {
            return false;
        }
        
        // Step 5: Set the ID on the homework object for the event
        homework.setHomeworkId(homeworkId);
        
        // Step 6: Publish HomeworkRecordedEvent
        HomeworkRecordedEvent event = new HomeworkRecordedEvent(lessonId, studentId, status , markedBy);
        eventBusService.publish(event);
        
        return true;
    }
    
    @Override
    public boolean bulkMarkHomework(List<Homework> homeworkList, Integer markedBy) {
        if (homeworkList == null || homeworkList.isEmpty()) {
            throw new ValidationException("Homework list cannot be null or empty");
        }
        
        // Step 1: Validate all students attended their respective lessons
        for (Homework hw : homeworkList) {
            // Validate student exists and is ACTIVE
            Student student = studentDAO.findById(hw.getStudentId());
            if (student == null) {
                throw new StudentNotFoundException(hw.getStudentId());
            }
            
            if (student.getStatus() != StudentStatus.ACTIVE) {
                throw new ValidationException("Cannot mark homework for archived student: " + 
                                            hw.getStudentId());
            }
            
            // Validate student attended the lesson
            Attendance attendance = attendanceDAO.findByLessonAndStudent(
                hw.getLessonId(), hw.getStudentId()
            );
            
            if (attendance == null || !attendance.isPresent()) {
                throw new ValidationException("Student " + hw.getStudentId() + 
                                            " did not attend lesson " + hw.getLessonId());
            }
        }
        
        // Step 2: Bulk insert all homework records
        boolean success = homeworkDAO.bulkInsert(homeworkList);
        
        if (!success) {
            return false;
        }
        
        // Step 3: Publish HomeworkRecordedEvent for each homework
        for (Homework hw : homeworkList) {
            HomeworkRecordedEvent event = new HomeworkRecordedEvent(hw.getLessonId(), hw.getStudentId(), hw.getStatus(), markedBy);
            eventBusService.publish(event);
        }
        
        // Step 4: Publish HomeworkBatchCompletedEvent
        HomeworkBatchCompletedEvent batchEvent = new HomeworkBatchCompletedEvent(
            homeworkList.get(0).getLessonId(),
            homeworkList.size(),
            (int)homeworkList.stream().filter(hw -> hw.getStatus() == HomeworkStatus.DONE).count(),
            (int)homeworkList.stream().filter(hw -> hw.getStatus() == HomeworkStatus.PARTIALLY_DONE).count(),
            (int)homeworkList.stream().filter(hw -> hw.getStatus() == HomeworkStatus.NOT_DONE).count(),
            markedBy
        );
        eventBusService.publish(batchEvent);
        
        return true;
    }
    
    @Override
    public boolean updateHomework(Integer homeworkId, HomeworkStatus newStatus) {
        if (homeworkId == null || newStatus == null) {
            throw new ValidationException("Homework ID and status cannot be null");
        }
        
        // Step 1: Find existing homework
        Homework homework = homeworkDAO.findById(homeworkId);
        if (homework == null) {
            throw new ValidationException("Homework not found: " + homeworkId);
        }
        
        // Step 2: Update status
        homework.setStatus(newStatus);
        
        // Step 3: Persist changes
        boolean success = homeworkDAO.update(homework);
        
        if (!success) {
            return false;
        }
        
        // Step 4: Publish HomeworkRecordedEvent
        HomeworkRecordedEvent event = new HomeworkRecordedEvent(homework.getLessonId(), homework.getStudentId(), newStatus, homework.getMarkedBy());
        eventBusService.publish(event);
        
        return true;
    }
    
    
    // ========== Retrieval Operations ==========
    
    @Override
    public Homework getHomework(Integer lessonId, Integer studentId) {
        if (lessonId == null || studentId == null) {
            throw new ValidationException("Lesson ID and student ID cannot be null");
        }
        return homeworkDAO.findByLessonAndStudent(lessonId, studentId);
    }
    
    @Override
    public List<Homework> getHomeworkByLesson(Integer lessonId) {
        if (lessonId == null) {
            throw new ValidationException("Lesson ID cannot be null");
        }
        return homeworkDAO.findByLessonId(lessonId);
    }
    
    @Override
    public List<Homework> getHomeworkByStudent(Integer studentId) {
        if (studentId == null) {
            throw new ValidationException("Student ID cannot be null");
        }
        return homeworkDAO.findByStudentId(studentId);
    }
    
    
    // ========== Statistics Operations ==========
    
    @Override
    public int getStudentHomeworkCount(Integer studentId, HomeworkStatus status) {
        if (studentId == null || status == null) {
            throw new ValidationException("Student ID and status cannot be null");
        }
        return homeworkDAO.countByStudentAndStatus(studentId, status);
    }
    
    @Override
    public HomeworkSummary getLessonHomeworkSummary(Integer lessonId) {
        if (lessonId == null) {
            throw new ValidationException("Lesson ID cannot be null");
        }
        
        // Step 1: Get homework statistics from DAO
        Map<HomeworkStatus, Integer> stats = homeworkDAO.getHomeworkStatsByLesson(lessonId);
        
        // Step 2: Extract counts for each status
        int doneCount = stats.getOrDefault(HomeworkStatus.DONE, 0);
        int partiallyDoneCount = stats.getOrDefault(HomeworkStatus.PARTIALLY_DONE, 0);
        int notDoneCount = stats.getOrDefault(HomeworkStatus.NOT_DONE, 0);
        
        // Step 3: Calculate total
        int totalStudents = doneCount + partiallyDoneCount + notDoneCount;
        
        // Step 4: Create and return summary
        return new HomeworkSummary(totalStudents, doneCount, partiallyDoneCount, notDoneCount);
    }
    
    @Override
    public int calculateHomeworkPoints(Integer studentId) {
        if (studentId == null) {
            throw new ValidationException("Student ID cannot be null");
        }
        
        // Step 1: Get all homework for student
        List<Homework> homeworkList = homeworkDAO.findByStudentId(studentId);
        
        // Step 2: Sum points
        int totalPoints = 0;
        for (Homework hw : homeworkList) {
            totalPoints += hw.getPoints();
        }
        
        return totalPoints;
    }
}