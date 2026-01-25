// ========== PerformanceSummary.java ==========
package com.studenttracker.model;

import com.studenttracker.model.PerformanceIndicator.TopicCategory;
import java.util.Map;

public class PerformanceSummary {
    private Map<TopicCategory, Double> averagePIByCategory;
    private TopicCategory strongestCategory;
    private TopicCategory weakestCategory;

    public PerformanceSummary() {}

    public PerformanceSummary(Map<TopicCategory, Double> averagePIByCategory,
                             TopicCategory strongestCategory, TopicCategory weakestCategory) {
        this.averagePIByCategory = averagePIByCategory;
        this.strongestCategory = strongestCategory;
        this.weakestCategory = weakestCategory;
    }

    public Map<TopicCategory, Double> getAveragePIByCategory() { return averagePIByCategory; }
    public void setAveragePIByCategory(Map<TopicCategory, Double> averagePIByCategory) { this.averagePIByCategory = averagePIByCategory; }

    public TopicCategory getStrongestCategory() { return strongestCategory; }
    public void setStrongestCategory(TopicCategory strongestCategory) { this.strongestCategory = strongestCategory; }

    public TopicCategory getWeakestCategory() { return weakestCategory; }
    public void setWeakestCategory(TopicCategory weakestCategory) { this.weakestCategory = weakestCategory; }
}