package com.studenttracker.model;

import java.time.LocalDateTime;

public class Fasee7Points {
    private Integer pointsId;
    private Integer studentId;
    private Double quizPoints;
    private int attendancePoints;
    private int homeworkPoints;
    private int targetPoints;
    private Double totalPoints;
    private LocalDateTime lastUpdated;

    public Fasee7Points() {
    }

    public Fasee7Points(Integer studentId, Double quizPoints, int attendancePoints, 
                        int homeworkPoints, int targetPoints, Double totalPoints, 
                        LocalDateTime lastUpdated) {
        this.studentId = studentId;
        this.quizPoints = quizPoints;
        this.attendancePoints = attendancePoints;
        this.homeworkPoints = homeworkPoints;
        this.targetPoints = targetPoints;
        this.totalPoints = totalPoints;
        this.lastUpdated = lastUpdated;
    }

    public Integer getPointsId() {
        return pointsId;
    }

    public void setPointsId(Integer pointsId) {
        this.pointsId = pointsId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Double getQuizPoints() {
        return quizPoints;
    }

    public void setQuizPoints(Double quizPoints) {
        this.quizPoints = quizPoints;
    }

    public int getAttendancePoints() {
        return attendancePoints;
    }

    public void setAttendancePoints(int attendancePoints) {
        this.attendancePoints = attendancePoints;
    }

    public int getHomeworkPoints() {
        return homeworkPoints;
    }

    public void setHomeworkPoints(int homeworkPoints) {
        this.homeworkPoints = homeworkPoints;
    }

    public int getTargetPoints() {
        return targetPoints;
    }

    public void setTargetPoints(int targetPoints) {
        this.targetPoints = targetPoints;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void recalculateTotal() {
        this.totalPoints = quizPoints + attendancePoints + homeworkPoints + targetPoints;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Fasee7Points{" +
                "pointsId=" + pointsId +
                ", studentId=" + studentId +
                ", quizPoints=" + quizPoints +
                ", attendancePoints=" + attendancePoints +
                ", homeworkPoints=" + homeworkPoints +
                ", targetPoints=" + targetPoints +
                ", totalPoints=" + totalPoints +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}