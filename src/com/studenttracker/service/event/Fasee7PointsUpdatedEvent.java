package com.studenttracker.service.event;

/**
 * Event published when a student's Fasee7 points are updated.
 */
public class Fasee7PointsUpdatedEvent implements Event {
    
    private final Integer studentId;
    private final Double quizPoints;
    private final Integer attendancePoints;
    private final Integer homeworkPoints;
    private final Integer targetPoints;
    private final Double totalPoints;
    
    public Fasee7PointsUpdatedEvent(Integer studentId, Double quizPoints, Integer attendancePoints, Integer homeworkPoints, Integer targetPoints, Double totalPoints) {
        this.studentId = studentId;
        this.quizPoints = quizPoints;
        this.attendancePoints = attendancePoints;
        this.homeworkPoints = homeworkPoints;
        this.targetPoints = targetPoints;
        this.totalPoints = totalPoints;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public Double getQuizPoints() {
        return quizPoints;
    }
    
    public Integer getAttendancePoints() {
        return attendancePoints;
    }
    
    public Integer getHomeworkPoints() {
        return homeworkPoints;
    }
    
    public Integer getTargetPoints() {
        return targetPoints;
    }
    
    public Double getTotalPoints() {
        return totalPoints;
    }
    
    @Override
    public String toString() {
        return "Fasee7PointsUpdatedEvent{" +
                "studentId=" + studentId +
                ", quizPoints=" + quizPoints +
                ", attendancePoints=" + attendancePoints +
                ", homeworkPoints=" + homeworkPoints +
                ", targetPoints=" + targetPoints +
                ", totalPoints=" + totalPoints +
                '}';
    }
}