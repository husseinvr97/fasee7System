package com.studenttracker.service.impl;

import com.studenttracker.dao.PerformanceIndicatorDAO;
import com.studenttracker.dao.QuizScoreDAO;
import com.studenttracker.dao.QuizQuestionDAO;
import com.studenttracker.model.PerformanceIndicator;
import com.studenttracker.model.PerformanceTrend;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizScore;
import com.studenttracker.model.LessonTopic;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.PerformanceAnalysisService;
import com.studenttracker.service.event.PerformanceIndicatorCalculatedEvent;
import com.studenttracker.service.event.PerformanceDegradationDetectedEvent;
import com.studenttracker.service.event.PerformanceImprovementDetectedEvent;
import com.studenttracker.service.impl.helpers.PerformanceAnalysisServiceImplHelpers;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PerformanceAnalysisService.
 * Handles PI calculation, tracking, and analysis.
 * 
 * NOTE: QuizQuestion uses LessonTopic.TopicCategory
 *       PerformanceIndicator uses PerformanceIndicator.TopicCategory
 *       Conversion happens during PI calculation.
 */
public class PerformanceAnalysisServiceImpl implements PerformanceAnalysisService {
    
    private final PerformanceIndicatorDAO performanceIndicatorDAO;
    private final QuizScoreDAO quizScoreDAO;
    private final QuizQuestionDAO quizQuestionDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     */
    public PerformanceAnalysisServiceImpl(PerformanceIndicatorDAO performanceIndicatorDAO,
                                         QuizScoreDAO quizScoreDAO,
                                         QuizQuestionDAO quizQuestionDAO,
                                         EventBusService eventBusService) {
        this.performanceIndicatorDAO = performanceIndicatorDAO;
        this.quizScoreDAO = quizScoreDAO;
        this.quizQuestionDAO = quizQuestionDAO;
        this.eventBusService = eventBusService;
    }
    
    
    // ========== Calculate PI ==========
    
