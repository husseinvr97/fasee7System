package com.studenttracker.model;

public class AttendanceSummary {
    private int totalStudents;
    private int presentCount;
    private int absentCount;
    private double attendancePercentage;

    public AttendanceSummary() {}

    public AttendanceSummary(int totalStudents, int presentCount, int absentCount) {
        this.totalStudents = totalStudents;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.attendancePercentage = totalStudents > 0 
            ? (presentCount * 100.0 / totalStudents) 
            : 0.0;
    }

    // Getters and Setters
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { 
        this.attendancePercentage = attendancePercentage; 
    }

    @Override
    public String toString() {
        return String.format("AttendanceSummary{total=%d, present=%d, absent=%d, percentage=%.2f%%}",
                totalStudents, presentCount, absentCount, attendancePercentage);
    }
}