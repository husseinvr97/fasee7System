package com.studenttracker.service.event;


/**
 * Event published when bulk quiz grading is completed.
 */
public class QuizGradingCompletedEvent implements Event {
    private final Integer quizId;
    private final Integer lessonId;
    private final Integer totalStudentsGraded;
    private final Integer completedBy;
    
    public QuizGradingCompletedEvent(Integer quizId, Integer lessonId, Integer totalStudentsGraded, Integer completedBy) {
        this.quizId = quizId;
        this.lessonId = lessonId;
        this.totalStudentsGraded = totalStudentsGraded;
        this.completedBy = completedBy;
    }
    
    public Integer getQuizId() { return quizId; }
    public Integer getLessonId() { return lessonId; }
    public Integer getTotalStudentsGraded() { return totalStudentsGraded; }
    public Integer getCompletedBy() { return completedBy; }
    
    @Override
    public String toString() {
        return "QuizGradingCompletedEvent{" +
                "quizId=" + quizId +
                ", lessonId=" + lessonId +
                ", totalStudentsGraded=" + totalStudentsGraded +
                ", completedBy=" + completedBy +
                '}';
    }
}