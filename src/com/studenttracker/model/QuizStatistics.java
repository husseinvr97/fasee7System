package com.studenttracker.model;

import java.math.BigDecimal;

/**
 * Statistics for quiz performance analysis.
 */
public class QuizStatistics {
    private BigDecimal averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
    private BigDecimal passRate;
    
    // Constructors
    public QuizStatistics() {}
    
    public QuizStatistics(BigDecimal averageScore, BigDecimal highestScore, 
                         BigDecimal lowestScore, BigDecimal passRate) {
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.passRate = passRate;
    }
    
    // Getters and Setters
    public BigDecimal getAverageScore() { return averageScore; }
    public void setAverageScore(BigDecimal averageScore) { this.averageScore = averageScore; }
    
    public BigDecimal getHighestScore() { return highestScore; }
    public void setHighestScore(BigDecimal highestScore) { this.highestScore = highestScore; }
    
    public BigDecimal getLowestScore() { return lowestScore; }
    public void setLowestScore(BigDecimal lowestScore) { this.lowestScore = lowestScore; }
    
    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }
    
    @Override
    public String toString() {
        return "QuizStatistics{" +
                "average=" + averageScore +
                ", highest=" + highestScore +
                ", lowest=" + lowestScore +
                ", passRate=" + passRate + "%" +
                '}';
    }
}