package com.studenttracker.service.impl;


import com.studenttracker.dao.AttendanceDAO;
import com.studenttracker.dao.QuizCategoryTotalDAO;
import com.studenttracker.dao.QuizDAO;
import com.studenttracker.dao.QuizQuestionDAO;
import com.studenttracker.dao.QuizScoreDAO;
import com.studenttracker.dao.UserDAO;
import com.studenttracker.exception.ValidationException;
import com.studenttracker.model.Attendance;
import com.studenttracker.model.Attendance.AttendanceStatus;
import com.studenttracker.model.LessonTopic.TopicCategory;
import com.studenttracker.model.Quiz;
import com.studenttracker.model.QuizCategoryTotal;
import com.studenttracker.model.QuizQuestion;
import com.studenttracker.model.QuizScore;
import com.studenttracker.model.QuizStatistics;
import com.studenttracker.service.EventBusService;
import com.studenttracker.service.QuizService;
import com.studenttracker.service.event.QuizCreatedEvent;
import com.studenttracker.service.event.QuizGradedEvent;
import com.studenttracker.service.event.QuizGradingCompletedEvent;
import com.studenttracker.service.impl.helpers.QuizServiceImplHelpers;
import com.studenttracker.service.validator.AdminPermissionValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Implementation of QuizService.
 * Handles business logic, validation, and event publishing for quiz operations.
 */
public class QuizServiceImpl implements QuizService {
    
    private final QuizDAO quizDAO;
    private final QuizQuestionDAO quizQuestionDAO;
    private final QuizScoreDAO quizScoreDAO;
    private final QuizCategoryTotalDAO quizCategoryTotalDAO;
    private final AttendanceDAO attendanceDAO;
    private final UserDAO userDAO;
    private final EventBusService eventBusService;
    
    /**
     * Constructor with dependency injection.
     */
    public QuizServiceImpl(QuizDAO quizDAO, QuizQuestionDAO quizQuestionDAO,
                          QuizScoreDAO quizScoreDAO, QuizCategoryTotalDAO quizCategoryTotalDAO,
                           AttendanceDAO attendanceDAO, UserDAO userDAO) {
        this.quizDAO = quizDAO;
        this.quizQuestionDAO = quizQuestionDAO;
        this.quizScoreDAO = quizScoreDAO;
        this.quizCategoryTotalDAO = quizCategoryTotalDAO;
        this.attendanceDAO = attendanceDAO;
        this.userDAO = userDAO;
        this.eventBusService = EventBusService.getInstance();
    }
    
    
    // ========== Quiz Creation (Admin only) ==========
    
    @Override
    public Integer createQuiz(Integer lessonId, byte[] pdfData, 
                             List<QuizQuestion> questions, Integer createdBy) {
        // Step 1: Validate admin permission
        AdminPermissionValidator.validateAdminPermission(createdBy, userDAO);
        
        // Step 2: Validate at least one question
        if (questions == null || questions.isEmpty()) {
            throw new ValidationException("At least one question is required");
        }
        
        // Step 3: Calculate total marks
        BigDecimal totalMarks = BigDecimal.ZERO;
        for (QuizQuestion question : questions) {
            totalMarks = totalMarks.add(question.getPoints());
        }
        
        // Step 4: Create quiz entity
        Quiz quiz = new Quiz(lessonId, pdfData, totalMarks, createdBy);
        
        // Step 5: Insert quiz
        Integer quizId = quizDAO.insert(quiz);
        if (quizId == null) {
            return null;
        }
        
        // Step 6: Set quiz ID on all questions
        for (QuizQuestion question : questions) {
            question.setQuizId(quizId);
        }
        
        // Step 7: Bulk insert questions
        boolean questionsInserted = quizQuestionDAO.bulkInsert(questions);
        if (!questionsInserted) {
            return null;
        }
        
        // Step 8: Set the ID on quiz for event
        quiz.setQuizId(quizId);
        
        // Step 9: Publish QuizCreatedEvent
        QuizCreatedEvent event = new QuizCreatedEvent(quiz, createdBy);
        eventBusService.publish(event);
        
        return quizId;
    }
    
    @Override
    public boolean updateQuiz(Integer quizId, byte[] pdfData, 
                             List<QuizQuestion> questions, Integer updatedBy) {
        // Step 1: Validate admin permission
        AdminPermissionValidator.validateAdminPermission(updatedBy, userDAO);
        
        // Step 2: Validate no scores entered yet
        List<QuizScore> existingScores = quizScoreDAO.findByQuizId(quizId);
        if (existingScores != null && !existingScores.isEmpty()) {
            throw new ValidationException("Cannot update quiz after students have been graded");
        }
        
        // Step 3: Validate at least one question
        if (questions == null || questions.isEmpty()) {
            throw new ValidationException("At least one question is required");
        }
        
        // Step 4: Calculate new total marks
        BigDecimal totalMarks = BigDecimal.ZERO;
        for (QuizQuestion question : questions) {
            totalMarks = totalMarks.add(question.getPoints());
        }
        
        // Step 5: Get existing quiz
        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new ValidationException("Quiz not found: " + quizId);
        }
        
