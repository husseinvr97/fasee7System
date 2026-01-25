// ========== TopAchiever.java ==========
package com.studenttracker.model;

public class TopAchiever {
    private int studentId;
    private String studentName;
    private int targetPointsEarned;

    public TopAchiever() {}

    public TopAchiever(int studentId, String studentName, int targetPointsEarned) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.targetPointsEarned = targetPointsEarned;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getTargetPointsEarned() { return targetPointsEarned; }
    public void setTargetPointsEarned(int targetPointsEarned) { this.targetPointsEarned = targetPointsEarned; }
}