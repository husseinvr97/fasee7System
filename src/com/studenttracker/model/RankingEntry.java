// ========== RankingEntry.java ==========
package com.studenttracker.model;

public class RankingEntry {
    private int studentId;
    private String studentName;
    private int rank;
    private double totalPoints;
    private double quizPoints;
    private int attendancePoints;
    private int homeworkPoints;
    private int targetPoints;

    public RankingEntry() {}

    public RankingEntry(int studentId, String studentName, int rank, double totalPoints,
                       double quizPoints, int attendancePoints, int homeworkPoints, int targetPoints) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rank = rank;
        this.totalPoints = totalPoints;
        this.quizPoints = quizPoints;
        this.attendancePoints = attendancePoints;
        this.homeworkPoints = homeworkPoints;
        this.targetPoints = targetPoints;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(double totalPoints) { this.totalPoints = totalPoints; }

    public double getQuizPoints() { return quizPoints; }
    public void setQuizPoints(double quizPoints) { this.quizPoints = quizPoints; }

    public int getAttendancePoints() { return attendancePoints; }
    public void setAttendancePoints(int attendancePoints) { this.attendancePoints = attendancePoints; }

    public int getHomeworkPoints() { return homeworkPoints; }
    public void setHomeworkPoints(int homeworkPoints) { this.homeworkPoints = homeworkPoints; }

    public int getTargetPoints() { return targetPoints; }
    public void setTargetPoints(int targetPoints) { this.targetPoints = targetPoints; }
}