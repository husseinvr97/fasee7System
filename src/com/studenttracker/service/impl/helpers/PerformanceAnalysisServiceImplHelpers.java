package com.studenttracker.service.impl.helpers;

import com.studenttracker.model.PerformanceIndicator;
import com.studenttracker.model.PerformanceTrend;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizScore;
import com.studenttracker.model.LessonTopic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Helper class for PerformanceAnalysisServiceImpl.
 * Contains utility methods for PI calculation and analysis.
 */
public class PerformanceAnalysisServiceImplHelpers {
    
    private PerformanceAnalysisServiceImplHelpers() {}
    
    
    // ========== Essay Scoring Helper ==========
    
    /**
     * Calculate correct/wrong count for essay questions based on proportional scoring.
     * 
     * @param pointsEarned Points earned on the essay
     * @param maxPoints Maximum possible points
     * @return Array: [correctCount, wrongCount]
     */
    public static double[] getEssayCorrectWrongCount(BigDecimal pointsEarned, BigDecimal maxPoints) {
        if (maxPoints == null || maxPoints.compareTo(BigDecimal.ZERO) == 0) {
            return new double[]{0.0, 0.0};
        }
        
        // Calculate proportion: pointsEarned / maxPoints
        BigDecimal proportion = pointsEarned.divide(maxPoints, 4, RoundingMode.HALF_UP);
        
        double correctCount = proportion.doubleValue();
        double wrongCount = 1.0 - correctCount;
        
        return new double[]{correctCount, wrongCount};
    }
    
    
    // ========== Grouping Helper ==========
    
    /**
     * Group quiz questions by category.
     * Uses LessonTopic.TopicCategory from QuizQuestion.
     * 
     * @param questions List of quiz questions
     * @return Map of LessonTopic.TopicCategory to list of questions
     */
    public static Map<LessonTopic.TopicCategory, List<QuizQuestion>> groupQuestionsByCategory(List<QuizQuestion> questions) {
        Map<LessonTopic.TopicCategory, List<QuizQuestion>> grouped = new HashMap<>();
        
        for (QuizQuestion question : questions) {
            LessonTopic.TopicCategory category = question.getCategory();
            grouped.putIfAbsent(category, new ArrayList<>());
            grouped.get(category).add(question);
        }
        
        return grouped;
    }
    
    
    // ========== PI Calculation Helper ==========
    
    /**
     * Calculate correct/wrong counts and PI for each category.
     * Uses LessonTopic.TopicCategory from QuizQuestion.
     * 
     * @param scores Student's quiz scores
     * @param questions Quiz questions
     * @return Map of LessonTopic.TopicCategory to [correct, wrong] counts
     */
    public static Map<LessonTopic.TopicCategory, double[]> calculateCategoryCorrectWrong(
            List<QuizScore> scores, List<QuizQuestion> questions) {
        
        // Create question lookup map
        Map<Integer, QuizQuestion> questionMap = new HashMap<>();
        for (QuizQuestion q : questions) {
            questionMap.put(q.getQuestionId(), q);
        }
        
        // Initialize category counts
        Map<LessonTopic.TopicCategory, double[]> categoryCounts = new HashMap<>();
        
        // Process each score
        for (QuizScore score : scores) {
            QuizQuestion question = questionMap.get(score.getQuestionId());
            if (question == null) continue;
            
            LessonTopic.TopicCategory category = question.getCategory();
            categoryCounts.putIfAbsent(category, new double[]{0.0, 0.0});
            
            double[] counts = categoryCounts.get(category);
            
            if (question.isMCQ()) {
                // MCQ: correct if score == max points, wrong otherwise
                if (score.getPointsEarned().compareTo(question.getPoints()) == 0) {
                    counts[0] += 1.0; // correct
                } else {
                    counts[1] += 1.0; // wrong
                }
            } else if (question.isEssay()) {
                // Essay: proportional scoring
                double[] essayCounts = getEssayCorrectWrongCount(
                    score.getPointsEarned(), 
                    question.getPoints()
                );
                counts[0] += essayCounts[0]; // correct
                counts[1] += essayCounts[1]; // wrong
            }
        }
        
        return categoryCounts;
    }
    
    
    // ========== Trend Analysis Helper ==========
    
    /**
     * Analyze performance trend based on PI history.
     * 
     * @param piHistory List of performance indicators ordered by date
     * @return PerformanceTrend (IMPROVING, DEGRADING, STABLE)
     */
    public static PerformanceTrend analyzeTrend(List<PerformanceIndicator> piHistory) {
        if (piHistory == null || piHistory.size() < 2) {
            return PerformanceTrend.STABLE;
        }
        
        // Compare recent half vs older half
        int midpoint = piHistory.size() / 2;
        
        double olderAvg = 0.0;
        for (int i = 0; i < midpoint; i++) {
            olderAvg += piHistory.get(i).getPiValue();
        }
        olderAvg /= midpoint;
        
        double recentAvg = 0.0;
        int recentCount = piHistory.size() - midpoint;
        for (int i = midpoint; i < piHistory.size(); i++) {
            recentAvg += piHistory.get(i).getPiValue();
        }
        recentAvg /= recentCount;
        
        // Determine trend with threshold (10% change)
        double changePercent = Math.abs((recentAvg - olderAvg) / (Math.abs(olderAvg) + 1.0)) * 100;
        
        if (changePercent < 10) {
            return PerformanceTrend.STABLE;
        } else if (recentAvg > olderAvg) {
            return PerformanceTrend.IMPROVING;
        } else {
            return PerformanceTrend.DEGRADING;
        }
    }
    
    
    // ========== Average Calculation Helper ==========
    
    /**
     * Calculate average PI across all categories.
     * Uses PerformanceIndicator.TopicCategory.
     * 
     * @param categoryPIs Map of PerformanceIndicator.TopicCategory to cumulative PI
     * @return Average PI value
     */
    public static double calculateAveragePI(Map<PerformanceIndicator.TopicCategory, Integer> categoryPIs) {
        if (categoryPIs == null || categoryPIs.isEmpty()) {
            return 0.0;
        }
        
        int sum = 0;
        for (Integer pi : categoryPIs.values()) {
            sum += pi;
        }
        
        return (double) sum / categoryPIs.size();
    }
    
    
    // ========== Category Conversion Helper ==========
    
    /**
     * Convert LessonTopic.TopicCategory to PerformanceIndicator.TopicCategory.
     * Assumes both enums have matching values.
     * 
     * @param lessonCategory LessonTopic.TopicCategory
     * @return PerformanceIndicator.TopicCategory
     */
    public static PerformanceIndicator.TopicCategory convertToPerformanceCategory(
            LessonTopic.TopicCategory lessonCategory) {
        if (lessonCategory == null) {
            return null;
        }
        return PerformanceIndicator.TopicCategory.valueOf(lessonCategory.name());
    }
}