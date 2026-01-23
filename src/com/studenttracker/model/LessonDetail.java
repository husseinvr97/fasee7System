package com.studenttracker.model;

import java.util.List;

public class LessonDetail {
    private Lesson lesson;
    private List<LessonTopic> topics;
    private Quiz quiz;
    private AttendanceSummary attendanceStats;
    private HomeworkSummary homeworkStats;

    public LessonDetail() {}

    public LessonDetail(Lesson lesson, List<LessonTopic> topics, Quiz quiz, 
                       AttendanceSummary attendanceStats, HomeworkSummary homeworkStats) {
        this.lesson = lesson;
        this.topics = topics;
        this.quiz = quiz;
        this.attendanceStats = attendanceStats;
        this.homeworkStats = homeworkStats;
    }

    // Getters and Setters
    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public List<LessonTopic> getTopics() { return topics; }
    public void setTopics(List<LessonTopic> topics) { this.topics = topics; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public AttendanceSummary getAttendanceStats() { return attendanceStats; }
    public void setAttendanceStats(AttendanceSummary attendanceStats) { 
        this.attendanceStats = attendanceStats; 
    }

    public HomeworkSummary getHomeworkStats() { return homeworkStats; }
    public void setHomeworkStats(HomeworkSummary homeworkStats) { 
        this.homeworkStats = homeworkStats; 
    }

    @Override
    public String toString() {
        return "LessonDetail{lesson=" + lesson + ", topicsCount=" + 
               (topics != null ? topics.size() : 0) + ", hasQuiz=" + (quiz != null) + "}";
    }
}