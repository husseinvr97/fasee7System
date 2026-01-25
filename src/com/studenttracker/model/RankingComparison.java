package com.studenttracker.model;

/**
 * Represents a comparison between two rankings for a student.
 */
public class RankingComparison {
    
    private Integer studentId;
    private int oldRank;
    private int newRank;
    private int rankChange;
    
    public RankingComparison() {
    }
    
    public RankingComparison(Integer studentId, int oldRank, int newRank) {
        this.studentId = studentId;
        this.oldRank = oldRank;
        this.newRank = newRank;
        this.rankChange = oldRank - newRank; // Positive = improved
    }
    
    public Integer getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    
    public int getOldRank() {
        return oldRank;
    }
    
    public void setOldRank(int oldRank) {
        this.oldRank = oldRank;
        this.rankChange = oldRank - newRank;
    }
    
    public int getNewRank() {
        return newRank;
    }
    
    public void setNewRank(int newRank) {
        this.newRank = newRank;
        this.rankChange = oldRank - newRank;
    }
    
    public int getRankChange() {
        return rankChange;
    }
    
    @Override
    public String toString() {
        return "RankingComparison{" +
                "studentId=" + studentId +
                ", oldRank=" + oldRank +
                ", newRank=" + newRank +
                ", rankChange=" + rankChange +
                '}';
    }
}