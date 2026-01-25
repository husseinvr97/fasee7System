package com.studenttracker.service.impl.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.LessonTopic;
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
    Map<TopicCategory, Double> earnedByCategory = new HashMap<>();
    Map<TopicCategory, Double> totalByCategory = new HashMap<>();
    
    for (QuizScore score : scores) {
        QuizQuestion question = questionMap.get(score.getQuestionId());
        if (question != null) {
            TopicCategory category = question.getCategory();
            
            // Add earned points
            Double currentEarned = earnedByCategory.getOrDefault(category, 0.0);
            earnedByCategory.put(category, currentEarned + score.getPointsEarned());
            
            // Add total points
            Double currentTotal = totalByCategory.getOrDefault(category, 0.0);
            totalByCategory.put(category, currentTotal + question.getPoints());
        }
    }
    
    // Create QuizCategoryTotal objects
    List<QuizCategoryTotal> categoryTotals = new ArrayList<>();
    for (TopicCategory category : earnedByCategory.keySet()) {
        Double earned = earnedByCategory.get(category);
        Double total = totalByCategory.get(category);
        
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
private static LessonTopic.TopicCategory convertToQuizCategoryTotalEnum(TopicCategory lessonCategory) {
    // Assuming QuizCategoryTotal.TopicCategory should match LessonTopic.TopicCategory
    // If not, you need to update QuizCategoryTotal to use LessonTopic.TopicCategory
    return LessonTopic.TopicCategory.valueOf(lessonCategory.name());
}

/**
 * Calculates total score from a list of scores.
 * 
 * @param scores List of quiz scores
 * @return Total score
 */
public static Double calculateTotalScore(List<QuizScore> scores) {
    Double total = 0.0;
    for (QuizScore score : scores) {
        total += score.getPointsEarned();
    }
    return total;
}
}
