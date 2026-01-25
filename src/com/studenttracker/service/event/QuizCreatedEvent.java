package com.studenttracker.service.event;

import com.studenttracker.model.LessonTopic.TopicCategory;
import java.util.List;

/**
 * Event published when a quiz is created.
 */
public class QuizCreatedEvent implements Event {
    
    private final Integer quizId;
    private final Integer lessonId;
    private final Double totalMarks;
    private final Integer questionCount;
    private final List<TopicCategory> categories;
    private final Integer createdBy;
    
    public QuizCreatedEvent(Integer quizId, Integer lessonId, Double totalMarks, Integer questionCount, List<TopicCategory> categories, Integer createdBy) {
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.totalMarks = totalMarks;
        this.questionCount = questionCount;
        this.categories = categories;
        this.createdBy = createdBy;
    }
    
    public Integer getQuizId() {
        return quizId;
    }
    
    public Integer getLessonId() {
        return lessonId;
    }
    
    public Double getTotalMarks() {
        return totalMarks;
    }
    
    public Integer getQuestionCount() {
        return questionCount;
    }
    
    public List<TopicCategory> getCategories() {
        return categories;
    }
    
    @Override
    public String toString() {
        return "QuizCreatedEvent{" +
                "quizId=" + quizId +
                ", lessonId=" + lessonId +
                ", totalMarks=" + totalMarks +
                ", questionCount=" + questionCount +
                ", categories=" + categories +
                ", createdBy=" + createdBy +
                '}';
    }
}