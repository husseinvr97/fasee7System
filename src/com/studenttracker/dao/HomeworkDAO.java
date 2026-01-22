package com.studenttracker.dao;

import com.studenttracker.model.Homework;
import com.studenttracker.model.Homework.HomeworkStatus;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for Homework entity operations.
 */
public interface HomeworkDAO {
    
    // Standard CRUD operations
    Integer insert(Homework homework);
    boolean update(Homework homework);
    boolean delete(int homeworkId);
    Homework findById(int homeworkId);
    List<Homework> findAll();
    
    // Custom query methods
    List<Homework> findByLessonId(int lessonId);
    List<Homework> findByStudentId(int studentId);
    Homework findByLessonAndStudent(int lessonId, int studentId);
    int countByStudentAndStatus(int studentId, HomeworkStatus status);
    boolean bulkInsert(List<Homework> homeworkList);
    Map<HomeworkStatus, Integer> getHomeworkStatsByLesson(int lessonId);
}