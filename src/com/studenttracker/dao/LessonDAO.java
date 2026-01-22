package com.studenttracker.dao;

import com.studenttracker.model.Lesson;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for Lesson entity operations.
 */
public interface LessonDAO {
    
    // Standard CRUD operations
    Integer insert(Lesson lesson);
    boolean update(Lesson lesson);
    boolean delete(int lessonId);
    Lesson findById(int lessonId);
    List<Lesson> findAll();
    
    // Custom query methods
    List<Lesson> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Lesson> findByMonthGroup(String monthGroup);
    List<Lesson> findByCreatedBy(int userId);
    int countAll();
    Lesson findLatest();
}