package com.studenttracker.service;

import com.studenttracker.model.*;
import java.time.LocalDate;
import java.util.List;

public interface LessonService {
    
    // Lesson Creation
    Integer createLesson(LocalDate lessonDate, String monthGroup, 
                        List<LessonTopic> topics, Integer createdBy);
    
    boolean updateLesson(Integer lessonId, LocalDate lessonDate, String monthGroup, 
                        List<LessonTopic> topics, Integer updatedBy);
    
    boolean deleteLesson(Integer lessonId, Integer deletedBy);
    
    // Lesson Retrieval
    Lesson getLessonById(Integer lessonId);
    List<Lesson> getAllLessons();
    List<Lesson> getLessonsByDateRange(LocalDate start, LocalDate end);
    List<Lesson> getLessonsByMonth(String monthGroup);
    Lesson getLatestLesson();
    
    // Lesson Details (Aggregated Data)
    LessonDetail getLessonDetail(Integer lessonId);
    List<LessonTopic> getLessonTopics(Integer lessonId);
    
    // Statistics
    int getTotalLessonCount();
    List<Lesson> searchLessonsByTopic(String searchTerm);
}