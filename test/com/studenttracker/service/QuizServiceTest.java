package com.studenttracker.service;

import com.google.common.eventbus.Subscribe;
import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.QuizCategoryTotalDAO;
import com.studenttracker.dao.QuizDAO;
import com.studenttracker.dao.QuizQuestionDAO;
import com.studenttracker.dao.QuizScoreDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.UnauthorizedException;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.Quiz;
import com.studenttracker.model.QuizCategoryTotal;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizQuestion.QuestionType;
import com.studenttracker.model.QuizScore;
import com.studenttracker.model.User;
import com.studenttracker.model.User.UserRole;
import com.studenttracker.service.event.QuizCreatedEvent;
import com.studenttracker.service.event.QuizGradedEvent;
import com.studenttracker.service.event.QuizGradingCompletedEvent;
import com.studenttracker.service.impl.QuizServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("QuizService Tests")
public class QuizServiceTest {

    private QuizService quizService;
    private QuizDAO quizDAO;
    private QuizQuestionDAO quizQuestionDAO;
    private QuizScoreDAO quizScoreDAO;
    private QuizCategoryTotalDAO quizCategoryTotalDAO;
    private AttendanceDAO attendanceDAO;
    private UserDAO userDAO;
    private EventBusService eventBusService;

    @BeforeEach
    void setUp() {
        // Create mocks
        quizDAO = mock(QuizDAO.class);
        quizQuestionDAO = mock(QuizQuestionDAO.class);
        quizScoreDAO = mock(QuizScoreDAO.class);
        quizCategoryTotalDAO = mock(QuizCategoryTotalDAO.class);
        attendanceDAO = mock(AttendanceDAO.class);
        userDAO = mock(UserDAO.class);

        // Create service instance
        quizService = new QuizServiceImpl(
            quizDAO, 
            quizQuestionDAO, 
            quizScoreDAO, 
            quizCategoryTotalDAO, 
            attendanceDAO, 
            userDAO
        );

        // Get singleton EventBusService and register listener
        eventBusService = EventBusService.getInstance();
        eventListener = new TestEventListener();
        eventBusService.register(eventListener);
    }

    @AfterEach
    void tearDown() {
        // Unregister event listener
        if (eventListener != null) {
            eventBusService.unregister(eventListener);
        }
    }

    // ========== Test 7.1: Create Quiz - Valid Quiz with Questions ==========
    @Test
    @DisplayName("Test 7.1: Create Quiz - Valid Quiz with Questions")
    void testCreateQuiz_ValidQuizWithQuestions() {
        // Arrange
        Integer lessonId = 50;
        byte[] pdfData = "PDF_DATA".getBytes();
        Integer createdBy = 1;

        // Create 5 questions
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(null, 1, QuestionType.MCQ, TopicCategory.NAHW, 3.0, "C"));
        questions.add(new QuizQuestion(null, 2, QuestionType.MCQ, TopicCategory.NAHW, 4.0, "A"));
        questions.add(new QuizQuestion(null, 3, QuestionType.ESSAY, TopicCategory.NAHW, 5.0, null));
        questions.add(new QuizQuestion(null, 4, QuestionType.MCQ, TopicCategory.ADAB, 3.0, "B"));
        questions.add(new QuizQuestion(null, 5, QuestionType.ESSAY, TopicCategory.ADAB, 5.0, null));

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Mock quiz insertion
        when(quizDAO.insert(any(Quiz.class))).thenReturn(30);
        when(quizQuestionDAO.bulkInsert(anyList())).thenReturn(true);

        // Act
        Integer quizId = quizService.createQuiz(lessonId, pdfData, questions, createdBy);

        // Assert
        assertNotNull(quizId);
        assertEquals(30, quizId);

        // Verify admin validation
        verify(userDAO, times(1)).findById(1);

