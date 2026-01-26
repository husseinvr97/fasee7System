package com.studenttracker.service;

import com.studenttracker.dao.PerformanceIndicatorDAO;
import com.studenttracker.dao.QuizQuestionDAO;
import com.studenttracker.dao.QuizScoreDAO;
import com.studenttracker.model.LessonTopic;
import com.studenttracker.model.PerformanceIndicator;
import com.studenttracker.model.PerformanceTrend;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizQuestion.QuestionType;
import com.studenttracker.model.QuizScore;
import com.studenttracker.service.event.*;
import com.studenttracker.service.impl.PerformanceAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for PerformanceAnalysisService.
 * Tests all 12 test cases from the requirements.
 */
public class PerformanceAnalysisServiceTest {

    private PerformanceAnalysisService service;
    private PerformanceIndicatorDAO piDAO;
    private QuizScoreDAO scoreDAO;
    private QuizQuestionDAO questionDAO;
    private EventBusService eventBus;

    @BeforeEach
    void setUp() {
        piDAO = mock(PerformanceIndicatorDAO.class);
        scoreDAO = mock(QuizScoreDAO.class);
        questionDAO = mock(QuizQuestionDAO.class);
        eventBus = mock(EventBusService.class);
        
        service = new PerformanceAnalysisServiceImpl(piDAO, scoreDAO, questionDAO, eventBus);
    }

    // ========== Test 12.1: Calculate PI - First Quiz ==========
    
    @Test
    @DisplayName("Test 12.1: Calculate PI for first quiz with MCQ and Essay questions")
    void testCalculatePI_FirstQuiz() {
        // Setup quiz questions
        List<QuizQuestion> questions = Arrays.asList(
            createQuestion(1, 30, 1, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0),
            createQuestion(2, 30, 2, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 4.0),
            createQuestion(3, 30, 3, QuestionType.ESSAY, LessonTopic.TopicCategory.NAHW, 5.0),
            createQuestion(4, 30, 4, QuestionType.MCQ, LessonTopic.TopicCategory.ADAB, 3.0),
            createQuestion(5, 30, 5, QuestionType.ESSAY, LessonTopic.TopicCategory.ADAB, 5.0)
        );
        
        // Setup student scores
        List<QuizScore> scores = Arrays.asList(
            createScore(1, 30, 1, 1, 3.0), // Q1: 3/3 correct
            createScore(2, 30, 1, 2, 0.0), // Q2: 0/4 wrong
            createScore(3, 30, 1, 3, 4.0), // Q3: 4/5 essay (0.8 correct, 0.2 wrong)
            createScore(4, 30, 1, 4, 3.0), // Q4: 3/3 correct
            createScore(5, 30, 1, 5, 5.0)  // Q5: 5/5 correct
        );
        
        when(questionDAO.findByQuizId(30)).thenReturn(questions);
        when(scoreDAO.findByQuizAndStudent(30, 1)).thenReturn(scores);
        
        // No previous PI (first quiz)
        when(piDAO.findLatestByStudentAndCategory(1, com.studenttracker.model.LessonTopic.TopicCategory.NAHW)).thenReturn(null);
        when(piDAO.findLatestByStudentAndCategory(1,  LessonTopic.TopicCategory.ADAB)).thenReturn(null);
        
        when(piDAO.insert(any(PerformanceIndicator.class))).thenReturn(1, 2);
        
        // Execute
        service.calculatePerformanceIndicators(30, 1);
        
        // Verify DAO inserts
        ArgumentCaptor<PerformanceIndicator> piCaptor = ArgumentCaptor.forClass(PerformanceIndicator.class);
        verify(piDAO, times(2)).insert(piCaptor.capture());
        
        List<PerformanceIndicator> insertedPIs = piCaptor.getAllValues();
        
        // Verify NAHW PI: 1.8 correct, 1.2 wrong → PI = 1 (rounded), cumulative = 1
        PerformanceIndicator nahwPI = findPIByCategory(insertedPIs,  LessonTopic.TopicCategory.NAHW);
        assertNotNull(nahwPI);
        assertEquals(1, nahwPI.getStudentId());
        assertEquals( LessonTopic.TopicCategory.NAHW, nahwPI.getCategory());
        assertEquals(30, nahwPI.getQuizId());
        assertEquals(2, nahwPI.getCorrectAnswers()); // rounded from 1.8
        assertEquals(1, nahwPI.getWrongAnswers()); // rounded from 1.2
        assertEquals(1, nahwPI.getPiValue()); // 2 - 1 = 1
        assertEquals(1, nahwPI.getCumulativePi());
        
        // Verify ADAB PI: 2 correct, 0 wrong → PI = 2, cumulative = 2
        PerformanceIndicator adabPI = findPIByCategory(insertedPIs,  LessonTopic.TopicCategory.ADAB);
        assertNotNull(adabPI);
        assertEquals(1, adabPI.getStudentId());
        assertEquals( LessonTopic.TopicCategory.ADAB, adabPI.getCategory());
        assertEquals(30, adabPI.getQuizId());
        assertEquals(2, adabPI.getCorrectAnswers());
        assertEquals(0, adabPI.getWrongAnswers());
        assertEquals(2, adabPI.getPiValue());
        assertEquals(2, adabPI.getCumulativePi());
        
        // Verify events
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventBus, times(2)).publish(eventCaptor.capture());
        
