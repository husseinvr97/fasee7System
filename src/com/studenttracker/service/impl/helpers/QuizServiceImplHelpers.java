package com.studenttracker.service.impl.helpers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.QuizCategoryTotal;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizScore;

public class QuizServiceImplHelpers 
{
    private QuizServiceImplHelpers() {}

        /**
     * Calculates category totals for a student's quiz.
     * 
     * @param quizId ID of the quiz
     * @param studentId ID of the student
     * @param scores Student's scores
     * @param questions Quiz questions
     * @return List of category totals
     */
    public static List<QuizCategoryTotal> calculateCategoryTotals(Integer quizId, Integer studentId,
                                                            List<QuizScore> scores, 
                                                            List<QuizQuestion> questions) {
        // Create question map
        Map<Integer, QuizQuestion> questionMap = new HashMap<>();
        for (QuizQuestion q: questions) {
questionMap.put(q.getQuestionId(), q);
}
    // Calculate totals per category
    Map<TopicCategory, BigDecimal> earnedByCategory = new HashMap<>();
    Map<TopicCategory, BigDecimal> totalByCategory = new HashMap<>();
    
    for (QuizScore score : scores) {
        QuizQuestion question = questionMap.get(score.getQuestionId());
        if (question != null) {
            TopicCategory category = question.getCategory();
            
            // Add earned points
            BigDecimal currentEarned = earnedByCategory.getOrDefault(category, BigDecimal.ZERO);
            earnedByCategory.put(category, currentEarned.add(score.getPointsEarned()));
            
            // Add total points
            BigDecimal currentTotal = totalByCategory.getOrDefault(category, BigDecimal.ZERO);
            totalByCategory.put(category, currentTotal.add(question.getPoints()));
        }
    }
    
    // Create QuizCategoryTotal objects
    List<QuizCategoryTotal> categoryTotals = new ArrayList<>();
    for (TopicCategory category : earnedByCategory.keySet()) {
        BigDecimal earned = earnedByCategory.get(category);
        BigDecimal total = totalByCategory.get(category);
        
        QuizCategoryTotal categoryTotal = new QuizCategoryTotal(
            quizId, studentId, 
            convertToQuizCategoryTotalEnum(category), 
            earned, total
        );
        categoryTotals.add(categoryTotal);
    }
    
    return categoryTotals;
}

/**
 * Converts LessonTopic.TopicCategory to QuizCategoryTotal.TopicCategory.
 * Note: This assumes they have the same values.
 */
private static QuizCategoryTotal.TopicCategory convertToQuizCategoryTotalEnum(TopicCategory lessonCategory) {
    // Assuming QuizCategoryTotal.TopicCategory should match LessonTopic.TopicCategory
    // If not, you need to update QuizCategoryTotal to use LessonTopic.TopicCategory
    return QuizCategoryTotal.TopicCategory.valueOf(lessonCategory.name());
}

/**
 * Calculates total score from a list of scores.
 * 
 * @param scores List of quiz scores
 * @return Total score
 */
public static BigDecimal calculateTotalScore(List<QuizScore> scores) {
    BigDecimal total = BigDecimal.ZERO;
    for (QuizScore score : scores) {
        total = total.add(score.getPointsEarned());
    }
    return total;
}
}
