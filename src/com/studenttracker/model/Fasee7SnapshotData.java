// ========== Fasee7SnapshotData.java ==========
package com.studenttracker.model;

import java.util.List;
import java.util.Map;

public class Fasee7SnapshotData {
    private List<RankingEntry> currentTop10;
    private List<RankingEntry> previousTop10;
    private Map<Integer, Integer> rankChanges;

    public Fasee7SnapshotData() {}

    public Fasee7SnapshotData(List<RankingEntry> currentTop10, List<RankingEntry> previousTop10,
                             Map<Integer, Integer> rankChanges) {
        this.currentTop10 = currentTop10;
        this.previousTop10 = previousTop10;
        this.rankChanges = rankChanges;
    }

    public List<RankingEntry> getCurrentTop10() { return currentTop10; }
    public void setCurrentTop10(List<RankingEntry> currentTop10) { this.currentTop10 = currentTop10; }

    public List<RankingEntry> getPreviousTop10() { return previousTop10; }
    public void setPreviousTop10(List<RankingEntry> previousTop10) { this.previousTop10 = previousTop10; }

    public Map<Integer, Integer> getRankChanges() { return rankChanges; }
    public void setRankChanges(Map<Integer, Integer> rankChanges) { this.rankChanges = rankChanges; }
}