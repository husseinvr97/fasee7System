// ========== TargetSummary.java ==========
package com.studenttracker.model;

import java.util.List;

public class TargetSummary {
    private int activeTargetsCount;
    private int achievedTargetsThisMonth;
    private List<TopAchiever> topAchievers;

    public TargetSummary() {}

    public TargetSummary(int activeTargetsCount, int achievedTargetsThisMonth,
                        List<TopAchiever> topAchievers) {
        this.activeTargetsCount = activeTargetsCount;
        this.achievedTargetsThisMonth = achievedTargetsThisMonth;
        this.topAchievers = topAchievers;
    }

    public int getActiveTargetsCount() { return activeTargetsCount; }
    public void setActiveTargetsCount(int activeTargetsCount) { this.activeTargetsCount = activeTargetsCount; }

    public int getAchievedTargetsThisMonth() { return achievedTargetsThisMonth; }
    public void setAchievedTargetsThisMonth(int achievedTargetsThisMonth) { this.achievedTargetsThisMonth = achievedTargetsThisMonth; }

    public List<TopAchiever> getTopAchievers() { return topAchievers; }
    public void setTopAchievers(List<TopAchiever> topAchievers) { this.topAchievers = topAchievers; }
}