    @Override
    public void calculatePerformanceIndicators(Integer quizId, Integer studentId) {
        // Step 1: Get quiz questions (uses LessonTopic.TopicCategory)
        List<QuizQuestion> questions = quizQuestionDAO.findByQuizId(quizId);
        if (questions.isEmpty()) {
            return; // No questions, nothing to calculate
        }
        
        // Step 2: Get student's scores for this quiz
        List<QuizScore> scores = quizScoreDAO.findByQuizAndStudent(quizId, studentId);
        if (scores.isEmpty()) {
            return; // No scores, nothing to calculate
        }
        
        // Step 3: Calculate correct/wrong counts per category (returns LessonTopic.TopicCategory)
        Map<LessonTopic.TopicCategory, double[]> categoryCounts = 
            PerformanceAnalysisServiceImplHelpers.calculateCategoryCorrectWrong(scores, questions);
        
        // Step 4: For each category, calculate and save PI
        for (Map.Entry<LessonTopic.TopicCategory, double[]> entry : categoryCounts.entrySet()) {
            LessonTopic.TopicCategory lessonCategory = entry.getKey();
            
            // Convert LessonTopic.TopicCategory to PerformanceIndicator.TopicCategory
            PerformanceIndicator.TopicCategory piCategory = 
                PerformanceAnalysisServiceImplHelpers.convertToPerformanceCategory(lessonCategory);
            
            if (piCategory == null) {
                continue; // Skip if conversion fails
            }
            
            double[] counts = entry.getValue();
            
            int correctAnswers = (int) Math.round(counts[0]);
            int wrongAnswers = (int) Math.round(counts[1]);
            int currentPiValue = correctAnswers - wrongAnswers;
            
            // Step 5: Get previous cumulative PI for this category (uses PerformanceIndicator.TopicCategory)
            PerformanceIndicator previousPI = 
                performanceIndicatorDAO.findLatestByStudentAndCategory(studentId, piCategory);
            
            int previousCumulativePi = (previousPI != null) ? previousPI.getCumulativePi() : 0;
            int newCumulativePi = previousCumulativePi + currentPiValue;
            
            // Step 6: Create and save PerformanceIndicator (uses PerformanceIndicator.TopicCategory)
            PerformanceIndicator pi = new PerformanceIndicator(
                studentId,
                piCategory,
                quizId,
                correctAnswers,
                wrongAnswers,
                currentPiValue,
                newCumulativePi,
                LocalDateTime.now()
            );
            
            Integer piId = performanceIndicatorDAO.insert(pi);
            pi.setPiId(piId);
            
            // Step 7: Publish PerformanceIndicatorCalculatedEvent (uses PerformanceIndicator.TopicCategory)
            PerformanceIndicatorCalculatedEvent calculatedEvent = 
                new PerformanceIndicatorCalculatedEvent(
                    studentId,
                    piCategory,
                    quizId,
                    currentPiValue,
                    newCumulativePi,
                    pi.getCalculatedAt()
                );
            eventBusService.publish(calculatedEvent);
            
            // Step 8: Compare with previous PI and publish appropriate events
            if (previousPI != null) {
                int previousPiValue = previousPI.getPiValue();
                
                if (currentPiValue < previousPiValue) {
                    // Degradation detected
                    int degradationAmount = previousPiValue - currentPiValue;
                    PerformanceDegradationDetectedEvent degradationEvent = 
                        new PerformanceDegradationDetectedEvent(
                            studentId,
                            piCategory,
                            previousPiValue,
                            currentPiValue,
                            degradationAmount
                        );
                    eventBusService.publish(degradationEvent);
                    
                } else if (currentPiValue > previousPiValue) {
                    // Improvement detected
                    int improvementAmount = currentPiValue - previousPiValue;
                    PerformanceImprovementDetectedEvent improvementEvent = 
                        new PerformanceImprovementDetectedEvent(
                            studentId,
                            piCategory,
                            previousPiValue,
                            currentPiValue,
                            improvementAmount
                        );
                    eventBusService.publish(improvementEvent);
                }
            }
        }
    }
    
    @Override
    public void recalculateAllPIs(Integer studentId) {
        // Step 1: Get all quizzes student has taken (distinct quiz IDs from scores)
        List<QuizScore> allScores = quizScoreDAO.findByStudentId(studentId);
        
        Set<Integer> quizIds = allScores.stream()
            .map(QuizScore::getQuizId)
            .collect(Collectors.toSet());
        
        // Sort quiz IDs chronologically (assuming quiz_id represents chronological order)
        // In real scenario, you'd fetch quiz dates and sort by them
        List<Integer> sortedQuizIds = new ArrayList<>(quizIds);
        Collections.sort(sortedQuizIds);
        
        // Step 2: Delete all existing PIs for this student
        List<PerformanceIndicator> existingPIs = performanceIndicatorDAO.findByStudentId(studentId);
        for (PerformanceIndicator pi : existingPIs) {
            performanceIndicatorDAO.delete(pi.getPiId());
        }
        
        // Step 3: Recalculate PIs for all quizzes in chronological order
        for (Integer quizId : sortedQuizIds) {
            calculatePerformanceIndicators(quizId, studentId);
        }
    }
    
    
    // ========== Retrieval (All use PerformanceIndicator.TopicCategory) ==========
    
    @Override
    public List<PerformanceIndicator> getStudentPIHistory(Integer studentId) {
        return performanceIndicatorDAO.findByStudentId(studentId);
    }
    
    @Override
    public List<PerformanceIndicator> getStudentPIByCategory(Integer studentId, 
                                                             PerformanceIndicator.TopicCategory category) {
        return performanceIndicatorDAO.findByStudentAndCategory(studentId, category);
    }
    
    @Override
    public PerformanceIndicator getLatestPI(Integer studentId, 
                                           PerformanceIndicator.TopicCategory category) {
        return performanceIndicatorDAO.findLatestByStudentAndCategory(studentId, category);
    }
    
