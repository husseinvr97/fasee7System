package com.studenttracker.dao;

import com.studenttracker.model.Lesson;
import java.time.LocalDate;
import java.util.List;

public interface LessonDAO {
    
    // Standard CRUD operations
    Integer insert(Lesson lesson);
    boolean update(Lesson lesson);
    boolean delete(int lessonId);
    Lesson findById(int lessonId);
    List<Lesson> findAll();
    
    // Custom methods
    List<Lesson> findByDateRange(LocalDate startDate, LocalDate endDate);
    List<Lesson> findByMonthGroup(String monthGroup);
    List<Lesson> findByCreatedBy(int userId);
    int countAll();
    Lesson findLatest();
}