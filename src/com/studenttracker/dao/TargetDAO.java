package com.studenttracker.dao;

import com.studenttracker.model.Target;
import static com.studenttracker.model.LessonTopic.TopicCategory;
import java.util.List;

public interface TargetDAO {
    // Standard CRUD Methods
    Integer insert(Target target);
    boolean update(Target target);
    boolean delete(int targetId);
    Target findById(int targetId);
    List<Target> findAll();
    
    // Custom Methods
    List<Target> findByStudentId(int studentId);
    List<Target> findActiveByStudent(int studentId);
    List<Target> findAchievedByStudent(int studentId);
    List<Target> findByStudentAndCategory(int studentId, TopicCategory category);
    int countActiveByStudent(int studentId);
    int countAchievedByStudent(int studentId);
    boolean hasActiveTarget(int studentId, TopicCategory category, int targetValue);
}