    @Override
    public int getCurrentCumulativePI(Integer studentId, 
                                     PerformanceIndicator.TopicCategory category) {
        PerformanceIndicator latest = getLatestPI(studentId, category);
        return (latest != null) ? latest.getCumulativePi() : 0;
    }
    
    @Override
    public Map<PerformanceIndicator.TopicCategory, Integer> getAllCategoryPIs(Integer studentId) {
        return performanceIndicatorDAO.getCurrentPIsByStudent(studentId);
    }
    
    
    // ========== Analysis (All use PerformanceIndicator.TopicCategory) ==========
    
    @Override
    public PerformanceTrend getPerformanceTrend(Integer studentId, 
                                               PerformanceIndicator.TopicCategory category) {
        List<PerformanceIndicator> piHistory = getStudentPIByCategory(studentId, category);
        return PerformanceAnalysisServiceImplHelpers.analyzeTrend(piHistory);
    }
    
    @Override
    public int calculateOverallPI(Integer studentId) {
        Map<PerformanceIndicator.TopicCategory, Integer> categoryPIs = getAllCategoryPIs(studentId);
        
        int total = 0;
        for (Integer pi : categoryPIs.values()) {
            total += pi;
        }
        
        return total;
    }
    
    @Override
    public List<PerformanceIndicator.TopicCategory> getWeakCategories(Integer studentId) {
        Map<PerformanceIndicator.TopicCategory, Integer> categoryPIs = getAllCategoryPIs(studentId);
        
        if (categoryPIs.isEmpty()) {
            return new ArrayList<>();
        }
        
        double average = PerformanceAnalysisServiceImplHelpers.calculateAveragePI(categoryPIs);
        
        List<PerformanceIndicator.TopicCategory> weakCategories = new ArrayList<>();
        for (Map.Entry<PerformanceIndicator.TopicCategory, Integer> entry : categoryPIs.entrySet()) {
            if (entry.getValue() < average) {
                weakCategories.add(entry.getKey());
            }
        }
        
        return weakCategories;
    }
    
    @Override
    public List<PerformanceIndicator.TopicCategory> getStrongCategories(Integer studentId) {
        Map<PerformanceIndicator.TopicCategory, Integer> categoryPIs = getAllCategoryPIs(studentId);
        
        if (categoryPIs.isEmpty()) {
            return new ArrayList<>();
        }
        
        double average = PerformanceAnalysisServiceImplHelpers.calculateAveragePI(categoryPIs);
        
        List<PerformanceIndicator.TopicCategory> strongCategories = new ArrayList<>();
        for (Map.Entry<PerformanceIndicator.TopicCategory, Integer> entry : categoryPIs.entrySet()) {
            if (entry.getValue() > average) {
                strongCategories.add(entry.getKey());
            }
        }
        
        return strongCategories;
    }
    
    
    // ========== Charts/UI Data (All use PerformanceIndicator.TopicCategory) ==========
    
    @Override
    public Map<Integer, Integer> getPIProgressionByCategory(Integer studentId, 
                                                            PerformanceIndicator.TopicCategory category) {
        List<PerformanceIndicator> piHistory = getStudentPIByCategory(studentId, category);
        
        Map<Integer, Integer> progression = new LinkedHashMap<>();
        for (PerformanceIndicator pi : piHistory) {
            progression.put(pi.getQuizId(), pi.getCumulativePi());
        }
        
        return progression;
    }
    
    @Override
    public Map<Integer, Integer> getOverallPIProgression(Integer studentId) {
        List<PerformanceIndicator> allPIs = getStudentPIHistory(studentId);
        
        // Group by quiz ID and sum cumulative PIs
        Map<Integer, Integer> quizTotals = new LinkedHashMap<>();
        
        for (PerformanceIndicator pi : allPIs) {
            Integer quizId = pi.getQuizId();
            quizTotals.put(quizId, quizTotals.getOrDefault(quizId, 0) + pi.getCumulativePi());
        }
        
        return quizTotals;
    }
}