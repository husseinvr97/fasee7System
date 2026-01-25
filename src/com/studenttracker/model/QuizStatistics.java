package com.studenttracker.model;

/**
 * Statistics for quiz performance analysis.
 */
public class QuizStatistics {
    private Double averageScore;
    private Double highestScore;
    private Double lowestScore;
    private Double passRate;
    
    // Constructors
    public QuizStatistics() {}
    
    public QuizStatistics(Double averageScore, Double highestScore, 
                         Double lowestScore, Double passRate) {
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.passRate = passRate;
    }
    
    // Getters and Setters
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    
    public Double getHighestScore() { return highestScore; }
    public void setHighestScore(Double highestScore) { this.highestScore = highestScore; }
    
    public Double getLowestScore() { return lowestScore; }
    public void setLowestScore(Double lowestScore) { this.lowestScore = lowestScore; }
    
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    
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