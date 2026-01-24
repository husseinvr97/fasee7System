package com.studenttracker.service;

import com.studenttracker.model.Homework;
import com.studenttracker.model.Homework.HomeworkStatus;
import com.studenttracker.model.HomeworkSummary;
import java.util.List;

/**
 * Service interface for Homework operations.
 * Handles business logic for marking, updating, and retrieving homework records.
 */
public interface HomeworkService {
    
    // ========== Mark Homework Operations ==========
    
    /**
     * Marks homework for a single student in a lesson.
     * 
     * @param lessonId ID of the lesson
     * @param studentId ID of the student
     * @param status Homework status (DONE, PARTIALLY_DONE, NOT_DONE)
     * @param markedBy ID of the user marking the homework
     * @return true if successful, false otherwise
     * @throws StudentNotFoundException if student doesn't exist
     * @throws ValidationException if student is archived or didn't attend lesson
     */
    boolean markHomework(Integer lessonId, Integer studentId, 
                        HomeworkStatus status, Integer markedBy);
    
    /**
     * Marks homework for multiple students (bulk operation).
     * 
     * @param homeworkList List of homework records to insert
     * @param markedBy ID of the user marking the homework
     * @return true if all successful, false otherwise
     * @throws ValidationException if any student didn't attend their respective lesson
     */
    boolean bulkMarkHomework(List<Homework> homeworkList, Integer markedBy);
    
    /**
     * Updates existing homework status.
     * 
     * @param homeworkId ID of the homework record
     * @param newStatus New homework status
     * @return true if successful, false otherwise
     */
    boolean updateHomework(Integer homeworkId, HomeworkStatus newStatus);
    
    
    // ========== Retrieval Operations ==========
    
    /**
     * Gets homework for a specific student in a specific lesson.
     * 
     * @param lessonId ID of the lesson
     * @param studentId ID of the student
     * @return Homework record or null if not found
     */
    Homework getHomework(Integer lessonId, Integer studentId);
    
    /**
     * Gets all homework records for a lesson.
     * 
     * @param lessonId ID of the lesson
     * @return List of homework records
     */
    List<Homework> getHomeworkByLesson(Integer lessonId);
    
    /**
     * Gets all homework records for a student.
     * 
     * @param studentId ID of the student
     * @return List of homework records
     */
    List<Homework> getHomeworkByStudent(Integer studentId);
    
    
    // ========== Statistics Operations ==========
    
    /**
     * Counts homework records for a student by status.
     * 
     * @param studentId ID of the student
     * @param status Homework status to count
     * @return Number of homework records with the given status
     */
    int getStudentHomeworkCount(Integer studentId, HomeworkStatus status);
    
    /**
     * Gets summary statistics for homework in a lesson.
     * 
     * @param lessonId ID of the lesson
     * @return HomeworkSummary with counts for each status
     */
    HomeworkSummary getLessonHomeworkSummary(Integer lessonId);
    
    /**
     * Calculates total homework points for a student.
     * Points: DONE=3, PARTIALLY_DONE=1, NOT_DONE=0
     * 
     * @param studentId ID of the student
     * @return Total homework points
     */
    int calculateHomeworkPoints(Integer studentId);
}