package com.studenttracker.model;

public class HomeworkSummary {
    private int totalStudents;
    private int doneCount;
    private int partiallyDoneCount;
    private int notDoneCount;

    public HomeworkSummary() {}

    public HomeworkSummary(int totalStudents, int doneCount, int partiallyDoneCount, int notDoneCount) {
        this.totalStudents = totalStudents;
        this.doneCount = doneCount;
        this.partiallyDoneCount = partiallyDoneCount;
        this.notDoneCount = notDoneCount;
    }

    // Getters and Setters
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

    public int getDoneCount() { return doneCount; }
    public void setDoneCount(int doneCount) { this.doneCount = doneCount; }

    public int getPartiallyDoneCount() { return partiallyDoneCount; }
    public void setPartiallyDoneCount(int partiallyDoneCount) { 
        this.partiallyDoneCount = partiallyDoneCount; 
    }

    public int getNotDoneCount() { return notDoneCount; }
    public void setNotDoneCount(int notDoneCount) { this.notDoneCount = notDoneCount; }

    // Calculate percentage methods to add to existing HomeworkSummary
    public double getDonePercentage() {
        return totalStudents > 0 ? (doneCount * 100.0 / totalStudents) : 0.0;
    }

    public double getPartiallyDonePercentage() {
        return totalStudents > 0 ? (partiallyDoneCount * 100.0 / totalStudents) : 0.0;
    }

    public double getNotDonePercentage() {
        return totalStudents > 0 ? (notDoneCount * 100.0 / totalStudents) : 0.0;
    }

    @Override
    public String toString() {
        return String.format("HomeworkSummary{total=%d, done=%d, partial=%d, notDone=%d}",
                totalStudents, doneCount, partiallyDoneCount, notDoneCount);
    }
}