        // Step 6: Update quiz fields
        quiz.setQuizPdfData(pdfData);
        quiz.setTotalMarks(totalMarks);
        
        // Step 7: Update quiz
        boolean quizUpdated = quizDAO.update(quiz);
        if (!quizUpdated) {
            return false;
        }
        
        // Step 8: Delete old questions
        quizQuestionDAO.deleteByQuizId(quizId);
        
        // Step 9: Set quiz ID on new questions
        for (QuizQuestion question : questions) {
            question.setQuizId(quizId);
        }
        
        // Step 10: Insert new questions
        return quizQuestionDAO.bulkInsert(questions);
    }
    
    @Override
    public boolean deleteQuiz(Integer quizId, Integer deletedBy) {
        // Step 1: Validate admin permission
        AdminPermissionValidator.validateAdminPermission(deletedBy, userDAO);
        
        // Step 2: Delete quiz (CASCADE handles questions and scores)
        return quizDAO.delete(quizId);
    }
    
    
    // ========== Quiz Grading ==========
    
    @Override
    public boolean gradeStudent(Integer quizId, Integer studentId, 
                               List<QuizScore> scores, Integer gradedBy) {
        // Step 1: Get quiz and lesson information
        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new ValidationException("Quiz not found: " + quizId);
        }
        
        Integer lessonId = quiz.getLessonId();
        
        // Step 2: Validate student attended the lesson
        Attendance attendance = attendanceDAO.findByLessonAndStudent(lessonId, studentId);
        if (attendance == null || !attendance.isPresent()) {
            throw new ValidationException("Student " + studentId + " did not attend lesson " + lessonId);
        }
        
        // Step 3: Validate student is not in first lesson
        if (isFirstLesson(studentId, lessonId)) {
            throw new ValidationException("Cannot grade student in their first lesson");
        }
        
        // Step 4: Get quiz questions
        List<QuizQuestion> questions = quizQuestionDAO.findByQuizId(quizId);
        
        // Step 5: Validate scores match questions
        if (scores.size() != questions.size()) {
            throw new ValidationException("Number of scores must match number of questions");
        }
        
        // Step 6: Auto-grade MCQ questions
        Map<Integer, QuizQuestion> questionMap = new HashMap<>();
        for (QuizQuestion q : questions) {
            questionMap.put(q.getQuestionId(), q);
        }
        
        for (QuizScore score : scores) {
            QuizQuestion question = questionMap.get(score.getQuestionId());
            if (question != null && question.isMCQ()) {
                // For MCQ, we need the student's answer - assume it's stored somewhere
                // For now, we'll skip auto-grading and assume points are pre-calculated
                // In real implementation, you'd need to pass student answers
            }
            score.setQuizId(quizId);
            score.setStudentId(studentId);
            score.setEnteredAt(LocalDateTime.now());
            score.setEnteredBy(gradedBy);
        }
        
        // Step 7: Insert scores
        boolean scoresInserted = quizScoreDAO.bulkInsert(scores);
        if (!scoresInserted) {
            return false;
        }
        
        // Step 8: Calculate category totals
        List<QuizCategoryTotal> categoryTotals = QuizServiceImplHelpers.calculateCategoryTotals(quizId, studentId, scores, questions);
        
        // Step 9: Insert category totals
        boolean totalsInserted = quizCategoryTotalDAO.bulkInsert(categoryTotals);
        if (!totalsInserted) {
            return false;
        }
        
        // Step 10: Calculate total score
        BigDecimal totalScore = QuizServiceImplHelpers.calculateTotalScore(scores);
        
        // Step 11: Publish QuizGradedEvent
        QuizGradedEvent event = new QuizGradedEvent(quizId, studentId, totalScore, gradedBy);
        eventBusService.publish(event);
        
        return true;
    }
    
    @Override
    public boolean bulkGradeQuiz(Integer quizId, Map<Integer, List<QuizScore>> studentScores, 
                                Integer gradedBy) {
        // Step 1: Get quiz information
        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new ValidationException("Quiz not found: " + quizId);
        }
        
        Integer lessonId = quiz.getLessonId();
        
        // Step 2: Get quiz questions
        List<QuizQuestion> questions = quizQuestionDAO.findByQuizId(quizId);
        
        int studentsGraded = 0;
        
        // Step 3: Process each student
        for (Map.Entry<Integer, List<QuizScore>> entry : studentScores.entrySet()) {
            Integer studentId = entry.getKey();
            List<QuizScore> scores = entry.getValue();
            
            try {
                // Validate attendance
                Attendance attendance = attendanceDAO.findByLessonAndStudent(lessonId, studentId);
                if (attendance == null || !attendance.isPresent()) {
                    // Skip student and log error
                    System.err.println("Student " + studentId + " did not attend - skipping");
                    continue;
                }
                
                // Validate not first lesson
                if (isFirstLesson(studentId, lessonId)) {
                    System.err.println("Student " + studentId + " in first lesson - skipping");
                    continue;
                }
                
                // Set metadata on scores
                for (QuizScore score : scores) {
                    score.setQuizId(quizId);
                    score.setStudentId(studentId);
                    score.setEnteredAt(LocalDateTime.now());
                    score.setEnteredBy(gradedBy);
                }
                
                // Insert scores
                boolean scoresInserted = quizScoreDAO.bulkInsert(scores);
                if (!scoresInserted) {
                    continue;
                }
                
                // Calculate and insert category totals
                List<QuizCategoryTotal> categoryTotals = QuizServiceImplHelpers.calculateCategoryTotals(quizId, studentId, scores, questions);
                quizCategoryTotalDAO.bulkInsert(categoryTotals);
                
                // Calculate total score
                BigDecimal totalScore = QuizServiceImplHelpers.calculateTotalScore(scores);
                
                // Publish individual QuizGradedEvent
                QuizGradedEvent event = new QuizGradedEvent(quizId, studentId, totalScore, gradedBy);
                eventBusService.publish(event);
                
                studentsGraded++;
                
            } catch (Exception e) {
                System.err.println("Error grading student " + studentId + ": " + e.getMessage());
            }
        }
        
        // Step 4: Publish QuizGradingCompletedEvent
        QuizGradingCompletedEvent batchEvent = new QuizGradingCompletedEvent(quizId, studentsGraded, gradedBy);
        eventBusService.publish(batchEvent);
        
        return studentsGraded > 0;
    }
    
    @Override
    public boolean updateQuizScore(Integer scoreId, BigDecimal newPoints) {
        // Step 1: Get existing score
        QuizScore score = quizScoreDAO.findById(scoreId);
        if (score == null) {
            throw new ValidationException("Score not found: " + scoreId);
        }
        
        // Step 2: Get question to validate max points
        QuizQuestion question = quizQuestionDAO.findById(score.getQuestionId());
        if (question == null) {
            throw new ValidationException("Question not found");
        }
        
        // Step 3: Validate new points don't exceed max
        if (newPoints.compareTo(question.getPoints()) > 0) {
            throw new ValidationException("Points earned cannot exceed question max points");
        }
        
        // Step 4: Update score
        score.setPointsEarned(newPoints);
        boolean updated = quizScoreDAO.update(score);
        if (!updated) {
            return false;
        }
        
        // Step 5: Recalculate category totals
        List<QuizScore> allScores = quizScoreDAO.findByQuizAndStudent(score.getQuizId(), score.getStudentId());
        List<QuizQuestion> questions = quizQuestionDAO.findByQuizId(score.getQuizId());
        
        // Delete old category totals
        // Note: You'd need a deleteByQuizAndStudent method in DAO
        // For now, we'll just recalculate and update
        
        List<QuizCategoryTotal> categoryTotals = QuizServiceImplHelpers.calculateCategoryTotals(
            score.getQuizId(), score.getStudentId(), allScores, questions
        );
        quizCategoryTotalDAO.bulkInsert(categoryTotals);
        
        // Step 6: Calculate new total score
        BigDecimal totalScore = QuizServiceImplHelpers.calculateTotalScore(allScores);
        
        // Step 7: Publish QuizGradedEvent
        QuizGradedEvent event = new QuizGradedEvent(
            score.getQuizId(), score.getStudentId(), totalScore, score.getEnteredBy()
        );
        eventBusService.publish(event);
        
        return true;
    }
    
    
    // ========== Retrieval Operations ==========
    
    @Override
    public Quiz getQuizById(Integer quizId) {
        if (quizId == null) {
            throw new ValidationException("Quiz ID cannot be null");
        }
        return quizDAO.findById(quizId);
    }
    
    @Override
    public Quiz getQuizByLesson(Integer lessonId) {
        if (lessonId == null) {
            throw new ValidationException("Lesson ID cannot be null");
        }
        return quizDAO.findByLessonId(lessonId);
    }
    
    @Override
    public List<QuizQuestion> getQuizQuestions(Integer quizId) {
        if (quizId == null) {
            throw new ValidationException("Quiz ID cannot be null");
        }
        return quizQuestionDAO.findByQuizId(quizId);
    }
    
    @Override
    public byte[] getQuizPdf(Integer quizId) {
        if (quizId == null) {
            throw new ValidationException("Quiz ID cannot be null");
        }
        return quizDAO.getQuizPdf(quizId);
    }
    
    @Override
    public List<QuizScore> getStudentQuizScores(Integer quizId, Integer studentId) {
        if (quizId == null || studentId == null) {
            throw new ValidationException("Quiz ID and student ID cannot be null");
        }
        return quizScoreDAO.findByQuizAndStudent(quizId, studentId);
    }
    
    @Override
    public List<QuizCategoryTotal> getStudentCategoryTotals(Integer quizId, Integer studentId) {
        if (quizId == null || studentId == null) {
            throw new ValidationException("Quiz ID and student ID cannot be null");
        }
        return quizCategoryTotalDAO.findByQuizAndStudent(quizId, studentId);
    }
    
    
    // ========== Helper Methods ==========
    
    @Override
    public BigDecimal autoGradeMCQ(String studentAnswer, String modelAnswer, BigDecimal maxPoints) {
        if (studentAnswer == null || modelAnswer == null) {
            return BigDecimal.ZERO;
        }
        
        // Case-insensitive comparison
        if (studentAnswer.trim().equalsIgnoreCase(modelAnswer.trim())) {
            return maxPoints;
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Checks if this is a student's first lesson.
     * 
     * @param studentId ID of the student
     * @param lessonId ID of the lesson
     * @return true if first lesson, false otherwise
     */
    private boolean isFirstLesson(Integer studentId, Integer lessonId) {
        int attendanceCount = attendanceDAO.countByStudentAndStatus(studentId, AttendanceStatus.PRESENT);
        return attendanceCount == 1;
    }
    







// ========== Statistics ==========

@Override
public BigDecimal getStudentQuizTotal(Integer quizId, Integer studentId) {
    if (quizId == null || studentId == null) {
        throw new ValidationException("Quiz ID and student ID cannot be null");
    }
    return quizScoreDAO.getTotalScoreForStudent(quizId, studentId);
}

@Override
public QuizStatistics getQuizStatistics(Integer quizId) {
    if (quizId == null) {
        throw new ValidationException("Quiz ID cannot be null");
    }
    
    // Get all scores for the quiz
    List<QuizScore> allScores = quizScoreDAO.findByQuizId(quizId);
    
    if (allScores == null || allScores.isEmpty()) {
        return new QuizStatistics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
    
    // Group scores by student
    Map<Integer, BigDecimal> studentTotals = new HashMap<>();
    for (QuizScore score : allScores) {
        Integer studentId = score.getStudentId();
        BigDecimal currentTotal = studentTotals.getOrDefault(studentId, BigDecimal.ZERO);
        studentTotals.put(studentId, currentTotal.add(score.getPointsEarned()));
    }
    
    // Calculate statistics
    BigDecimal sum = BigDecimal.ZERO;
    BigDecimal highest = null;
    BigDecimal lowest = null;
    int passCount = 0;
    
    Quiz quiz = quizDAO.findById(quizId);
    BigDecimal passingScore = quiz.getTotalMarks().multiply(new BigDecimal("0.5")); // 50% to pass
    
    for (BigDecimal total : studentTotals.values()) {
        sum = sum.add(total);
        
        if (highest == null || total.compareTo(highest) > 0) {
            highest = total;
        }
        
        if (lowest == null || total.compareTo(lowest) < 0) {
            lowest = total;
        }
        
        if (total.compareTo(passingScore) >= 0) {
            passCount++;
        }
    }
    
    BigDecimal average = sum.divide(
        new BigDecimal(studentTotals.size()), 2, RoundingMode.HALF_UP
    );
    
    BigDecimal passRate = new BigDecimal(passCount)
        .divide(new BigDecimal(studentTotals.size()), 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"));
    
    return new QuizStatistics(average, highest, lowest, passRate);
}

@Override
public Map<TopicCategory, BigDecimal> getQuizCategoryBreakdown(Integer quizId) {
    if (quizId == null) {
        throw new ValidationException("Quiz ID cannot be null");
    }
    return quizQuestionDAO.getCategoryTotalsByQuiz(quizId);
}

@Override
public BigDecimal calculateQuizPoints(Integer studentId) {
    if (studentId == null) {
        throw new ValidationException("Student ID cannot be null");
    }
    
    // Get all quiz scores for student
    List<QuizScore> allScores = quizScoreDAO.findByStudentId(studentId);
    
    BigDecimal totalPoints = BigDecimal.ZERO;
    for (QuizScore score : allScores) {
        totalPoints = totalPoints.add(score.getPointsEarned());
    }
    
    return totalPoints;
}
}