        // Verify quiz insertion with correct total marks (3+4+5+3+5 = 20)
        ArgumentCaptor<Quiz> quizCaptor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizDAO, times(1)).insert(quizCaptor.capture());
        Quiz capturedQuiz = quizCaptor.getValue();
        assertEquals(20.0, capturedQuiz.getTotalMarks());
        assertEquals(lessonId, capturedQuiz.getLessonId());

        // Verify questions insertion
        ArgumentCaptor<List<QuizQuestion>> questionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(quizQuestionDAO, times(1)).bulkInsert(questionsCaptor.capture());
        List<QuizQuestion> capturedQuestions = questionsCaptor.getValue();
        assertEquals(5, capturedQuestions.size());
        
        // Verify all questions have quizId set
        for (QuizQuestion q : capturedQuestions) {
            assertEquals(30, q.getQuizId());
        }

        // Verify all questions have quizId set
        for (QuizQuestion q : capturedQuestions) {
            assertEquals(30, q.getQuizId());
        }

        // Verify QuizCreatedEvent published
        QuizCreatedEvent createdEvent = eventListener.getEventOfType(QuizCreatedEvent.class);
        assertNotNull(createdEvent, "QuizCreatedEvent should be published");
        assertEquals(30, createdEvent.getQuizId());
        assertEquals(50, createdEvent.getLessonId());
        assertEquals(20.0, createdEvent.getTotalMarks());
        assertEquals(5, createdEvent.getQuestionCount());
    }

    // ========== Test 7.2: Create Quiz - No Questions ==========
    @Test
    @DisplayName("Test 7.2: Create Quiz - No Questions")
    void testCreateQuiz_NoQuestions() {
        // Arrange
        Integer lessonId = 50;
        byte[] pdfData = "PDF_DATA".getBytes();
        List<QuizQuestion> questions = new ArrayList<>(); // Empty list
        Integer createdBy = 1;

        // Mock admin user
        User adminUser = new User("admin", "hash", "Admin User", UserRole.ADMIN);
        adminUser.setUserId(1);
        when(userDAO.findById(1)).thenReturn(adminUser);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            quizService.createQuiz(lessonId, pdfData, questions, createdBy);
        });

        assertEquals("At least one question is required", exception.getMessage());
        
        // Verify no quiz insertion
        verify(quizDAO, never()).insert(any(Quiz.class));
    }

    // ========== Test 7.3: Create Quiz - Non-Admin ==========
    @Test
    @DisplayName("Test 7.3: Create Quiz - Non-Admin")
    void testCreateQuiz_NonAdmin() {
        // Arrange
        Integer lessonId = 50;
        byte[] pdfData = "PDF_DATA".getBytes();
        List<QuizQuestion> questions = createSampleQuestions();
        Integer createdBy = 5;

        // Mock assistant user
        User assistantUser = new User("assistant", "hash", "Assistant User", UserRole.ASSISTANT);
        assistantUser.setUserId(5);
        when(userDAO.findById(5)).thenReturn(assistantUser);

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            quizService.createQuiz(lessonId, pdfData, questions, createdBy);
        });

        assertEquals("Only administrators can perform this action", exception.getMessage());
        
        // Verify no quiz insertion
        verify(quizDAO, never()).insert(any(Quiz.class));
    }

    // ========== Test 7.4: Grade Student - MCQ Auto-Grading ==========
    @Test
    @DisplayName("Test 7.4: Grade Student - MCQ Auto-Grading")
    void testGradeStudent_MCQAutoGrading() {
        // Arrange
        Integer quizId = 30;
        Integer studentId = 1;
        Integer gradedBy = 2;

        // Mock quiz
        Quiz quiz = new Quiz(50, "PDF".getBytes(), 20.0, 1);
        quiz.setQuizId(30);
        when(quizDAO.findById(30)).thenReturn(quiz);

        // Mock attendance - student present
        Attendance attendance = new Attendance(50, 1, AttendanceStatus.PRESENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 1)).thenReturn(attendance);

        // Mock not first lesson (2 attendances)
        when(attendanceDAO.countByStudentAndStatus(1, AttendanceStatus.PRESENT)).thenReturn(2);

        // Mock quiz questions
        List<QuizQuestion> questions = createSampleQuestions();
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setQuestionId(100 + i);
        }
        when(quizQuestionDAO.findByQuizId(30)).thenReturn(questions);

        // Create scores with student answers
        List<QuizScore> scores = new ArrayList<>();
        scores.add(new QuizScore(null, null, 100, 3.0, null, null)); // Q1: Correct (C==C)
        scores.add(new QuizScore(null, null, 101, 0.0, null, null)); // Q2: Wrong (B!=A)
        scores.add(new QuizScore(null, null, 102, 4.0, null, null)); // Q3: Essay manually graded
        scores.add(new QuizScore(null, null, 103, 3.0, null, null)); // Q4: Correct (B==B)
        scores.add(new QuizScore(null, null, 104, 5.0, null, null)); // Q5: Essay manually graded

        // Mock insertions
        when(quizScoreDAO.bulkInsert(anyList())).thenReturn(true);
        when(quizCategoryTotalDAO.bulkInsert(anyList())).thenReturn(true);

        // Act
        boolean result = quizService.gradeStudent(quizId, studentId, scores, gradedBy);

        // Assert
        assertTrue(result);

        // Verify attendance check
        verify(attendanceDAO, times(1)).findByLessonAndStudent(50, 1);

        // Verify not first lesson check
        verify(attendanceDAO, times(1)).countByStudentAndStatus(1, AttendanceStatus.PRESENT);

        // Verify scores insertion
        ArgumentCaptor<List<QuizScore>> scoresCaptor = ArgumentCaptor.forClass(List.class);
        verify(quizScoreDAO, times(1)).bulkInsert(scoresCaptor.capture());
        List<QuizScore> capturedScores = scoresCaptor.getValue();
        assertEquals(5, capturedScores.size());

        // Verify category totals insertion
        ArgumentCaptor<List<QuizCategoryTotal>> totalsCaptor = ArgumentCaptor.forClass(List.class);
        verify(quizCategoryTotalDAO, times(1)).bulkInsert(totalsCaptor.capture());
        List<QuizCategoryTotal> categoryTotals = totalsCaptor.getValue();
        
        // Should have 2 categories: NAHW and ADAB
        assertEquals(2, categoryTotals.size());
        
        // Verify NAHW totals: 3+0+4 = 7/12
        QuizCategoryTotal nahwTotal = categoryTotals.stream()
            .filter(ct -> ct.getCategory() == TopicCategory.NAHW)
            .findFirst().orElse(null);
        assertNotNull(nahwTotal);
        assertEquals(7.0, nahwTotal.getPointsEarned());
        assertEquals(12.0, nahwTotal.getTotalPoints());
        
        // Verify ADAB totals: 3+5 = 8/8
        QuizCategoryTotal adabTotal = categoryTotals.stream()
            .filter(ct -> ct.getCategory() == TopicCategory.ADAB)
            .findFirst().orElse(null);
        assertNotNull(adabTotal);
        assertEquals(8.0, adabTotal.getPointsEarned());
        assertEquals(8.0, adabTotal.getTotalPoints());

        assertNotNull(adabTotal);
        assertEquals(8.0, adabTotal.getPointsEarned());
        assertEquals(8.0, adabTotal.getTotalPoints());

        // Verify QuizGradedEvent published
        QuizGradedEvent gradedEvent = eventListener.getEventOfType(QuizGradedEvent.class);
        assertNotNull(gradedEvent, "QuizGradedEvent should be published");
        assertEquals(30, gradedEvent.getQuizId());
        assertEquals(1, gradedEvent.getStudentId());
        assertEquals(15.0, gradedEvent.getPointsEarned()); // Total: 3+0+4+3+5 = 15
        assertEquals(2, gradedEvent.getEnteredBy());
    }

    // ========== Test 7.5: Grade Student - First Lesson Student ==========
    @Test
    @DisplayName("Test 7.5: Grade Student - First Lesson Student")
    void testGradeStudent_FirstLessonStudent() {
        // Arrange
        Integer quizId = 30;
        Integer studentId = 200;
        Integer gradedBy = 2;

        // Mock quiz
        Quiz quiz = new Quiz(50, "PDF".getBytes(), 20.0, 1);
        quiz.setQuizId(30);
        when(quizDAO.findById(30)).thenReturn(quiz);

        // Mock attendance - student present
        Attendance attendance = new Attendance(50, 200, AttendanceStatus.PRESENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 200)).thenReturn(attendance);

        // Mock first lesson (only 1 attendance)
        when(attendanceDAO.countByStudentAndStatus(200, AttendanceStatus.PRESENT)).thenReturn(1);

        List<QuizScore> scores = createSampleScores();

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            quizService.gradeStudent(quizId, studentId, scores, gradedBy);
        });

        assertEquals("Cannot grade student in their first lesson", exception.getMessage());
        
        // Verify no scores inserted
        verify(quizScoreDAO, never()).bulkInsert(anyList());
    }

    // ========== Test 7.6: Grade Student - Absent Student ==========
    @Test
    @DisplayName("Test 7.6: Grade Student - Absent Student")
    void testGradeStudent_AbsentStudent() {
        // Arrange
        Integer quizId = 30;
        Integer studentId = 5;
        Integer gradedBy = 2;

        // Mock quiz
        Quiz quiz = new Quiz(50, "PDF".getBytes(), 20.0, 1);
        quiz.setQuizId(30);
        when(quizDAO.findById(30)).thenReturn(quiz);

        // Mock attendance - student absent
        Attendance attendance = new Attendance(50, 5, AttendanceStatus.ABSENT, 2);
        when(attendanceDAO.findByLessonAndStudent(50, 5)).thenReturn(attendance);

        List<QuizScore> scores = createSampleScores();

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            quizService.gradeStudent(quizId, studentId, scores, gradedBy);
        });

        assertEquals("Student 5 did not attend lesson 50", exception.getMessage());
        
        // Verify no scores inserted
        verify(quizScoreDAO, never()).bulkInsert(anyList());
    }

    // ========== Test 7.7: Auto-Grade MCQ - Correct Answer ==========
    @Test
    @DisplayName("Test 7.7: Auto-Grade MCQ - Correct Answer")
    void testAutoGradeMCQ_CorrectAnswer() {
        // Arrange
        String studentAnswer = "C";
        String modelAnswer = "C";
        Double maxPoints = 3.0;

        // Act
        Double result = quizService.autoGradeMCQ(studentAnswer, modelAnswer, maxPoints);

        // Assert
        assertEquals(3.0, result);
    }

    // ========== Test 7.8: Auto-Grade MCQ - Wrong Answer ==========
    @Test
    @DisplayName("Test 7.8: Auto-Grade MCQ - Wrong Answer")
    void testAutoGradeMCQ_WrongAnswer() {
        // Arrange
        String studentAnswer = "B";
        String modelAnswer = "A";
        Double maxPoints = 3.0;

        // Act
        Double result = quizService.autoGradeMCQ(studentAnswer, modelAnswer, maxPoints);

        // Assert
        assertEquals(0.0, result);
    }

    // ========== Test 7.9: Bulk Grade Quiz - Multiple Students ==========
    @Test
    @DisplayName("Test 7.9: Bulk Grade Quiz - Multiple Students")
    void testBulkGradeQuiz_MultipleStudents() {
        // Arrange
        Integer quizId = 30;
        Integer gradedBy = 2;

        // Mock quiz
        Quiz quiz = new Quiz(50, "PDF".getBytes(), 20.0, 1);
        quiz.setQuizId(30);
        when(quizDAO.findById(30)).thenReturn(quiz);

        // Mock questions
        List<QuizQuestion> questions = createSampleQuestions();
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setQuestionId(100 + i);
        }
        when(quizQuestionDAO.findByQuizId(30)).thenReturn(questions);

        // Prepare scores for 3 students
        Map<Integer, List<QuizScore>> studentScores = new HashMap<>();
        studentScores.put(1, createSampleScores());
        studentScores.put(2, createSampleScores());
        studentScores.put(4, createSampleScores());

        // Mock attendance for all 3 students
        for (Integer studentId : studentScores.keySet()) {
            Attendance attendance = new Attendance(50, studentId, AttendanceStatus.PRESENT, 2);
            when(attendanceDAO.findByLessonAndStudent(50, studentId)).thenReturn(attendance);
            when(attendanceDAO.countByStudentAndStatus(studentId, AttendanceStatus.PRESENT)).thenReturn(2);
        }

        // Mock insertions
        when(quizScoreDAO.bulkInsert(anyList())).thenReturn(true);
        when(quizCategoryTotalDAO.bulkInsert(anyList())).thenReturn(true);

        // Act
        boolean result = quizService.bulkGradeQuiz(quizId, studentScores, gradedBy);

        // Assert
        assertTrue(result);

        // Verify attendance checks for all 3 students
        verify(attendanceDAO, times(3)).findByLessonAndStudent(eq(50), anyInt());

        // Verify scores inserted for all 3 students
        verify(quizScoreDAO, times(3)).bulkInsert(anyList());

        // Verify category totals inserted for all 3 students
        verify(quizCategoryTotalDAO, times(3)).bulkInsert(anyList());

        // Verify category totals inserted for all 3 students
        verify(quizCategoryTotalDAO, times(3)).bulkInsert(anyList());

        // Verify QuizGradedEvent published for each student (3 events)
        List<QuizGradedEvent> gradedEvents = eventListener.getEventsOfType(QuizGradedEvent.class);
        assertEquals(3, gradedEvents.size(), "Should publish 3 QuizGradedEvents");

        // Verify QuizGradingCompletedEvent published once
        QuizGradingCompletedEvent completedEvent = eventListener.getEventOfType(QuizGradingCompletedEvent.class);
        assertNotNull(completedEvent, "QuizGradingCompletedEvent should be published");
        assertEquals(30, completedEvent.getQuizId());
        assertEquals(50, completedEvent.getLessonId());
        assertEquals(3, completedEvent.getTotalStudentsGraded());
        assertEquals(2, completedEvent.getCompletedBy());
    }

    // ========== Test 7.10: Calculate Quiz Points for Student ==========
    @Test
    @DisplayName("Test 7.10: Calculate Quiz Points for Student")
    void testCalculateQuizPoints() {
        // Arrange
        Integer studentId = 1;

        // Mock scores for 10 quizzes totaling 85 points
        List<QuizScore> allScores = new ArrayList<>();
        allScores.add(createScore(1, 8.0));
        allScores.add(createScore(2, 9.0));
        allScores.add(createScore(3, 7.0));
        allScores.add(createScore(4, 10.0));
        allScores.add(createScore(5, 8.5));
        allScores.add(createScore(6, 9.5));
        allScores.add(createScore(7, 7.5));
        allScores.add(createScore(8, 8.0));
        allScores.add(createScore(9, 9.0));
        allScores.add(createScore(10, 8.5));

        when(quizScoreDAO.findByStudentId(1)).thenReturn(allScores);

        // Act
        Double totalPoints = quizService.calculateQuizPoints(studentId);

        // Assert
        assertEquals(85.0, totalPoints);
        verify(quizScoreDAO, times(1)).findByStudentId(1);
    }

    // ========== Test 7.11: Update Quiz Score ==========
    @Test
    @DisplayName("Test 7.11: Update Quiz Score")
    void testUpdateQuizScore_Valid() {
        // Arrange
        Integer scoreId = 500;
        Double newPoints = 4.5;

        // Mock existing score
        QuizScore existingScore = new QuizScore(30, 1, 102, 3.0, LocalDateTime.now(), 2);
        existingScore.setScoreId(500);
        when(quizScoreDAO.findById(500)).thenReturn(existingScore);

        // Mock question with max 5.0 points
        QuizQuestion question = new QuizQuestion(30, 3, QuestionType.ESSAY, TopicCategory.NAHW, 5.0, null);
        question.setQuestionId(102);
        when(quizQuestionDAO.findById(102)).thenReturn(question);

        // Mock update
        when(quizScoreDAO.update(any(QuizScore.class))).thenReturn(true);

        // Mock recalculation
        List<QuizScore> allScores = new ArrayList<>();
        allScores.add(existingScore);
        when(quizScoreDAO.findByQuizAndStudent(30, 1)).thenReturn(allScores);

        List<QuizQuestion> questions = createSampleQuestions();
        when(quizQuestionDAO.findByQuizId(30)).thenReturn(questions);

        when(quizCategoryTotalDAO.bulkInsert(anyList())).thenReturn(true);

        // Act
        boolean result = quizService.updateQuizScore(scoreId, newPoints);

        // Assert
        assertTrue(result);

        // Verify score updated
        ArgumentCaptor<QuizScore> scoreCaptor = ArgumentCaptor.forClass(QuizScore.class);
        verify(quizScoreDAO, times(1)).update(scoreCaptor.capture());
        assertEquals(4.5, scoreCaptor.getValue().getPointsEarned());

        // Verify category totals recalculated
        verify(quizCategoryTotalDAO, times(1)).bulkInsert(anyList());

        // Verify category totals recalculated
        verify(quizCategoryTotalDAO, times(1)).bulkInsert(anyList());

        // Verify QuizGradedEvent published (triggers PI recalculation)
        QuizGradedEvent gradedEvent = eventListener.getEventOfType(QuizGradedEvent.class);
        assertNotNull(gradedEvent, "QuizGradedEvent should be published to trigger recalculation");
        assertEquals(30, gradedEvent.getQuizId());
        assertEquals(1, gradedEvent.getStudentId());
    }

    // ========== Test 7.12: Update Quiz Score - Exceeds Max Points ==========
    @Test
    @DisplayName("Test 7.12: Update Quiz Score - Exceeds Max Points")
    void testUpdateQuizScore_ExceedsMaxPoints() {
        // Arrange
        Integer scoreId = 500;
        Double newPoints = 6.0;

        // Mock existing score
        QuizScore existingScore = new QuizScore(30, 1, 102, 3.0, LocalDateTime.now(), 2);
        existingScore.setScoreId(500);
        when(quizScoreDAO.findById(500)).thenReturn(existingScore);

        // Mock question with max 5.0 points
        QuizQuestion question = new QuizQuestion(30, 3, QuestionType.ESSAY, TopicCategory.NAHW, 5.0, null);
        question.setQuestionId(102);
        when(quizQuestionDAO.findById(102)).thenReturn(question);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            quizService.updateQuizScore(scoreId, newPoints);
        });

        assertEquals("Points earned cannot exceed question max points", exception.getMessage());
        
        // Verify no update
        verify(quizScoreDAO, never()).update(any(QuizScore.class));
    }

    // ========== Helper Methods ==========

    private List<QuizQuestion> createSampleQuestions() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(null, 1, QuestionType.MCQ, TopicCategory.NAHW, 3.0, "C"));
        questions.add(new QuizQuestion(null, 2, QuestionType.MCQ, TopicCategory.NAHW, 4.0, "A"));
        questions.add(new QuizQuestion(null, 3, QuestionType.ESSAY, TopicCategory.NAHW, 5.0, null));
        questions.add(new QuizQuestion(null, 4, QuestionType.MCQ, TopicCategory.ADAB, 3.0, "B"));
        questions.add(new QuizQuestion(null, 5, QuestionType.ESSAY, TopicCategory.ADAB, 5.0, null));
        return questions;
    }

    private List<QuizScore> createSampleScores() {
        List<QuizScore> scores = new ArrayList<>();
        scores.add(new QuizScore(null, null, 100, 3.0, null, null));
        scores.add(new QuizScore(null, null, 101, 4.0, null, null));
        scores.add(new QuizScore(null, null, 102, 5.0, null, null));
        scores.add(new QuizScore(null, null, 103, 3.0, null, null));
        scores.add(new QuizScore(null, null, 104, 5.0, null, null));
        return scores;
    }

    private QuizScore createScore(Integer quizId, Double points) {
        return new QuizScore(quizId, 1, 100, points, LocalDateTime.now(), 2);
    }

    // ========== Event Listener for Testing ==========
    private static class TestEventListener {
        private final List<Object> capturedEvents = new ArrayList<>();

        @Subscribe
        public void onEvent(Object event) {
            capturedEvents.add(event);
        }

        public List<Object> getCapturedEvents() {
            return capturedEvents;
        }

        public void clear() {
            capturedEvents.clear();
        }

        public <T> T getEventOfType(Class<T> eventType) {
            return capturedEvents.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .findFirst()
                .orElse(null);
        }

        public <T> List<T> getEventsOfType(Class<T> eventType) {
            return capturedEvents.stream()
                .filter(eventType::isInstance)
                .map(eventType::cast)
                .toList();
        }
    }

    private TestEventListener eventListener;
}