        List<Event> events = eventCaptor.getAllValues();
        assertEquals(2, events.size());
        assertTrue(events.stream().allMatch(e -> e instanceof PerformanceIndicatorCalculatedEvent));
        
        // No improvement/degradation events (first quiz)
        assertTrue(events.stream().noneMatch(e -> e instanceof PerformanceImprovementDetectedEvent));
        assertTrue(events.stream().noneMatch(e -> e instanceof PerformanceDegradationDetectedEvent));
    }

    // ========== Test 12.2: Calculate PI - Improvement Detected ==========
    
    @Test
    @DisplayName("Test 12.2: Calculate PI - Improvement detected")
    void testCalculatePI_ImprovementDetected() {
        // Setup: Second quiz with all correct answers
        List<QuizQuestion> questions = Arrays.asList(
            createQuestion(1, 31, 1, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0),
            createQuestion(2, 31, 2, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0),
            createQuestion(3, 31, 3, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0)
        );
        
        List<QuizScore> scores = Arrays.asList(
            createScore(1, 31, 1, 1, 3.0), // All correct
            createScore(2, 31, 1, 2, 3.0),
            createScore(3, 31, 1, 3, 3.0)
        );
        
        when(questionDAO.findByQuizId(31)).thenReturn(questions);
        when(scoreDAO.findByQuizAndStudent(31, 1)).thenReturn(scores);
        
        // Previous PI for NAHW
        PerformanceIndicator previousPI = new PerformanceIndicator(
            1,  LessonTopic.TopicCategory.NAHW, 30, 2, 1, 1, 1, LocalDateTime.now()
        );
        when(piDAO.findLatestByStudentAndCategory(1,  LessonTopic.TopicCategory.NAHW)).thenReturn(previousPI);
        when(piDAO.insert(any(PerformanceIndicator.class))).thenReturn(3);
        
        // Execute
        service.calculatePerformanceIndicators(31, 1);
        
        // Verify PI inserted
        ArgumentCaptor<PerformanceIndicator> piCaptor = ArgumentCaptor.forClass(PerformanceIndicator.class);
        verify(piDAO).insert(piCaptor.capture());
        
        PerformanceIndicator newPI = piCaptor.getValue();
        assertEquals(3, newPI.getCorrectAnswers());
        assertEquals(0, newPI.getWrongAnswers());
        assertEquals(3, newPI.getPiValue());
        assertEquals(4, newPI.getCumulativePi()); // 1 + 3 = 4
        
        // Verify events
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventBus, times(2)).publish(eventCaptor.capture());
        
        List<Event> events = eventCaptor.getAllValues();
        
        // Should have PerformanceIndicatorCalculatedEvent and PerformanceImprovementDetectedEvent
        assertTrue(events.stream().anyMatch(e -> e instanceof PerformanceIndicatorCalculatedEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof PerformanceImprovementDetectedEvent));
        
        PerformanceImprovementDetectedEvent improvementEvent = (PerformanceImprovementDetectedEvent)
            events.stream().filter(e -> e instanceof PerformanceImprovementDetectedEvent).findFirst().get();
        
        assertEquals(1, improvementEvent.getStudentId());
        assertEquals( LessonTopic.TopicCategory.NAHW, improvementEvent.getCategory());
        assertEquals(1, improvementEvent.getPreviousPi());
        assertEquals(3, improvementEvent.getCurrentPi());
        assertEquals(2, improvementEvent.getImprovementAmount());
    }

    // ========== Test 12.3: Calculate PI - Degradation Detected ==========
    
    @Test
    @DisplayName("Test 12.3: Calculate PI - Degradation detected")
    void testCalculatePI_DegradationDetected() {
        // Setup: Quiz with mostly wrong answers
        List<QuizQuestion> questions = Arrays.asList(
            createQuestion(1, 32, 1, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0),
            createQuestion(2, 32, 2, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0),
            createQuestion(3, 32, 3, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0),
            createQuestion(4, 32, 4, QuestionType.MCQ, LessonTopic.TopicCategory.NAHW, 3.0)
        );
        
        List<QuizScore> scores = Arrays.asList(
            createScore(1, 32, 1, 1, 3.0),  // 1 correct
            createScore(2, 32, 1, 2, 0.0),  // 3 wrong
            createScore(3, 32, 1, 3, 0.0),
            createScore(4, 32, 1, 4, 0.0)
        );
        
        when(questionDAO.findByQuizId(32)).thenReturn(questions);
        when(scoreDAO.findByQuizAndStudent(32, 1)).thenReturn(scores);
        
        // Previous cumulative PI = 4
        PerformanceIndicator previousPI = new PerformanceIndicator(
            1,  LessonTopic.TopicCategory.NAHW, 31, 3, 0, 3, 4, LocalDateTime.now()
        );
        when(piDAO.findLatestByStudentAndCategory(1,  LessonTopic.TopicCategory.NAHW)).thenReturn(previousPI);
        when(piDAO.insert(any(PerformanceIndicator.class))).thenReturn(5);
        
        // Execute
        service.calculatePerformanceIndicators(32, 1);
        
        // Verify PI
        ArgumentCaptor<PerformanceIndicator> piCaptor = ArgumentCaptor.forClass(PerformanceIndicator.class);
        verify(piDAO).insert(piCaptor.capture());
        
        PerformanceIndicator newPI = piCaptor.getValue();
        assertEquals(1, newPI.getCorrectAnswers());
        assertEquals(3, newPI.getWrongAnswers());
        assertEquals(-2, newPI.getPiValue()); // 1 - 3 = -2
        assertEquals(2, newPI.getCumulativePi()); // 4 + (-2) = 2
        
        // Verify degradation event
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventBus, times(2)).publish(eventCaptor.capture());
        
        List<Event> events = eventCaptor.getAllValues();
        assertTrue(events.stream().anyMatch(e -> e instanceof PerformanceDegradationDetectedEvent));
        
        PerformanceDegradationDetectedEvent degradationEvent = (PerformanceDegradationDetectedEvent)
            events.stream().filter(e -> e instanceof PerformanceDegradationDetectedEvent).findFirst().get();
        
        assertEquals(1, degradationEvent.getStudentId());
        assertEquals( LessonTopic.TopicCategory.NAHW, degradationEvent.getCategory());
        assertEquals(3, degradationEvent.getPreviousPi());
        assertEquals(-2, degradationEvent.getCurrentPi());
        assertEquals(5, degradationEvent.getDegradationAmount());
    }

    // ========== Test 12.4: Get Current Cumulative PI ==========
    
    @Test
    @DisplayName("Test 12.4: Get current cumulative PI")
    void testGetCurrentCumulativePI() {
        PerformanceIndicator latestPI = new PerformanceIndicator(
            1,  LessonTopic.TopicCategory.NAHW, 33, 2, 1, 1, 2, LocalDateTime.now()
        );
        
        when(piDAO.findLatestByStudentAndCategory(1,  LessonTopic.TopicCategory.NAHW)).thenReturn(latestPI);
        
        int cumulativePI = service.getCurrentCumulativePI(1,  LessonTopic.TopicCategory.NAHW);
        
        assertEquals(2, cumulativePI);
        verify(piDAO).findLatestByStudentAndCategory(1,  LessonTopic.TopicCategory.NAHW);
    }

    // ========== Test 12.5: Get Current Cumulative PI - No Data ==========
    
    @Test
    @DisplayName("Test 12.5: Get current cumulative PI - No data returns 0")
    void testGetCurrentCumulativePI_NoData() {
        when(piDAO.findLatestByStudentAndCategory(200,  LessonTopic.TopicCategory.NAHW)).thenReturn(null);
        
        int cumulativePI = service.getCurrentCumulativePI(200,  LessonTopic.TopicCategory.NAHW);
        
        assertEquals(0, cumulativePI);
    }

    // ========== Test 12.6: Calculate Overall PI ==========
    
    @Test
    @DisplayName("Test 12.6: Calculate overall PI across all categories")
    void testCalculateOverallPI() {
        Map< LessonTopic.TopicCategory, Integer> categoryPIs = new HashMap<>();
        categoryPIs.put( LessonTopic.TopicCategory.NAHW, 2);
        categoryPIs.put( LessonTopic.TopicCategory.ADAB, 5);
        categoryPIs.put( LessonTopic.TopicCategory.QISSA, 3);
        categoryPIs.put( LessonTopic.TopicCategory.TABEER, 0);
        categoryPIs.put( LessonTopic.TopicCategory.NUSUS, 7);
        categoryPIs.put( LessonTopic.TopicCategory.QIRAA, 2);
        
        when(piDAO.getCurrentPIsByStudent(1)).thenReturn(categoryPIs);
        
        int overallPI = service.calculateOverallPI(1);
        
        assertEquals(19, overallPI); // 2+5+3+0+7+2 = 19
    }

    // ========== Test 12.7: Get Performance Trend - Improving ==========
    
    @Test
    @DisplayName("Test 12.7: Get performance trend - Improving")
    void testGetPerformanceTrend_Improving() {
        List<PerformanceIndicator> piHistory = Arrays.asList(
            createPI(1,  LessonTopic.TopicCategory.NAHW, 30, 1, 1),
            createPI(1,  LessonTopic.TopicCategory.NAHW, 31, 3, 4),
            createPI(1,  LessonTopic.TopicCategory.NAHW, 32, 2, 6),
            createPI(1,  LessonTopic.TopicCategory.NAHW, 33, 3, 9)
        );
        
        when(piDAO.findByStudentAndCategory(1,  LessonTopic.TopicCategory.NAHW)).thenReturn(piHistory);
        
        PerformanceTrend trend = service.getPerformanceTrend(1,  LessonTopic.TopicCategory.NAHW);
        
        assertEquals(PerformanceTrend.IMPROVING, trend);
    }

    // ========== Test 12.8: Get Performance Trend - Degrading ==========
    
    @Test
    @DisplayName("Test 12.8: Get performance trend - Degrading")
    void testGetPerformanceTrend_Degrading() {
        List<PerformanceIndicator> piHistory = Arrays.asList(
            createPI(1,  LessonTopic.TopicCategory.ADAB, 30, 4, 8),
            createPI(1,  LessonTopic.TopicCategory.ADAB, 31, 2, 6),
            createPI(1,  LessonTopic.TopicCategory.ADAB, 32, 0, 4),
            createPI(1,  LessonTopic.TopicCategory.ADAB, 33, -2, 2)
        );
        
        when(piDAO.findByStudentAndCategory(1,  LessonTopic.TopicCategory.ADAB)).thenReturn(piHistory);
        
        PerformanceTrend trend = service.getPerformanceTrend(1,  LessonTopic.TopicCategory.ADAB);
        
        assertEquals(PerformanceTrend.DEGRADING, trend);
    }

    // ========== Test 12.9: Get Performance Trend - Stable ==========
    
    @Test
    @DisplayName("Test 12.9: Get performance trend - Stable")
    void testGetPerformanceTrend_Stable() {
        List<PerformanceIndicator> piHistory = Arrays.asList(
            createPI(1,  LessonTopic.TopicCategory.QISSA, 30, 0, 5),
            createPI(1,  LessonTopic.TopicCategory.QISSA, 31, 1, 6),
            createPI(1,  LessonTopic.TopicCategory.QISSA, 32, -1, 5),
            createPI(1,  LessonTopic.TopicCategory.QISSA, 33, 0, 5)
        );
        
        when(piDAO.findByStudentAndCategory(1,  LessonTopic.TopicCategory.QISSA)).thenReturn(piHistory);
        
        PerformanceTrend trend = service.getPerformanceTrend(1,  LessonTopic.TopicCategory.QISSA);
        
        assertEquals(PerformanceTrend.STABLE, trend);
    }

    // ========== Test 12.10: Get Weak Categories ==========
    
    @Test
    @DisplayName("Test 12.10: Get weak categories (below average)")
    void testGetWeakCategories() {
        Map< LessonTopic.TopicCategory, Integer> categoryPIs = new HashMap<>();
        categoryPIs.put( LessonTopic.TopicCategory.NAHW, 12);
        categoryPIs.put( LessonTopic.TopicCategory.ADAB, 5);
        categoryPIs.put( LessonTopic.TopicCategory.QISSA, 3);
        categoryPIs.put( LessonTopic.TopicCategory.TABEER, 0);
        categoryPIs.put( LessonTopic.TopicCategory.NUSUS, 7);
        categoryPIs.put( LessonTopic.TopicCategory.QIRAA, 2);
        
        when(piDAO.getCurrentPIsByStudent(1)).thenReturn(categoryPIs);
        
        List< LessonTopic.TopicCategory> weakCategories = service.getWeakCategories(1);
        
        // Average = (12+5+3+0+7+2)/6 = 4.83
        // Below average: QISSA(3), TABEER(0), QIRAA(2)
        assertEquals(3, weakCategories.size());
        assertTrue(weakCategories.contains( LessonTopic.TopicCategory.QISSA));
        assertTrue(weakCategories.contains( LessonTopic.TopicCategory.TABEER));
        assertTrue(weakCategories.contains( LessonTopic.TopicCategory.QIRAA));
    }

    // ========== Test 12.11: Get PI Progression by Category ==========
    
    @Test
    @DisplayName("Test 12.11: Get PI progression by category for charting")
    void testGetPIProgressionByCategory() {
        List<PerformanceIndicator> piHistory = Arrays.asList(
            createPI(1,  LessonTopic.TopicCategory.NAHW, 30, 1, 1),
            createPI(1,  LessonTopic.TopicCategory.NAHW, 31, 3, 4),
            createPI(1,  LessonTopic.TopicCategory.NAHW, 32, -2, 2),
            createPI(1,  LessonTopic.TopicCategory.NAHW, 33, 3, 5)
        );
        
        when(piDAO.findByStudentAndCategory(1,  LessonTopic.TopicCategory.NAHW)).thenReturn(piHistory);
        
        Map<Integer, Integer> progression = service.getPIProgressionByCategory(1,  LessonTopic.TopicCategory.NAHW);
        
        assertEquals(4, progression.size());
        assertEquals(1, progression.get(30));
        assertEquals(4, progression.get(31));
        assertEquals(2, progression.get(32));
        assertEquals(5, progression.get(33));
    }

    // ========== Test 12.12: Recalculate All PIs ==========
    
    @Test
    @DisplayName("Test 12.12: Recalculate all PIs after score correction")
    void testRecalculateAllPIs() {
        // Student has taken 3 quizzes
        List<QuizScore> allScores = Arrays.asList(
            createScore(1, 30, 1, 1, 3.0),
            createScore(2, 30, 1, 2, 4.0),
            createScore(3, 31, 1, 3, 5.0),
            createScore(4, 32, 1, 4, 2.0)
        );
        
        when(scoreDAO.findByStudentId(1)).thenReturn(allScores);
        
        // Existing PIs
        List<PerformanceIndicator> existingPIs = Arrays.asList(
            createPI(1,  LessonTopic.TopicCategory.NAHW, 30, 2, 2),
            createPI(1,  LessonTopic.TopicCategory.ADAB, 31, 1, 1),
            createPI(1,  LessonTopic.TopicCategory.QISSA, 32, 1, 1)
        );
        existingPIs.get(0).setPiId(1);
        existingPIs.get(1).setPiId(2);
        existingPIs.get(2).setPiId(3);
        
        when(piDAO.findByStudentId(1)).thenReturn(existingPIs);
        
        // Mock questions and scores for each quiz
        when(questionDAO.findByQuizId(anyInt())).thenReturn(new ArrayList<>());
        when(scoreDAO.findByQuizAndStudent(anyInt(), eq(1))).thenReturn(new ArrayList<>());
        
        // Execute
        service.recalculateAllPIs(1);
        
        // Verify all existing PIs were deleted
        verify(piDAO).delete(1);
        verify(piDAO).delete(2);
        verify(piDAO).delete(3);
        
        // Verify recalculation for each quiz (in order: 30, 31, 32)
        verify(questionDAO).findByQuizId(30);
        verify(questionDAO).findByQuizId(31);
        verify(questionDAO).findByQuizId(32);
    }

    // ========== Helper Methods ==========
    
    private QuizQuestion createQuestion(Integer id, Integer quizId, int number, 
                                       QuestionType type, LessonTopic.TopicCategory category, 
                                       Double points) {
        QuizQuestion q = new QuizQuestion(quizId, number, type, category, points, null);
        q.setQuestionId(id);
        return q;
    }
    
    private QuizScore createScore(Integer id, Integer quizId, Integer studentId, 
                                 Integer questionId, Double pointsEarned) {
        QuizScore s = new QuizScore(quizId, studentId, questionId, pointsEarned, 
                                   LocalDateTime.now(), 1);
        s.setScoreId(id);
        return s;
    }
    
    private PerformanceIndicator createPI(Integer studentId,  LessonTopic.TopicCategory category, 
                                         Integer quizId, int piValue, int cumulativePi) {
        int correct = Math.max(0, piValue);
        int wrong = Math.max(0, -piValue);
        return new PerformanceIndicator(studentId, category, quizId, correct, wrong, 
                                       piValue, cumulativePi, LocalDateTime.now());
    }
    
    private PerformanceIndicator findPIByCategory(List<PerformanceIndicator> pis, 
                                                  LessonTopic.TopicCategory category) {
        return pis.stream()
            .filter(pi -> pi.getCategory() == category)
            .findFirst()
            .orElse(null);
    }
}