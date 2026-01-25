// ========== OverviewData.java ==========
package com.studenttracker.model;

public class OverviewData {
    private int totalLessons;
    private int totalActiveStudents;
    private int totalArchivedStudents;
    private double overallAttendanceRate;
    private int quizzesCount;
    private double averageQuizScore;
    private int warningsGenerated;
    private int studentsArchivedThisMonth;

    public OverviewData() {}

    public OverviewData(int totalLessons, int totalActiveStudents, int totalArchivedStudents,
                       double overallAttendanceRate, int quizzesCount, double averageQuizScore,
                       int warningsGenerated, int studentsArchivedThisMonth) {
        this.totalLessons = totalLessons;
        this.totalActiveStudents = totalActiveStudents;
        this.totalArchivedStudents = totalArchivedStudents;
        this.overallAttendanceRate = overallAttendanceRate;
        this.quizzesCount = quizzesCount;
        this.averageQuizScore = averageQuizScore;
        this.warningsGenerated = warningsGenerated;
        this.studentsArchivedThisMonth = studentsArchivedThisMonth;
    }

    public int getTotalLessons() { return totalLessons; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }

    public int getTotalActiveStudents() { return totalActiveStudents; }
    public void setTotalActiveStudents(int totalActiveStudents) { this.totalActiveStudents = totalActiveStudents; }

    public int getTotalArchivedStudents() { return totalArchivedStudents; }
    public void setTotalArchivedStudents(int totalArchivedStudents) { this.totalArchivedStudents = totalArchivedStudents; }

    public double getOverallAttendanceRate() { return overallAttendanceRate; }
    public void setOverallAttendanceRate(double overallAttendanceRate) { this.overallAttendanceRate = overallAttendanceRate; }

    public int getQuizzesCount() { return quizzesCount; }
    public void setQuizzesCount(int quizzesCount) { this.quizzesCount = quizzesCount; }

    public double getAverageQuizScore() { return averageQuizScore; }
    public void setAverageQuizScore(double averageQuizScore) { this.averageQuizScore = averageQuizScore; }

    public int getWarningsGenerated() { return warningsGenerated; }
    public void setWarningsGenerated(int warningsGenerated) { this.warningsGenerated = warningsGenerated; }

    public int getStudentsArchivedThisMonth() { return studentsArchivedThisMonth; }
    public void setStudentsArchivedThisMonth(int studentsArchivedThisMonth) { this.studentsArchivedThisMonth = studentsArchivedThisMonth; }
}